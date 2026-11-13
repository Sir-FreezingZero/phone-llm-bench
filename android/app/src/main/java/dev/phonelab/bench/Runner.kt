package dev.phonelab.bench

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Runs one full round: fetch plan -> download models -> run tests -> upload results. */
class Runner(private val ctx: Context) {

    private val modelsDir = File(ctx.filesDir, "models").apply { mkdirs() }
    private val workDir = File(ctx.cacheDir, "run").apply { mkdirs() }

    fun run() {
        val token = Lab.token(ctx)
        val repo = Lab.repo(ctx)
        if (token.isBlank()) throw IOException("No GitHub token saved yet")
        val gh = GitHub(token, repo)

        Lab.setStatus("Fetching test plan…")
        val plan = JSONObject(gh.getRaw("plan.json"))
        val version = plan.getString("version")
        val coolTarget = plan.optDouble("cooldown_c", 34.0)
        val coolMaxWait = plan.optInt("cooldown_max_wait_s", 600)
        val testTimeoutMin = plan.optLong("test_timeout_min", 20)
        Lab.log("Plan $version")

        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
        val device = Device.info(ctx)
        val binaries = Device.binaries(ctx)
        Lab.log("Binaries in this app: ${binaries.keys.joinToString()}")

        // ---- models ----
        val modelErr = mutableMapOf<String, String>()
        val models = plan.optJSONArray("models") ?: JSONArray()
        val keep = mutableSetOf<String>()
        for (i in 0 until models.length()) {
            checkStop()
            val m = models.getJSONObject(i)
            val name = m.getString("name")
            keep += "$name.gguf"
            val f = File(modelsDir, "$name.gguf")
            if (!f.exists() || f.length() == 0L) {
                try {
                    download(m.getString("url"), f, name)
                } catch (e: Exception) {
                    modelErr[name] = "download failed: ${e.message}"
                    Lab.log("Download failed for $name: ${e.message}")
                }
            }
        }
        modelsDir.listFiles().orEmpty().forEach {
            if (it.name !in keep) { Lab.log("Removing old model ${it.name}"); it.delete() }
        }

        // ---- tests ----
        val tests = plan.optJSONArray("tests") ?: JSONArray()
        val results = JSONArray()
        val summary = StringBuilder()
        for (i in 0 until tests.length()) {
            if (Lab.stopRequested) break
            val t = tests.getJSONObject(i)
            val id = t.getString("id")
            val modelName = t.getString("model")
            val binName = t.optString("binary", "default")
            val pin = t.optString("pin", "")
            val args = t.optString("args", "").trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            val modelFile = File(modelsDir, "$modelName.gguf")
            val bin = binaries[binName]
            val r = JSONObject().put("id", id).put("model", modelName).put("binary", binName)
                .put("args", args.joinToString(" ")).put("pin", pin)

            val pre = when {
                modelErr[modelName] != null -> modelErr[modelName]
                !modelFile.exists() -> "model not in plan or missing: $modelName"
                bin == null -> "binary '$binName' not in this app build (have: ${binaries.keys.joinToString()})"
                else -> null
            }
            if (pre != null) {
                results.put(r.put("ok", false).put("error", pre))
                summary.append("✗ $id: $pre\n")
                continue
            }

            Lab.setStatus("Test ${i + 1}/${tests.length()}: $id")
            waitCool(coolTarget, coolMaxWait)
            val cmd = mutableListOf<String>()
            if (pin.isNotBlank()) cmd += listOf("/system/bin/taskset", cpuMask(pin))
            cmd += bin!!.absolutePath
            cmd += listOf("-m", modelFile.absolutePath)
            cmd += args
            cmd += listOf("-o", "json")

            val out = File(workDir, "out_$i.json")
            val err = File(workDir, "err_$i.txt")
            val t0 = Device.batteryTempC(ctx)
            val s0 = System.currentTimeMillis()
            val p = ProcessBuilder(cmd).directory(workDir)
                .redirectOutput(out).redirectError(err).start()
            var finished = false
            val deadline = s0 + TimeUnit.MINUTES.toMillis(testTimeoutMin)
            while (System.currentTimeMillis() < deadline) {
                if (p.waitFor(2, TimeUnit.SECONDS)) { finished = true; break }
                if (Lab.stopRequested) break
            }
            if (!finished) p.destroyForcibly()
            val rc = if (finished) p.exitValue() else -1
            val secs = (System.currentTimeMillis() - s0) / 1000
            val t1 = Device.batteryTempC(ctx)
            val bench = runCatching { JSONArray(out.readText()) }.getOrNull()

            r.put("ok", rc == 0 && bench != null).put("exit_code", rc)
                .put("temp_start_c", t0 ?: JSONObject.NULL).put("temp_end_c", t1 ?: JSONObject.NULL)
                .put("seconds", secs).put("bench", bench ?: JSONObject.NULL)
            if (!finished) r.put("error", if (Lab.stopRequested) "stopped by user" else "timed out after $testTimeoutMin min")
            if (rc != 0) r.put("stderr_tail", runCatching { err.readText().takeLast(2000) }.getOrDefault(""))
            results.put(r)
            summary.append(summarize(id, bench, rc)).append('\n')
            Lab.log(summarize(id, bench, rc))
        }

        // ---- save + upload ----
        val doc = JSONObject()
            .put("plan_version", version)
            .put("timestamp", stamp)
            .put("source", "android-app")
            .put("app_build", Device.appBuild(ctx))
            .put("device", device)
            .put("charging_at_end", Device.isCharging(ctx))
            .put("stopped_early", Lab.stopRequested)
            .put("binaries", Device.binaryInfo(ctx))
            .put("tests", results)
        val text = doc.toString(2)
        File(ctx.filesDir, "last_result.json").writeText(text)
        ctx.getSharedPreferences(Lab.PREFS, Context.MODE_PRIVATE).edit()
            .putString("last_summary", "Plan $version · $stamp\n$summary")
            .putString("last_plan_version", version).apply()

        Lab.setStatus("Uploading results…")
        gh.putFile("results/$stamp.json", text, "results $stamp (plan $version) from phone app")
        workDir.listFiles().orEmpty().forEach { it.delete() }
        Lab.setStatus(if (Lab.stopRequested) "Stopped. Partial results uploaded." else "Done. Results uploaded ($stamp).")
    }

