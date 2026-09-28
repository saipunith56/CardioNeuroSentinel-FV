package com.example

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ThemePreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemePreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testDefaultTheme() = runTest {
        val testFile = tempFolder.newFile("default_theme.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { testFile }
        )
        val themePrefs = ThemePreferences(dataStore)

        // Requirement: Default theme must be false (Light theme)
        val initialFromFlow = themePrefs.isDarkThemeFlow.first()
        val initialFromGet = themePrefs.getInitialDarkTheme()

        assertFalse("Default theme should be light (false)", initialFromFlow)
        assertFalse("Default initial theme should be light (false)", initialFromGet)
    }

    @Test
    fun testEnablingDarkTheme() = runTest {
        val testFile = tempFolder.newFile("enable_theme.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { testFile }
        )
        val themePrefs = ThemePreferences(dataStore)

        // Requirement: Enabling dark theme
        themePrefs.setDarkTheme(true)

        val updatedFlow = themePrefs.isDarkThemeFlow.first()
        val updatedGet = themePrefs.getInitialDarkTheme()

        assertTrue("Dark theme should be enabled (true)", updatedFlow)
        assertTrue("Initial dark theme getter should return true", updatedGet)
    }

    @Test
    fun testDisablingDarkTheme() = runTest {
        val testFile = tempFolder.newFile("disable_theme.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { testFile }
        )
        val themePrefs = ThemePreferences(dataStore)

        // First enable dark theme
        themePrefs.setDarkTheme(true)
        assertTrue(themePrefs.isDarkThemeFlow.first())

        // Requirement: Disabling dark theme
        themePrefs.setDarkTheme(false)

        val updatedFlow = themePrefs.isDarkThemeFlow.first()
        val updatedGet = themePrefs.getInitialDarkTheme()

        assertFalse("Dark theme should be disabled (false)", updatedFlow)
        assertFalse("Initial dark theme getter should return false", updatedGet)
    }

    @Test
    fun testPersistedValueRestored() = runTest {
        val testFile = tempFolder.newFile("restore_test.preferences_pb")
        val job1 = kotlinx.coroutines.Job()
        val scope1 = kotlinx.coroutines.CoroutineScope(backgroundScope.coroutineContext + job1)

        // 1. First instance sets dark theme to true
        val dataStore1 = PreferenceDataStoreFactory.create(
            scope = scope1,
            produceFile = { testFile }
        )
        val themePrefs1 = ThemePreferences(dataStore1)
        themePrefs1.setDarkTheme(true)
        assertTrue("Instance 1 set dark theme to true", themePrefs1.isDarkThemeFlow.first())

        // Close first DataStore by cancelling its scope (simulates process termination)
        job1.cancel()

        // 2. Second instance reads from the same underlying file, simulating process restart
        val job2 = kotlinx.coroutines.Job()
        val scope2 = kotlinx.coroutines.CoroutineScope(backgroundScope.coroutineContext + job2)
        val dataStore2 = PreferenceDataStoreFactory.create(
            scope = scope2,
            produceFile = { testFile }
        )
        val themePrefs2 = ThemePreferences(dataStore2)

        val restoredFromGet = themePrefs2.getInitialDarkTheme()
        val restoredFromFlow = themePrefs2.isDarkThemeFlow.first()

        // Requirement: Persisted value being restored
        assertTrue("Persisted dark theme should be restored across instances via getInitialDarkTheme", restoredFromGet)
        assertTrue("Persisted dark theme should be restored across instances via isDarkThemeFlow", restoredFromFlow)

        job2.cancel()
    }

    @Test
    fun testContextThemePreferences() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val themePrefs = ThemePreferences.getInstance(context)

        themePrefs.setDarkTheme(false)
        assertFalse(themePrefs.getInitialDarkTheme())

        themePrefs.setDarkTheme(true)
        assertTrue(themePrefs.getInitialDarkTheme())
    }
}
