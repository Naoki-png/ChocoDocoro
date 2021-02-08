package com.example.pien.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import com.example.pien.util.METHOD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class LoginDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferenceKeys {
        val signInMethod = preferencesKey<String>(METHOD)
    }

    suspend fun saveSignInMethod(method: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.signInMethod] = method
        }
    }

    val readSignInMethod: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val method = preferences[PreferenceKeys.signInMethod]
            method
        }
}