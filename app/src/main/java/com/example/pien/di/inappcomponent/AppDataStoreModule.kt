package com.example.pien.di.inappcomponent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import com.example.pien.di.qualifiers.CurrentTabQualifier
import com.example.pien.di.qualifiers.SignInMethodQualifier
import com.example.pien.util.CURRENT_TAB
import com.example.pien.util.SIGN_IN_METHOD
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDataStoreModule {

    @Provides
    @SignInMethodQualifier
    fun signInMethodDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.createDataStore(name = SIGN_IN_METHOD)
    }

    @Provides
    @CurrentTabQualifier
    fun currentTabDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.createDataStore(name = CURRENT_TAB)
    }
}