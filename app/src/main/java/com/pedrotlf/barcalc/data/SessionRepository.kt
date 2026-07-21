package com.pedrotlf.barcalc.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pedrotlf.barcalc.ui.TabSession
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.sessionDataStore by preferencesDataStore(name = "bar_tab_session")

/**
 * Persists the in-progress [TabSession] as JSON so a half-entered tab
 * survives an app restart. Cleared on reset (Done).
 */
class SessionRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(): TabSession? {
        val raw = context.sessionDataStore.data.first()[KEY_SESSION] ?: return null
        return runCatching { json.decodeFromString<TabSession>(raw) }.getOrNull()
    }

    suspend fun save(session: TabSession) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_SESSION] = json.encodeToString(TabSession.serializer(), session)
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.remove(KEY_SESSION) }
    }

    private companion object {
        val KEY_SESSION = stringPreferencesKey("session_json")
    }
}
