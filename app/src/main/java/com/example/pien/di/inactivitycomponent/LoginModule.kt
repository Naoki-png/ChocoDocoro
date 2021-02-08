package com.example.pien.di.inactivitycomponent

import android.app.Activity
import android.app.Application
import com.example.pien.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object LoginModule {

    // FirebaseAuth
    @Provides
    fun firebaseAuth() = FirebaseAuth.getInstance()

    // Google SignIn
    @Provides
    fun googleSignInOptions(activity: Activity): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    @Provides
    fun googleSignInClient(
        googleSignInOptions: GoogleSignInOptions,
        activity: Activity
    ) = GoogleSignIn.getClient(activity, googleSignInOptions)

    @Provides
    fun googleSignInApi() = Auth.GoogleSignInApi



}