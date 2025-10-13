package com.example.incidentscompose.data.store

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.incidentscompose.util.JwtDecoder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class TokenPreferences(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    private fun getTokenFlow() = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun getToken(): String? = getTokenFlow().first()

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun getUserRole(): String? {
        val token = getToken() ?: return null
        return JwtDecoder.getRoleFromToken(token)
    }
}