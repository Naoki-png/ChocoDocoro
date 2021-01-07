package com.example.pien.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.pien.models.Post
import com.example.pien.util.State
import com.example.pien.repository.PostRepository
import com.example.pien.util.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(val app: Application) : AndroidViewModel(app) {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    var posts = MutableLiveData<List<Post>>()
    var myPosts = MutableLiveData<List<Post>>()
    var userProfileName = MutableLiveData<String>()
    var userProfileImage = MutableLiveData<String>()
    private val postRepository = PostRepository()

    /**
     * ユーザープロフィール変更
     */
    fun editUserInfo(userName: String, userImage: String) {
        viewModelScope.launch {
            postRepository.editUserInfo(userName, userImage).collect { state ->
                when (state) {
                    is State.Loading -> {
                        // load中に表示用のデータ処理
                    }
                    is State.Success -> {
                        myPosts.value = state.data
                        userProfileName.value = currentUser.displayName
                        userProfileImage.value = currentUser.photoUrl.toString()
                    }
                    is State.Failed -> {
                        // error時に表示用のデータ処理
                    }
                }
            }
        }
    }

    /**
     * Home表示用の全件データ取得
     */
    fun getAllPosts() {
        viewModelScope.launch {
            postRepository.getAllPosts().collect { state ->
                when (state) {
                    is State.Loading -> {
                        // load中に表示用のデータ処理
                    }
                    is State.Success -> {
                        posts.value = state.data
                    }
                    is State.Failed -> {
                        // error時に表示用のデータ処理
                    }
                }
            }
        }
    }

    /**
     * MyPage表示用のデータ取得
     */
    fun getMyPosts() {
        viewModelScope.launch {
            postRepository.getMyPosts().collect { state ->
                when (state) {
                    is State.Loading -> {
                        // load中に表示用のデータ処理
                    }
                    is State.Success -> {
                        myPosts.value = state.data
                    }
                    is State.Failed -> {
                        // error時に表示用のデータ処理
                    }
                }
            }
        }
    }

    /**
     * 投稿する
     */
    fun addPost(post: Post) {
        viewModelScope.launch {
            postRepository.addPost(post).collect { state ->
                when (state) {
                    is State.Loading -> {
                        Log.d("PostFragment", "loading")
                        // load中に表示用のデータ処理
                    }
                    is State.Success -> {
                        posts.value = state.data
                        makeToast(app as Context, "Successfully Posted!")
                    }
                    is State.Failed -> {
                        // error時に表示用のデータ処理
                        Log.e("PostFragment", "posting isn't completed")
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