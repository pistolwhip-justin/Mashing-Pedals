package com.pistolwhip.mashingpedals

enum class AppCommand {
    DASHBOARD, MAPS, MUSIC, HEALTH, SETTINGS, START_RIDE, PAUSE_RIDE, RESUME_RIDE,
    END_RIDE, RESET_RIDE, PLAY, PAUSE_MEDIA, STOP_MEDIA, NEXT, PREVIOUS, UNKNOWN
}
object CommandDispatcher {
    fun parse(input: String): AppCommand {
        val s = input.trim().lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ")
        return when (s) {
            "dashboard", "open dashboard", "show dashboard", "home" -> AppCommand.DASHBOARD
            "maps", "map", "open maps", "show maps" -> AppCommand.MAPS
            "music", "open music", "show music" -> AppCommand.MUSIC
            "health", "open health", "show health" -> AppCommand.HEALTH
            "settings", "open settings", "show settings" -> AppCommand.SETTINGS
            "start ride", "begin ride" -> AppCommand.START_RIDE
            "pause ride" -> AppCommand.PAUSE_RIDE
            "resume ride", "continue ride" -> AppCommand.RESUME_RIDE
            "stop ride", "end ride", "finish ride" -> AppCommand.END_RIDE
            "reset ride" -> AppCommand.RESET_RIDE
            "play", "play music", "resume music" -> AppCommand.PLAY
            "pause", "pause music" -> AppCommand.PAUSE_MEDIA
            "stop music" -> AppCommand.STOP_MEDIA
            "next", "next track", "skip track" -> AppCommand.NEXT
            "previous", "last track", "previous track" -> AppCommand.PREVIOUS
            else -> AppCommand.UNKNOWN
        }
    }
}
