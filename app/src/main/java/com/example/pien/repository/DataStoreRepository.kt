package com.example.pien.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import com.example.pien.di.qualifiers.CurrentTabQualifier
import com.example.pien.di.qualifiers.SignInMethodQualifier
import com.example.pien.util.METHOD
import com.example.pien.util.TAB
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepository @Inject constructor(
    @SignInMethodQualifier private val signInMethodDataStore: DataStore<Preferences>,
    @CurrentTabQualifier private val currentTabDataStore: DataStore<Preferences>,
) {

    private object PreferenceKeys {
        val method = preferencesKey<String>(METHOD)
        val tab = preferencesKey<String>(TAB)
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

    suspend fun readSignInMethod() = signInMethodDataStore.data

    suspend fun readCurrentTab() = currentTabDataStore.data
}