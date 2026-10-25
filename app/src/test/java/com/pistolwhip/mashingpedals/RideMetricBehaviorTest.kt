package com.pistolwhip.mashingpedals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
class RideMetricBehaviorTest {
 @Test fun unavailableValuesRemainNull(){val heart:Int?=null;val cadence:Int?=null;assertNull(heart);assertNull(cadence)}
 @Test fun averageUsesOnlyAvailableSamples(){assertEquals(15.0,listOf(10.0,20.0).average(),0.001)}
 @Test fun pausedTimeDoesNotAdvance(){fun tick(speed:Double?,s:Long)=if(speed!=null&&speed>0)s+1 else s;assertEquals(7L,tick(0.0,7));assertEquals(7L,tick(null,7));assertEquals(8L,tick(3.0,7))}
}