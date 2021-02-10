package com.example.pien.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pien.repository.PostRepository
import com.example.pien.util.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPageViewModel @ViewModelInject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    private var _state = MutableLiveData<State.StateConst>()
    val state: LiveData<State.StateConst> = _state

    fun deleteAccountCompletely() {
        //todo make coroutineScope accurate scope(should be MypageFragmentScope, this method be called in the fragment)
        CoroutineScope(Dispatchers.IO).launch {
            deleteAllFilesInStorage()
            deleteAllPosts()
            deleteAllFavorites()
//            deleteAccount()
        }
    }

    private suspend fun deleteAllFilesInStorage() {
        postRepository.deleteAllFilesInStorage()
    }

    private suspend fun deleteAllPosts() {
        postRepository.deleteAllPosts()
    }

    private suspend fun deleteAllFavorites() {
        postRepository.deleteAllFavorites()
    }

    private suspend fun deleteAccount() {
        postRepository.deleteAccount()
    }
}