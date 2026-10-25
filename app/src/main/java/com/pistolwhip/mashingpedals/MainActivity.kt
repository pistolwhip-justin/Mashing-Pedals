package com.pistolwhip.mashingpedals

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private lateinit var store: DashboardSettingsStore
    private var settings = DashboardSettings()
    private var module = "Dashboard"
    private val modules = listOf("Dashboard", "Maps", "Music", "Health", "Settings")

    private val amoledBlack = Color.BLACK
    private val darkTile = Color.rgb(12, 12, 12)
    private val darkBorder = Color.rgb(38, 38, 38)
    private val accent = Color.rgb(80, 210, 170)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        store = DashboardSettingsStore(getSharedPreferences("dashboard", MODE_PRIVATE))
        settings = store.load()
        render()
    }

    private fun render() {
        val dark = settings.theme == DashboardSettings.DARK
        val bg = if (dark) amoledBlack else Color.rgb(245, 247, 250)
        val surface = if (dark) amoledBlack else Color.WHITE
        val tileColor = if (dark) darkTile else Color.rgb(235, 241, 247)
        val border = if (dark) darkBorder else Color.rgb(220, 226, 233)
        val fg = if (dark) Color.rgb(245, 245, 245) else Color.rgb(27, 39, 52)
        val muted = if (dark) Color.rgb(165, 165, 165) else Color.rgb(90, 105, 120)

        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = if (dark) 0 else
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setBackgroundColor(bg)
        }

        // The module workspace takes two thirds; the ride computer stays in the right third.
        val workspace = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(2), dp(8), dp(2))
        }
        val rideComputer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = rounded(surface, 16, border)
        }
        root.addView(workspace, LinearLayout.LayoutParams(0, -1, 2f))
        root.addView(rideComputer, LinearLayout.LayoutParams(0, -1, 1f).apply {
            leftMargin = dp(4)
        })

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val heading = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        heading.addView(txt("Mashing Pedals", 22, fg, true))
        heading.addView(txt("Cycling dashboard · Phase 1", 12, muted, false))
        header.addView(heading, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = "Test command"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(if (dark) accent else Color.rgb(0, 120, 90))
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(tileColor, 10, border)
            contentDescription = "Test a typed voice command"
            setOnClickListener { commandDialog() }
        }, LinearLayout.LayoutParams(-2, dp(42)))
        workspace.addView(header, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(8)
        })

        // The module body scrolls independently above the fixed bottom selector.
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            setBackgroundColor(bg)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(2), dp(8), dp(8))
        }
        scroll.addView(content)
        workspace.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        renderModule(content, dark, fg, muted, tileColor, border)

        // All module buttons stay in one horizontal row at the bottom of the left workspace.
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        modules.forEach { name ->
            val selected = module == name
            val item = TextView(this).apply {
                text = if (name == "Dashboard") "Ride" else name
                textSize = 11f
                gravity = Gravity.CENTER
                maxLines = 1
                setTextColor(if (selected) (if (dark) Color.BLACK else Color.WHITE) else fg)
                background = rounded(
                    if (selected) (if (dark) accent else Color.rgb(25, 115, 90)) else tileColor,
                    10, border
                )
                setPadding(dp(2), dp(4), dp(2), dp(4))
                contentDescription = "Open $name module"
                isClickable = true
                isFocusable = true
                setOnClickListener { module = name; render() }
            }
            nav.addView(item, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                if (nav.childCount > 0) leftMargin = dp(4)
            })
        }
        workspace.addView(nav, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(8)
        })

        // Persistent ride computer panel, always on the right regardless of selected module.
        rideComputer.addView(txt("RIDE COMPUTER", 13, fg, true))
        rideComputer.addView(txt("Preview only · live tracking in Phase 2", 11, muted, false).apply {
            setPadding(0, dp(3), 0, dp(8))
        })
        val visibleMetrics = listOf(
            Triple(settings.showSpeed, "SPEED", "—"),
            Triple(settings.showDistance, "DISTANCE", "—"),
            Triple(settings.showElapsed, "ELAPSED TIME", "—")
        ).filter { it.first }
        if (visibleMetrics.isEmpty()) {
            rideComputer.addView(txt("All metrics hidden. Enable them in Settings.", 13, muted, false))
        } else {
            visibleMetrics.forEachIndexed { index, metric ->
                val tile = box(tileColor, 12, border).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(12), dp(7), dp(12), dp(7))
                }
                tile.addView(txt(metric.second, 10, muted, true))
                tile.addView(txt(metric.third, if (settings.layout == DashboardSettings.COMPACT) 23 else 30, fg, true)
                    .apply { setPadding(0, dp(1), 0, 0) })
                rideComputer.addView(tile, LinearLayout.LayoutParams(-1, 0, 1f).apply {
                    if (index > 0) topMargin = dp(7)
                })
            }
        }
        setContentView(root)
    }

    private fun renderModule(
        parent: LinearLayout,
        dark: Boolean,
        fg: Int,
        muted: Int,
        tileColor: Int,
        border: Int
    ) {
        val panel = box(if (dark) amoledBlack else Color.WHITE, 14, border)
        when (module) {
            "Dashboard" -> {
                panel.addView(txt("Dashboard", 20, fg, true))
                panel.addView(txt(
                    "Your ride computer stays visible on the right while you switch modules here.",
                    14, muted, false
                ).apply { setPadding(0, dp(4), 0, dp(10)) })
                panel.addView(txt(
                    "Phase 1 preview: values are placeholders. GPS tracking and ride controls arrive in Phase 2.",
                    13, muted, false
                ))
            }
            "Maps" -> placeholder(panel, "Maps",
                "Map display, current position, and navigation are planned for Phase 4.", fg, muted)
            "Music" -> placeholder(panel, "Music",
                "Player status and supported media controls are planned for Phase 5.", fg, muted)
            "Health" -> placeholder(panel, "Health",
                "Live health data is planned for Phase 6, subject to verified device/API support.", fg, muted)
            "Settings" -> {
                panel.addView(txt("Settings", 20, fg, true))
                panel.addView(txt("Changes save automatically.", 12, muted, false).apply {
                    setPadding(0, dp(2), 0, dp(8))
                })
                choice(panel, "Theme", settings.theme,
                    listOf("AMOLED black" to DashboardSettings.DARK, "Light" to DashboardSettings.LIGHT)) {
                    settings = settings.copy(theme = it); persist()
                }
                choice(panel, "Layout", settings.layout,
                    listOf("Metric cards" to DashboardSettings.CARDS, "Compact cards" to DashboardSettings.COMPACT)) {
                    settings = settings.copy(layout = it); persist()
                }
                panel.addView(txt("Visible ride-computer metrics", 14, fg, true).apply {
                    setPadding(0, dp(6), 0, dp(2))
                })
                check(panel, "Speed", settings.showSpeed, fg, dark) {
                    settings = settings.copy(showSpeed = it); persist()
                }
                check(panel, "Distance", settings.showDistance, fg, dark) {
                    settings = settings.copy(showDistance = it); persist()
                }
                check(panel, "Elapsed time", settings.showElapsed, fg, dark) {
                    settings = settings.copy(showElapsed = it); persist()
                }
            }
        }
        parent.addView(panel, LinearLayout.LayoutParams(-1, -2))
    }

    private fun commandDialog() {
        val input = EditText(this).apply {
            hint = "Try “open maps” or “start ride”"
            setSingleLine(true)
        }
        AlertDialog.Builder(this, dialogTheme())
            .setTitle("Test command")
            .setMessage("Typed input only; this does not use the microphone.")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Run") { _, _ -> handle(CommandDispatcher.parse(input.text.toString())) }
            .show()
    }

    private fun dialogTheme() = if (settings.theme == DashboardSettings.DARK) {
        android.R.style.Theme_Material_Dialog_Alert
    } else {
        android.R.style.Theme_Material_Light_Dialog_Alert
    }

    private fun handle(c: AppCommand) {
        val dest = when (c) {
            AppCommand.DASHBOARD -> "Dashboard"
            AppCommand.MAPS -> "Maps"
            AppCommand.MUSIC -> "Music"
            AppCommand.HEALTH -> "Health"
            AppCommand.SETTINGS -> "Settings"
            else -> null
        }
        if (dest != null) {
            module = dest
            render()
            Toast.makeText(this, "Opened $dest", Toast.LENGTH_SHORT).show()
            return
        }
        val msg = when (c) {
            AppCommand.START_RIDE, AppCommand.PAUSE_RIDE, AppCommand.RESUME_RIDE,
            AppCommand.END_RIDE, AppCommand.RESET_RIDE ->
                "Ride command recognized; execution arrives in Phase 2."
            AppCommand.PLAY, AppCommand.PAUSE_MEDIA, AppCommand.STOP_MEDIA,
            AppCommand.NEXT, AppCommand.PREVIOUS ->
                "Media command recognized; integration arrives in Phase 5."
            else -> "Not recognized. Try “open maps”, “music”, or “settings”."
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun choice(
        parent: LinearLayout,
        title: String,
        selected: String,
        options: List<Pair<String, String>>,
        changed: (String) -> Unit
    ) {
        parent.addView(TextView(this).apply {
            text = "$title: " + (options.firstOrNull { it.second == selected }?.first ?: options.first().first)
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(if (settings.theme == DashboardSettings.DARK) Color.WHITE else Color.rgb(27, 39, 52))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(
                if (settings.theme == DashboardSettings.DARK) darkTile else Color.rgb(245, 247, 250),
                10, if (settings.theme == DashboardSettings.DARK) darkBorder else Color.rgb(220, 226, 233)
            )
            isClickable = true
            isFocusable = true
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity, dialogTheme()).setTitle(title)
                    .setSingleChoiceItems(options.map { it.first }.toTypedArray(),
                        options.indexOfFirst { it.second == selected }) { dialog, index ->
                        dialog.dismiss()
                        changed(options[index].second)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(5) })
    }

    private fun check(
        parent: LinearLayout,
        title: String,
        checked: Boolean,
        color: Int,
        dark: Boolean,
        changed: (Boolean) -> Unit
    ) {
        parent.addView(CheckBox(this).apply {
            text = title
            setTextColor(color)
            buttonTintList = android.content.res.ColorStateList.valueOf(
                if (dark) accent else Color.rgb(25, 115, 90)
            )
            isChecked = checked
            setOnCheckedChangeListener { _, value -> changed(value) }
        }, ViewGroup.LayoutParams(-1, -2))
    }

    private fun placeholder(parent: LinearLayout, title: String, body: String, fg: Int, muted: Int) {
        parent.addView(txt(title, 20, fg, true))
        parent.addView(txt(body, 14, muted, false).apply { setPadding(0, dp(4), 0, 0) })
    }

    private fun persist() {
        store.save(settings)
        render()
    }

    private fun box(color: Int, radius: Int, border: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(12), dp(12), dp(12))
        background = rounded(color, radius, border)
    }

    private fun rounded(color: Int, radius: Int, border: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), border)
    }

    private fun txt(s: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = s
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
}
