package dev.phonelab.bench

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared state between the screen and the background service. */
object Lab {
    const val PREFS = "lab"
    const val DEFAULT_REPO = "Sir-FreezingZero/phone-llm-bench"

    private val lines = ArrayDeque<String>()
    @Volatile var status: String = "Idle"
        private set
    @Volatile var running = false
    @Volatile var stopRequested = false
    @Volatile var onStatus: ((String) -> Unit)? = null

    fun log(msg: String) {
        val t = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        synchronized(lines) {
            lines.addLast("$t  $msg")
            while (lines.size > 400) lines.removeFirst()
        }
        Log.i("LlmLab", msg)
    }

    fun setStatus(s: String) {
        status = s
        log(s)
        onStatus?.invoke(s)
    }

    fun logText(): String = synchronized(lines) { lines.reversed().joinToString("\n") }

    fun repo(ctx: Context): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("repo", DEFAULT_REPO) ?: DEFAULT_REPO

    fun token(ctx: Context): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("token", "") ?: ""
}

/** Minimal GitHub REST client (contents + releases). */
class GitHub(private val token: String, private val repo: String) {

    private fun open(path: String, method: String = "GET"): HttpURLConnection {
        val c = URL("https://api.github.com/repos/$repo/$path").openConnection() as HttpURLConnection
        c.requestMethod = method
        if (token.isNotBlank()) c.setRequestProperty("Authorization", "Bearer ${token.trim()}")
        c.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        c.setRequestProperty("User-Agent", "phone-llm-lab")
        c.connectTimeout = 20_000
        c.readTimeout = 60_000
        return c
    }

    private fun read(c: HttpURLConnection): String {
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw IOException("GitHub HTTP $code: ${body.take(300)}")
        return body
    }

    fun getRaw(path: String): String {
        val c = open("contents/$path")
        c.setRequestProperty("Accept", "application/vnd.github.raw+json")
        return read(c)
    }

    fun latestRelease(): JSONObject {
        val c = open("releases/latest")
        c.setRequestProperty("Accept", "application/vnd.github+json")
        return JSONObject(read(c))
    }

    fun putFile(path: String, content: String, message: String) {
        val c = open("contents/$path", "PUT")
        c.doOutput = true
        c.setRequestProperty("Accept", "application/vnd.github+json")
        c.setRequestProperty("Content-Type", "application/json")
        val body = JSONObject()
            .put("message", message)
            .put("content", Base64.encodeToString(content.toByteArray(), Base64.NO_WRAP))
        c.outputStream.use { it.write(body.toString().toByteArray()) }
        read(c)
    }
}

/** Phone sensors and facts. */
object Device {
    fun batteryTempC(ctx: Context): Double? {
        val i: Intent? = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val t = i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        return if (t == Int.MIN_VALUE) null else t / 10.0
    }

    fun isCharging(ctx: Context): Boolean {
        val i: Intent? = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return (i?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
    }

    fun info(ctx: Context): JSONObject {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val freqs = JSONObject()
        for (n in 0 until 16) {
            val f = File("/sys/devices/system/cpu/cpu$n/cpufreq/cpuinfo_max_freq")
            if (f.exists()) runCatching { freqs.put("cpu$n", f.readText().trim().toLong()) }
        }
        val soc = if (Build.VERSION.SDK_INT >= 31) "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}" else Build.HARDWARE
        return JSONObject()
            .put("model", "${Build.MANUFACTURER} ${Build.MODEL}")
            .put("soc", soc)
            .put("android", Build.VERSION.RELEASE)
            .put("cores", Runtime.getRuntime().availableProcessors())
            .put("mem_mb", mi.totalMem / 1_048_576)
            .put("avail_mb_at_start", mi.availMem / 1_048_576)
            .put("cpu_max_khz", freqs)
    }

    /** Executables packaged in the APK, keyed by variant name. */
    fun binaries(ctx: Context): Map<String, File> {
        val dir = File(ctx.applicationInfo.nativeLibraryDir)
        return dir.listFiles().orEmpty()
            .filter { it.name.startsWith("libllamabench_") && it.name.endsWith(".so") }
            .associateBy { it.name.removePrefix("libllamabench_").removeSuffix(".so") }
    }

    /** Build info written by CI (llama.cpp commit and cmake flags per variant). */
    fun binaryInfo(ctx: Context): JSONArray = runCatching {
        JSONArray(ctx.assets.open("binaries.json").bufferedReader().use { it.readText() })
    }.getOrDefault(JSONArray())

    fun appBuild(ctx: Context): Long {
        val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode else @Suppress("DEPRECATION") pi.versionCode.toLong()
    }
}
