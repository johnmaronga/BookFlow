package com.johnmaronga.bookflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    val userId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY] != null
    }

    suspend fun saveSession(userId: Long, email: String, name: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            name?.let { preferences[USER_NAME_KEY] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getCurrentUserId(): Long? {
        var userId: Long? = null
        context.dataStore.data.map { preferences ->
            userId = preferences[USER_ID_KEY]
        }
        return userId
    }
}
