package com.example.pien.di.inappcomponent

import android.app.Application
import com.example.pien.MyApplication
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
    @Singleton
    fun firebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    fun firebaseCurrentUser(firebaseAuth: FirebaseAuth) = firebaseAuth.currentUser

}