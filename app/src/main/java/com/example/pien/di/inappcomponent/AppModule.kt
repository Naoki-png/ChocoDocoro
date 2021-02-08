package com.example.pien.di.inappcomponent

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.createDataStore
import com.example.pien.MyApplication
import com.example.pien.util.SIGNIN_METHOD
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun dataStore(application: Application) = (application as Context).createDataStore(name = SIGNIN_METHOD)


}