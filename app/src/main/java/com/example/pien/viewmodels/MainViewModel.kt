package com.example.pien.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.pien.models.Favorite
import com.example.pien.models.Post
import com.example.pien.repository.PostRepository
import com.example.pien.util.State
import com.example.pien.util.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(val app: Application) : AndroidViewModel(app) {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    var posts = MutableLiveData<List<Post>>()
    var searchedPosts = MutableLiveData<List<Post>>()
    var myPosts = MutableLiveData<List<Post>>()
    var favoritePosts = MutableLiveData<List<Post>>()
    var userProfileName = MutableLiveData<String>()
    var userProfileImage = MutableLiveData<String>()
    var state = MutableLiveData<String>()
    private val postRepository = PostRepository()

    /**
     * ユーザープロフィール変更
     */
    @ExperimentalCoroutinesApi
    fun editUserInfo(userName: String, userImage: String) {
        viewModelScope.launch {
            postRepository.editUserInfo(userName, userImage).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        myPosts.value = currentState.data
                        userProfileName.value = currentUser.displayName
                        userProfileImage.value = currentUser.photoUrl.toString()

                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    /**
     * Home表示用の全件データ取得
     */
    @ExperimentalCoroutinesApi
    fun getAllPosts() {
        viewModelScope.launch {
            postRepository.getAllPosts().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        posts.value = currentState.data

                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    /**
     * 検索データ取得
     */
    @ExperimentalCoroutinesApi
    fun getSearchedPosts(query: String) {
        viewModelScope.launch {
            postRepository.getSearchedPosts(query).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        searchedPosts.value = currentState.data

                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    /**
     * MyPage表示用のデータ取得
     */
    @ExperimentalCoroutinesApi
    fun getMyPosts() {
        viewModelScope.launch {
            postRepository.getMyPosts().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        myPosts.value = currentState.data

                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    /**
     * 投稿する
     */
    @ExperimentalCoroutinesApi
    fun addPost(post: Post) {
        viewModelScope.launch {
            postRepository.addPost(post).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        posts.value = currentState.data
                        makeToast(app as Context, "Successfully Posted!")

                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    /**
     * お気に入り追加
     */
    @ExperimentalCoroutinesApi
    fun addFavorite(favorite: Favorite) {
        viewModelScope.launch {
            postRepository.addFavorite(favorite).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        favoritePosts.value = currentState.data
                        makeToast(app as Context, "add to Favorite")

                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    /**
     * お気に入りデータ取得
     */
    fun getFavorite() {
        viewModelScope.launch {
            postRepository.getFavorite().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING.name
                    }
                    is State.Success -> {
                        favoritePosts.value = currentState.data
                        state.value = State.StateConst.SUCCESS.name
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED.name
                    }
                }
            }
        }
    }

    fun setUserProfileImage(imageUri: String) {
        userProfileImage.value = imageUri
    }

    fun setUserProfileName(displayName: String?) {
        userProfileName.value = displayName
    }
}