package com.example.pien.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
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
    var cheapPosts = MutableLiveData<List<Post>>()
    var luxuryPosts = MutableLiveData<List<Post>>()
    var searchedPosts = MutableLiveData<List<Post>>()
    var myPosts = MutableLiveData<List<Post>>()
    var favoritePosts = MutableLiveData<List<Post>>()
    var userProfileName = MutableLiveData<String>()
    var userProfileImage = MutableLiveData<String>()
    var state = MutableLiveData<State.StateConst>()
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
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        myPosts.value = currentState.data
                        userProfileName.value = currentUser.displayName
                        userProfileImage.value = currentUser.photoUrl.toString()

                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
                    }
                }
            }
        }
    }

    /**
     * Cheapデータ取得
     */
    @ExperimentalCoroutinesApi
    fun getCheapPosts() {
        viewModelScope.launch {
            postRepository.getCheapPosts().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        cheapPosts.value = currentState.data

                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
                    }
                }
            }
        }
    }

    /**
     * Cheapデータ取得
     */
    @ExperimentalCoroutinesApi
    fun getLuxuryPosts() {
        viewModelScope.launch {
            postRepository.getLuxuryPosts().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        luxuryPosts.value = currentState.data

                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
                    }
                }
            }
        }
    }

    /**
     * 検索データ取得
     */
    @ExperimentalCoroutinesApi
    fun getSearchedPosts(query: String, currentTab: String?) {
        viewModelScope.launch {
            postRepository.getSearchedPosts(query, currentTab).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        searchedPosts.value = currentState.data

                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
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
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        myPosts.value = currentState.data

                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
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
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        cheapPosts.value = currentState.data
                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
                    }
                }
            }
        }
    }

    /**
     * お気に入り追加
     */
    @ExperimentalCoroutinesApi
    fun addFavorite(post: Post) {
        viewModelScope.launch {
            postRepository.addFavorite(post).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        makeToast(app as Context, "add to Favorite")
                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
                    }
                }
            }
        }
    }

    /**
     * お気に入りから削除
     */
    @ExperimentalCoroutinesApi
    fun removeFavorite(post: Post) {
        viewModelScope.launch {
            postRepository.removeFavorite(post).collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        makeToast(app as Context, "remove from Favorite")
                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
                    }
                }
            }
        }
    }

    /**
     * お気に入りデータ取得
     */
    @ExperimentalCoroutinesApi
    fun getFavorite() {
        viewModelScope.launch {
            postRepository.getFavorite().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        state.value = State.StateConst.LOADING
                    }
                    is State.Success -> {
                        favoritePosts.value = currentState.data
                        state.value = State.StateConst.SUCCESS
                    }
                    is State.Failed -> {
                        state.value = State.StateConst.FAILED
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