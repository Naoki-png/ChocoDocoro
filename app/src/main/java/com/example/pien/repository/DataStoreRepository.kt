package com.example.pien.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.pien.di.qualifiers.CurrentTabQualifier
import com.example.pien.di.qualifiers.SignInMethodQualifier
import com.example.pien.util.METHOD
import com.example.pien.util.SignInMethod
import com.example.pien.util.TAB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepository @Inject constructor(
    @SignInMethodQualifier private val signInMethodDataStore: DataStore<Preferences>,
    @CurrentTabQualifier private val currentTabDataStore: DataStore<Preferences>,
) {

    object PreferenceKeys {
        val method = stringPreferencesKey(METHOD)
        val tab = stringPreferencesKey(TAB)
    }

    suspend fun saveSignInMethod(method: String) {
        signInMethodDataStore.edit { preferences ->
            preferences[PreferenceKeys.method] = method
        }
    }

    suspend fun saveCurrentTab(currentTab: String) {
        signInMethodDataStore.edit { preferences ->
            preferences[PreferenceKeys.tab] = currentTab
        }
    }

    val readSignInMethod: Flow<SignInMethod> = signInMethodDataStore
        .data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
        SignInMethod.valueOf(preferences[PreferenceKeys.method] ?: SignInMethod.LOGOUT.name)
    }

    suspend fun readCurrentTab() = currentTabDataStore.data
}