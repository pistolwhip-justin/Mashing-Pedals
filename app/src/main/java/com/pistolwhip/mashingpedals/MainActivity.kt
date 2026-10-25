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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = DashboardSettingsStore(getSharedPreferences("dashboard", MODE_PRIVATE))
        settings = store.load()
        render()
    }

    private fun render() {
        val dark = settings.theme == DashboardSettings.DARK
        val bg = if (dark) Color.rgb(18,24,32) else Color.rgb(245,247,250)
        val surface = if (dark) Color.rgb(31,41,53) else Color.WHITE
        val fg = if (dark) Color.rgb(242,246,250) else Color.rgb(27,39,52)
        val muted = if (dark) Color.rgb(174,188,203) else Color.rgb(90,105,120)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = if (dark) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val page = LinearLayout(this).apply { orientation = 1; setPadding(dp(16), dp(14), dp(16), dp(22)) }
        scroll.addView(page)
        page.addView(txt("Mashing Pedals",28,fg,true))
        page.addView(txt("Cycling dashboard · Phase 1 foundation",14,muted,false).apply { setPadding(0,dp(2),0,dp(12)) })

        val dash = box(surface,16)
        dash.addView(txt("RIDE COMPUTER",12,muted,true))
        dash.addView(txt("Preview only — live tracking arrives in Phase 2",12,muted,false).apply { setPadding(0,dp(4),0,dp(10)) })
        val row = LinearLayout(this).apply { orientation = 0 }
        val metrics = listOf(Triple(settings.showSpeed,"SPEED","—"),Triple(settings.showDistance,"DISTANCE","—"),Triple(settings.showElapsed,"ELAPSED","—")).filter { it.first }
        if (metrics.isEmpty()) row.addView(txt("All metrics hidden. Change this in Settings.",14,muted,false))
        metrics.forEachIndexed { i,m ->
            val tile = box(if(dark) Color.rgb(40,54,68) else Color.rgb(235,241,247),12).apply { gravity=Gravity.CENTER }
            tile.addView(txt(m.second,10,muted,true))
            tile.addView(txt(m.third,if(settings.layout==DashboardSettings.COMPACT)22 else 28,fg,true))
            row.addView(tile,LinearLayout.LayoutParams(0,-2,1f).apply { if(i>0) leftMargin=dp(6) })
        }
        dash.addView(row)
        page.addView(dash, margin(12))

        listOf(modules.take(3),modules.drop(3)).forEach { names ->
            val nav = LinearLayout(this).apply { orientation=0 }
            names.forEach { name ->
                nav.addView(Button(this).apply {
                    text = if(module==name) "• $name" else name
                    isAllCaps=false; textSize=13f
                    setOnClickListener { module=name; render() }
                },LinearLayout.LayoutParams(0,-2,1f).apply { rightMargin=dp(3) })
            }
            page.addView(nav,margin(4))
        }
        val content = box(surface,16)
        when(module) {
            "Dashboard" -> {
                content.addView(txt("Dashboard",20,fg,true))
                content.addView(txt("Ride metrics stay visible as you switch between modules. No GPS tracking is active in this phase.",14,muted,false))
            }
            "Maps" -> placeholder(content,"Maps","Map display and navigation are planned for Phase 4.",fg,muted)
            "Music" -> placeholder(content,"Music","Player status and media controls are planned for Phase 5.",fg,muted)
            "Health" -> placeholder(content,"Health","Live health data is planned for Phase 6, subject to supported APIs.",fg,muted)
            "Settings" -> {
                content.addView(txt("Settings",20,fg,true))
                content.addView(txt("Changes save automatically.",13,muted,false))
                choice(content,"Theme",settings.theme,listOf("Dark" to DashboardSettings.DARK,"Light" to DashboardSettings.LIGHT)) { settings=settings.copy(theme=it); persist() }
                choice(content,"Layout",settings.layout,listOf("Metric cards" to DashboardSettings.CARDS,"Compact cards" to DashboardSettings.COMPACT)) { settings=settings.copy(layout=it); persist() }
                content.addView(txt("Visible metrics",15,fg,true))
                check(content,"Speed",settings.showSpeed) { settings=settings.copy(showSpeed=it); persist() }
                check(content,"Distance",settings.showDistance) { settings=settings.copy(showDistance=it); persist() }
                check(content,"Elapsed time",settings.showElapsed) { settings=settings.copy(showElapsed=it); persist() }
            }
        }
        page.addView(content,margin(10))
        page.addView(Button(this).apply {
            text="Test a voice command"; isAllCaps=false
            setOnClickListener { commandDialog() }
        },ViewGroup.LayoutParams(-1,-2))
        page.addView(txt("Typed command test only · no microphone or wake phrase in Phase 1.",12,muted,false).apply { gravity=Gravity.CENTER })
        setContentView(scroll)
    }

    private fun commandDialog() {
        val input=EditText(this).apply { hint="Try “open maps” or “start ride”"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("Test command")
            .setMessage("Typed input only; this does not use the microphone.")
            .setView(input).setNegativeButton("Cancel",null)
            .setPositiveButton("Run") { _,_ -> handle(CommandDispatcher.parse(input.text.toString())) }.show()
    }
    private fun handle(c:AppCommand) {
        val dest=when(c) {
            AppCommand.DASHBOARD->"Dashboard"; AppCommand.MAPS->"Maps"; AppCommand.MUSIC->"Music"
            AppCommand.HEALTH->"Health"; AppCommand.SETTINGS->"Settings"; else->null
        }
        if(dest!=null) { module=dest; render(); Toast.makeText(this,"Opened $dest",Toast.LENGTH_SHORT).show(); return }
        val msg=when(c) {
            AppCommand.START_RIDE,AppCommand.PAUSE_RIDE,AppCommand.RESUME_RIDE,AppCommand.END_RIDE,AppCommand.RESET_RIDE -> "Ride command recognized; execution arrives in Phase 2."
            AppCommand.PLAY,AppCommand.PAUSE_MEDIA,AppCommand.STOP_MEDIA,AppCommand.NEXT,AppCommand.PREVIOUS -> "Media command recognized; integration arrives in Phase 5."
            else -> "Not recognized. Try “open maps”, “music”, or “settings”."
        }
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
    }
    private fun choice(parent:LinearLayout,title:String,selected:String,options:List<Pair<String,String>>,changed:(String)->Unit) {
        parent.addView(Button(this).apply {
            text="$title: "+(options.firstOrNull{it.second==selected}?.first ?: options.first().first)
            isAllCaps=false; gravity=Gravity.START or Gravity.CENTER_VERTICAL
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity).setTitle(title)
                    .setSingleChoiceItems(options.map{it.first}.toTypedArray(),options.indexOfFirst{it.second==selected}) { d,i -> d.dismiss(); changed(options[i].second) }
                    .setNegativeButton("Cancel",null).show()
            }
        },margin(3))
    }
    private fun check(parent:LinearLayout,title:String,checked:Boolean,changed:(Boolean)->Unit) {
        parent.addView(CheckBox(this).apply { text=title; isChecked=checked; setOnCheckedChangeListener { _,v->changed(v) } },ViewGroup.LayoutParams(-1,-2))
    }
    private fun placeholder(parent:LinearLayout,title:String,body:String,fg:Int,muted:Int) {
        parent.addView(txt(title,20,fg,true)); parent.addView(txt(body,14,muted,false))
    }
    private fun persist(){ store.save(settings); render() }
    private fun box(color:Int,radius:Int)=LinearLayout(this).apply {
        orientation=1; setPadding(dp(12),dp(12),dp(12),dp(12))
        background=GradientDrawable().apply{setColor(color);cornerRadius=dp(radius).toFloat()}
    }
    private fun txt(s:String,size:Int,color:Int,bold:Boolean)=TextView(this).apply {
        text=s;textSize=size.toFloat();setTextColor(color);if(bold)setTypeface(typeface,Typeface.BOLD)
    }
    private fun margin(bottom:Int)=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(bottom)}
    private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
}
