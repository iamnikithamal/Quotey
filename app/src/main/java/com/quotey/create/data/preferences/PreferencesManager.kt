package com.quotey.create.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quotey.create.data.model.AppPreferences
import com.quotey.create.data.model.AspectRatio
import com.quotey.create.data.model.ExportFormat
import com.quotey.create.data.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quotey_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val DEFAULT_ASPECT_RATIO = stringPreferencesKey("default_aspect_ratio")
        val DEFAULT_EXPORT_FORMAT = stringPreferencesKey("default_export_format")
        val DEFAULT_EXPORT_QUALITY = intPreferencesKey("default_export_quality")
        val AUTO_SAVE_ENABLED = booleanPreferencesKey("auto_save_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferences: Flow<AppPreferences> = dataStore.data.map { preferences ->
        AppPreferences(
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
            defaultAspectRatio = preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] ?: AspectRatio.SQUARE.name,
            defaultExportFormat = preferences[PreferencesKeys.DEFAULT_EXPORT_FORMAT] ?: ExportFormat.PNG.name,
            defaultExportQuality = preferences[PreferencesKeys.DEFAULT_EXPORT_QUALITY] ?: 100,
            autoSaveEnabled = preferences[PreferencesKeys.AUTO_SAVE_ENABLED] ?: true,
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        )
    }

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        try {
            ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setDefaultAspectRatio(aspectRatio: AspectRatio) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] = aspectRatio.name
        }
    }

    suspend fun setDefaultExportFormat(format: ExportFormat) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_EXPORT_FORMAT] = format.name
        }
    }

    suspend fun setDefaultExportQuality(quality: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_EXPORT_QUALITY] = quality.coerceIn(0, 100)
        }
    }

    suspend fun setAutoSaveEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        dataStore.edit { preferences ->
            val current = AppPreferences(
                hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
                defaultAspectRatio = preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] ?: AspectRatio.SQUARE.name,
                defaultExportFormat = preferences[PreferencesKeys.DEFAULT_EXPORT_FORMAT] ?: ExportFormat.PNG.name,
                defaultExportQuality = preferences[PreferencesKeys.DEFAULT_EXPORT_QUALITY] ?: 100,
                autoSaveEnabled = preferences[PreferencesKeys.AUTO_SAVE_ENABLED] ?: true,
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            )
            val updated = update(current)
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = updated.hasCompletedOnboarding
            preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] = updated.defaultAspectRatio
            preferences[PreferencesKeys.DEFAULT_EXPORT_FORMAT] = updated.defaultExportFormat
            preferences[PreferencesKeys.DEFAULT_EXPORT_QUALITY] = updated.defaultExportQuality
            preferences[PreferencesKeys.AUTO_SAVE_ENABLED] = updated.autoSaveEnabled
            preferences[PreferencesKeys.THEME_MODE] = updated.themeMode
        }
    }
}
