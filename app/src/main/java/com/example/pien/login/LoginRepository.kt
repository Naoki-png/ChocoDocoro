package com.example.pien.login

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.pien.datastore.LoginDataStore
import com.example.pien.util.State
import com.facebook.FacebookCallback
import com.facebook.login.LoginResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataStore: LoginDataStore,
    private val googleSignIn: GoogleSignIn,
    val myFaceBookCallback: MyFaceBookCallback
) {
    val facebookCallbackManger = myFaceBookCallback.callbackManager

    fun requestGoogleSignIn(fragment: Fragment) {
        googleSignIn.requestGoogleSignIn(fragment)
    }

    fun signIn(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                CoroutineScope(Dispatchers.Main).launch {
                    googleSignIn.handleResult(requestCode, resultCode, data)
                }
            }
            else -> {
                facebookCallbackManger.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    suspend fun signOut() {
        loginDataStore.readSignInMethod.collect { value ->
            if (value != null) {
                when (SignInMethod.valueOf(value)) {
                    SignInMethod.GOOGLE -> googleSignIn.googleSignOut()
                    SignInMethod.FACEBOOK -> myFaceBookCallback.facebookSignOut()
                }
            }
        }
    }
}