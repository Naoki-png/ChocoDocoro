package com.example.pien.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.pien.repository.LoginRepository
import com.example.pien.util.SignInMethod
import com.example.pien.util.State
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow

class LoginViewModel @ViewModelInject constructor(
    private val loginRepository: LoginRepository
): ViewModel() {

    suspend fun loginCheck(): Flow<State<SignInMethod>> {
        return loginRepository.loginCheck()
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Flow<State<State.StateConst>> {
        return loginRepository.firebaseAuthWithGoogle(account)
    }

    suspend fun signOut(): Flow<State<State.StateConst>> {
        return loginRepository.signOut()
    }
}