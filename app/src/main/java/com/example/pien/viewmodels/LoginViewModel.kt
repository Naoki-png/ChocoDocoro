package com.example.pien.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pien.repository.LoginRepository
import com.example.pien.util.State
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel @ViewModelInject constructor(
    private val loginRepository: LoginRepository
): ViewModel() {

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Flow<State<State.StateConst>> {
        return loginRepository.firebaseAuthWithGoogle(account)
    }

}