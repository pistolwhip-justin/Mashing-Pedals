package com.pistolwhip.mashingpedals

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardSettingsStoreTest {
    @Test fun settingsPersistAcrossStoreInstances() {
        val prefs = InMemorySharedPreferences()
        val expected = DashboardSettings(DashboardSettings.LIGHT, DashboardSettings.COMPACT, false, true, false)
        DashboardSettingsStore(prefs).save(expected)
        assertEquals(expected, DashboardSettingsStore(prefs).load())
    }

    @Test fun defaultsAreDarkCardsWithAllMetricsVisible() {
        assertEquals(DashboardSettings(), DashboardSettingsStore(InMemorySharedPreferences()).load())
    }
}

private class InMemorySharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any>()

    override fun getAll(): Map<String, *> = values.toMap()
    override fun getString(key: String, defValue: String?): String? = values[key] as? String ?: defValue
    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
        (values[key] as? Set<*>)?.filterIsInstance<String>()?.toMutableSet() ?: defValues
    override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = values[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
    override fun contains(key: String): Boolean = values.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun edit(): SharedPreferences.Editor = MemoryEditor()

    private inner class MemoryEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any>()
        private val removals = mutableSetOf<String>()
        private var clearFirst = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor = change(key, value)
        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = change(key, values?.toSet())
        override fun putInt(key: String, value: Int): SharedPreferences.Editor = change(key, value)
        override fun putLong(key: String, value: Long): SharedPreferences.Editor = change(key, value)
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = change(key, value)
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = change(key, value)
        override fun remove(key: String): SharedPreferences.Editor {
            pending.remove(key)
            removals.add(key)
            return this
        }
        override fun clear(): SharedPreferences.Editor {
            clearFirst = true
            return this
        }
        override fun commit(): Boolean {
            applyChanges()
            return true
        }
        override fun apply() = applyChanges()

        private fun change(key: String, value: Any?): SharedPreferences.Editor {
            removals.remove(key)
            if (value == null) pending.remove(key) else pending[key] = value
            if (value == null) removals.add(key)
            return this
        }
        private fun applyChanges() {
            if (clearFirst) values.clear()
            removals.forEach(values::remove)
            values.putAll(pending)
        }
    }
}
