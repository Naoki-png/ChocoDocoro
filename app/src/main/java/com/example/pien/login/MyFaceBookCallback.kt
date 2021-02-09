package com.example.pien.login

import android.util.Log
import com.example.pien.datastore.LoginDataStore
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MyFaceBookCallback @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val loginDataStore: LoginDataStore,
    private val loginManager: LoginManager,
    val callbackManager: CallbackManager
): FacebookCallback<LoginResult>, BaseLoginCallback() {

    override fun onSuccess(loginResult: LoginResult?) {
        firebaseAuthWithFacebook(loginResult?.accessToken!!)
        Log.d("Facebook Login", "facebook:onSuccess:$loginResult")
    }

    override fun onCancel() {
        Log.d("Facebook Login", "facebook:onCancel")
    }

    override fun onError(error: FacebookException?) {
        Log.d("Facebook Login", "facebook:onError", error)
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        CoroutineScope(Dispatchers.Main).launch {
            firebaseAuth.signInWithCredential(credential).await()
            loginDataStore.saveSignInMethod(SignInMethod.FACEBOOK.name)
            loginListener!!.onLoginCompleted()
        }
    }

    suspend fun facebookSignOut() {
        firebaseAuth.signOut()
        loginManager.logOut()
        loginDataStore.saveSignInMethod(SignInMethod.LOGOUT.name)
        logoutListener!!.onLogoutCompleted()
    }

}