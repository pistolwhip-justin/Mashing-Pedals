package com.pistolwhip.mashingpedals

import android.content.SharedPreferences

data class DashboardSettings(
    val theme: String = DARK, val layout: String = CARDS,
    val showSpeed: Boolean = true, val showDistance: Boolean = true, val showElapsed: Boolean = true
) {
    companion object {
        const val DARK = "dark"
        const val LIGHT = "light"
        const val CARDS = "cards"
        const val COMPACT = "compact"
    }
}
class DashboardSettingsStore(private val prefs: SharedPreferences) {
    fun load() = DashboardSettings(
        theme = prefs.getString("theme", DashboardSettings.DARK) ?: DashboardSettings.DARK,
        layout = prefs.getString("layout", DashboardSettings.CARDS) ?: DashboardSettings.CARDS,
        showSpeed = prefs.getBoolean("speed", true),
        showDistance = prefs.getBoolean("distance", true),
        showElapsed = prefs.getBoolean("elapsed", true)
    )
    fun save(s: DashboardSettings) {
        prefs.edit().putString("theme", s.theme).putString("layout", s.layout)
            .putBoolean("speed", s.showSpeed).putBoolean("distance", s.showDistance)
            .putBoolean("elapsed", s.showElapsed).apply()
    }
}