    private fun checkStop() { if (Lab.stopRequested) throw IOException("Stopped by user") }

    private fun download(url: String, dest: File, name: String) {
        Lab.setStatus("Downloading $name…")
        val part = File(dest.path + ".part")
        val c = URL(url).openConnection() as HttpURLConnection
        c.instanceFollowRedirects = true
        c.connectTimeout = 30_000
        c.readTimeout = 60_000
        c.setRequestProperty("User-Agent", "phone-llm-lab")
        val code = c.responseCode
        if (code !in 200..299) throw IOException("HTTP $code")
        val total = c.contentLengthLong
        var done = 0L
        var lastPct = -1
        c.inputStream.use { input ->
            part.outputStream().use { output ->
                val buf = ByteArray(1 shl 16)
                while (true) {
                    if (Lab.stopRequested) throw IOException("Stopped by user")
                    val n = input.read(buf)
                    if (n < 0) break
                    output.write(buf, 0, n)
                    done += n
                    if (total > 0) {
                        val pct = (done * 100 / total).toInt()
                        if (pct >= lastPct + 10) { lastPct = pct; Lab.setStatus("Downloading $name… $pct%") }
                    }
                }
            }
        }
        if (total > 0 && done != total) { part.delete(); throw IOException("incomplete ($done of $total bytes)") }
        if (!part.renameTo(dest)) throw IOException("could not save file")
        Lab.log("Downloaded $name (${done / 1_048_576} MB)")
    }

    private fun waitCool(target: Double, maxWaitS: Int) {
        var waited = 0
        while (waited < maxWaitS && !Lab.stopRequested) {
            val t = Device.batteryTempC(ctx) ?: return
            if (t <= target) return
            Lab.setStatus("Cooling down: $t°C → $target°C")
            Thread.sleep(15_000)
            waited += 15
        }
    }

    companion object {
        /** "6,7" or "4-7" -> hex affinity mask for toybox taskset. */
        fun cpuMask(list: String): String {
            var mask = 0L
            for (part in list.split(',').map { it.trim() }.filter { it.isNotEmpty() }) {
                val bounds = part.split('-').map { it.trim().toInt() }
                val lo = bounds.first()
                val hi = bounds.last()
                for (cpu in lo..hi) mask = mask or (1L shl cpu)
            }
            return java.lang.Long.toHexString(mask)
        }

        fun summarize(id: String, bench: JSONArray?, rc: Int): String {
            if (bench == null || bench.length() == 0) return "✗ $id: failed (exit $rc)"
            var bestPp: Pair<Double, Int>? = null
            var bestTg: Pair<Double, Int>? = null
            for (k in 0 until bench.length()) {
                val row = bench.getJSONObject(k)
                val ts = row.optDouble("avg_ts", 0.0)
                val th = row.optInt("n_threads", 0)
                if (row.optInt("n_prompt", 0) > 0 && (bestPp == null || ts > bestPp.first)) bestPp = ts to th
                if (row.optInt("n_gen", 0) > 0 && (bestTg == null || ts > bestTg.first)) bestTg = ts to th
            }
            val tg = bestTg?.let { "tg %.1f t/s (%dt)".format(Locale.US, it.first, it.second) } ?: "tg –"
            val pp = bestPp?.let { "pp %.1f t/s (%dt)".format(Locale.US, it.first, it.second) } ?: "pp –"
            return "✓ $id: $tg · $pp"
        }
    }
}
