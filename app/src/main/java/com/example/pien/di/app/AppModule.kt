package com.example.pien.di.app

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun firebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    fun firebaseCurrentUser(firebaseAuth: FirebaseAuth) = firebaseAuth.currentUser

}