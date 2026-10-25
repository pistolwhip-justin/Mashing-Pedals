package com.pistolwhip.mashingpedals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RideMetricBehaviorTest {
    @Test
    fun unavailableValuesRemainNull() {
        val heart: Int? = null
        val cadence: Int? = null
        assertNull(heart)
        assertNull(cadence)
    }

    @Test
    fun averageUsesAvailableSamplesOnly() {
        assertEquals(15.0, listOf(10.0, 20.0).average(), 0.001)
    }

    @Test
    fun pausedTimeDoesNotAdvance() {
        fun tick(speed: Double?, seconds: Long): Long {
            return if (speed != null && speed > 0.0) seconds + 1 else seconds
        }
        assertEquals(7L, tick(0.0, 7L))
        assertEquals(7L, tick(null, 7L))
        assertEquals(8L, tick(3.0, 7L))
    }
}
