package com.pistolwhip.mashingpedals
import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
@RunWith(RobolectricTestRunner::class)
class DashboardSettingsStoreTest {
    @Test fun settingsPersistAcrossStoreInstances() {
        val prefs=RuntimeEnvironment.getApplication().getSharedPreferences("settings-test",0)
        prefs.edit().clear().commit()
        val expected=DashboardSettings(DashboardSettings.LIGHT,DashboardSettings.COMPACT,false,true,false)
        DashboardSettingsStore(prefs).save(expected)
        assertEquals(expected,DashboardSettingsStore(prefs).load())
    }
    @Test fun defaultsAreDarkCardsWithAllMetricsVisible() {
        val prefs=RuntimeEnvironment.getApplication<Application>().getSharedPreferences("defaults-test",0)
        prefs.edit().clear().commit()
        assertEquals(DashboardSettings(),DashboardSettingsStore(prefs).load())
    }
}
