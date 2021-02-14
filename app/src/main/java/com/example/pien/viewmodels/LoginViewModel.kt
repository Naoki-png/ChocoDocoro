package com.example.pien.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pien.repository.LoginRepository
import com.example.pien.util.SignInMethod
import com.example.pien.util.State
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoginViewModel @ViewModelInject constructor(
    private val loginRepository: LoginRepository
): ViewModel() {

    suspend fun loginCheck(): Flow<State<SignInMethod>> {
        return loginRepository.loginCheck()
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Flow<State<State.StateConst>> {
        return loginRepository.firebaseAuthWithGoogle(account)
    }

    fun firebaseAuthWithFacebook(accessToken: AccessToken): Flow<State<State.StateConst>> {
        return loginRepository.firebaseAuthWithFacebook(accessToken)
    }

    suspend fun signOut(): Flow<State<State.StateConst>> {
        return loginRepository.signOut()
    }
}