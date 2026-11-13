package dev.phonelab.bench

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var summary: TextView
    private lateinit var logView: TextView
    private lateinit var runBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var updateBtn: Button
    private val ui = Handler(Looper.getMainLooper())
    private var updateUrl: String? = null

    private val tick = object : Runnable {
        override fun run() {
            refresh()
            ui.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(Lab.PREFS, Context.MODE_PRIVATE)
        val pad = (16 * resources.displayMetrics.density).toInt()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        fun label(text: String, size: Float = 14f, bold: Boolean = false) = TextView(this).apply {
            this.text = text
            textSize = size
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, pad / 2, 0, pad / 4)
        }.also { col.addView(it) }

        label("LLM Speed Lab", 22f, true)
        label("Build ${Device.appBuild(this)} · binaries: ${Device.binaries(this).keys.sorted().joinToString().ifEmpty { "none" }}", 12f)

        label("GitHub repo (owner/name)")
        val repo = EditText(this).apply {
            setText(Lab.repo(this@MainActivity))
            setSingleLine()
        }
        col.addView(repo)

        label("GitHub token (fine-grained, Contents: read & write)")
        val token = EditText(this).apply {
            setText(Lab.token(this@MainActivity))
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        col.addView(token)

        val saveBtn = Button(this).apply { text = "Save" }
        col.addView(saveBtn)
        saveBtn.setOnClickListener {
            prefs.edit().putString("repo", repo.text.toString().trim())
                .putString("token", token.text.toString().trim()).apply()
            Lab.log("Settings saved")
            checkUpdate()
        }

        runBtn = Button(this).apply { text = "▶  Run tests" }
        stopBtn = Button(this).apply { text = "■  Stop" }
        updateBtn = Button(this).apply { text = "Check for app update"; visibility = View.VISIBLE }
        col.addView(runBtn); col.addView(stopBtn); col.addView(updateBtn)

        runBtn.setOnClickListener {
            if (!Device.isCharging(this)) Lab.log("Tip: plug in the charger for steadier numbers")
            BenchService.start(this)
        }
        stopBtn.setOnClickListener {
            Lab.stopRequested = true
            Lab.log("Stopping after the current step…")
        }
        updateBtn.setOnClickListener {
            val url = updateUrl
            if (url != null) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) else checkUpdate()
        }

        status = label("", 16f, true)
        label("Last results", 14f, true)
        summary = label("", 13f)
        label("Log", 14f, true)
        logView = label("", 11f).apply { typeface = Typeface.MONOSPACE }

        setContentView(ScrollView(this).apply { addView(col) })

        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Lab.token(this).isBlank()) Lab.log("Paste your GitHub token above and tap Save.")
        checkUpdate()
    }

    override fun onResume() { super.onResume(); ui.post(tick) }
    override fun onPause() { super.onPause(); ui.removeCallbacks(tick) }

    private fun refresh() {
        val prefs = getSharedPreferences(Lab.PREFS, Context.MODE_PRIVATE)
        status.text = if (Lab.running) "Running: ${Lab.status}" else Lab.status
        summary.text = prefs.getString("last_summary", "No runs yet.")
        logView.text = Lab.logText()
        runBtn.isEnabled = !Lab.running
        stopBtn.isEnabled = Lab.running
    }

    private fun checkUpdate() {
        val token = Lab.token(this)
        if (token.isBlank()) return
        val repo = Lab.repo(this)
        val mine = Device.appBuild(this)
        thread {
            val msg = try {
                val rel = GitHub(token, repo).latestRelease()
                val latest = rel.optString("tag_name").removePrefix("build-").toLongOrNull() ?: 0
                if (latest > mine) {
                    updateUrl = rel.optString("html_url")
                    "⬆  Update available: build $latest (tap to open)"
                } else {
                    updateUrl = null
                    "App is up to date (build $mine)"
                }
            } catch (e: Exception) {
                updateUrl = null
                Lab.log("Update check failed: ${e.message}")
                "Check for app update"
            }
            ui.post { updateBtn.text = msg }
        }
    }
}
