package com.example.pien.login

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.datastore.LoginDataStore
import com.example.pien.util.*
import com.google.android.gms.auth.api.signin.GoogleSignInApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleSignIn @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val googleSignInApi: GoogleSignInApi,
    private val loginDataStore: LoginDataStore
) {

    fun requestGoogleSignIn(fragment: Fragment) {
        val signInIntent = googleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, REQUEST_SIGN_IN_WITH_GOOGLE)
    }

    suspend fun handleResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_SIGN_IN_WITH_GOOGLE) {
            val result = googleSignInApi.getSignInResultFromIntent(data)
            if (result != null && result.isSuccess) {
                val credential = GoogleAuthProvider.getCredential(result.signInAccount?.idToken, null)
                val currentUser = firebaseAuth.signInWithCredential(credential).await().user
                loginDataStore.saveSignInMethod(SignInMethod.GOOGLE.name)
                return currentUser != null
            }
        }
        return false
    }

    suspend fun googleSignOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().await()
        loginDataStore.saveSignInMethod(SignInMethod.LOGOUT.name)
    }
}