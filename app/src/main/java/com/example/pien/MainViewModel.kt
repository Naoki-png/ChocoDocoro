package com.example.pien

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.pien.data.model.Post
import com.example.pien.data.model.State
import com.example.pien.data.repository.PostRepository
import com.example.pien.util.REQUEST_GET_POST_IMAGE
import com.example.pien.util.REQUEST_GET_USER_IMAGE
import com.example.pien.util.makeToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(val app: Application) : AndroidViewModel(app) {
    var posts = MutableLiveData<List<Post>>()
    var myPosts = MutableLiveData<List<Post>>()
    private val postRepository = PostRepository()

    /**
     * 画像のUriをゲットする
     */
    fun getImageUriFromDevice(requestCode: Int, resultCode: Int, data: Intent?) : String? {
        postRepository.getImageUriFromDevice(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GET_POST_IMAGE -> return postRepository.postImageUriFromDevice
            REQUEST_GET_USER_IMAGE -> return postRepository.userImageUriFromDevice
            else -> return null
        }
    }

    /**
     * ユーザープロフィール変更
     */
    fun editUserInfo(userName: String, userImage: String) {
        postRepository.editUserInfo(userName, userImage)
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
                        Log.d("loading", "loading")
                        // load中に表示用のデータ処理
                    }
                    is State.Success -> {
                        posts.value = state.data
                        makeToast(app as Context, "Successfully Posted!")
                    }
                    is State.Failed -> {
                        // error時に表示用のデータ処理
                    }
                }
            }
        }
    }
}