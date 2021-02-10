package com.example.pien.di.inappcomponent

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppFirebaseModule {

    @Provides
    @Singleton
    fun firebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun firebaseFireStore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun firebaseStorage() = FirebaseStorage.getInstance()

}