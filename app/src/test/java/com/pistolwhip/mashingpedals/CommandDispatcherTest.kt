package com.pistolwhip.mashingpedals
import org.junit.Assert.assertEquals
import org.junit.Test
class CommandDispatcherTest {
    @Test fun moduleCommandsAreCaseInsensitive() {
        assertEquals(AppCommand.MAPS,CommandDispatcher.parse(" OPEN maps "))
        assertEquals(AppCommand.MUSIC,CommandDispatcher.parse("music"))
        assertEquals(AppCommand.HEALTH,CommandDispatcher.parse("show health"))
        assertEquals(AppCommand.SETTINGS,CommandDispatcher.parse("open settings"))
    }
    @Test fun rideAndMediaCommandsParseWithoutExecuting() {
        assertEquals(AppCommand.START_RIDE,CommandDispatcher.parse("start ride"))
        assertEquals(AppCommand.RESET_RIDE,CommandDispatcher.parse("reset ride"))
        assertEquals(AppCommand.NEXT,CommandDispatcher.parse("next track"))
        assertEquals(AppCommand.PREVIOUS,CommandDispatcher.parse("previous track"))
    }
    @Test fun unknownInputIsNotMappedToAnAction() {
        assertEquals(AppCommand.UNKNOWN,CommandDispatcher.parse("turn on the engine"))
        assertEquals(AppCommand.UNKNOWN,CommandDispatcher.parse(""))
    }
}
