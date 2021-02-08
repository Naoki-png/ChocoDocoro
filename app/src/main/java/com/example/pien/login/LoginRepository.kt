package com.example.pien.login

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.pien.datastore.LoginDataStore
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginDataStore: LoginDataStore,
    private val googleSignIn: GoogleSignIn,
    private val facebookSignIn: FacebookSignIn
) {

    suspend fun signIn(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        var result = false
        when (requestCode) {
            0 ->  {
                result = googleSignIn.handleResult(requestCode, resultCode, data)
            }
        }
        return result
    }

    suspend fun signOut() {
        loginDataStore.readSignInMethod.collect { value ->
            if (value != null) {
                when (SignInMethod.valueOf(value)) {
                    SignInMethod.GOOGLE -> googleSignIn.googleSignOut()
                    SignInMethod.FACEBOOK -> facebookSignIn.facebookSignOut()
                }
            }
        }
    }

    fun requestGoogleSignIn(fragment: Fragment) {
        googleSignIn.requestGoogleSignIn(fragment)
    }
}