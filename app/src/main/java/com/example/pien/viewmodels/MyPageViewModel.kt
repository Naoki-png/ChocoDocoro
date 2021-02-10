package com.example.pien.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pien.repository.PostRepository
import com.example.pien.util.State
import com.google.firebase.auth.FirebaseUser
import dagger.assisted.Assisted

class MyPageViewModel @ViewModelInject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    private var _state = MutableLiveData<State.StateConst>()
    val state: LiveData<State.StateConst> = _state

    fun deleteAccountCompletely() {
//        deleteAccount()
        deleteAccountsPosts()
//        deleteAccountsFavorites()
//        deleteAccountsStorage()
    }

    private fun deleteAccount() {
        postRepository.deleteAccount()
    }

    private fun deleteAccountsPosts() {
        postRepository.deleteAccountsPosts()
    }

    private fun deleteAccountsFavorites() {
        postRepository.deleteAccountsFavorites()
    }

    private fun deleteAccountsStorage() {
        postRepository.deleteAccountsStorage()
    }
}