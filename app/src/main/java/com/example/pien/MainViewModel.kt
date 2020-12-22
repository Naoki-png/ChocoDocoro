package com.example.pien

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pien.data.model.Post
import com.example.pien.data.repository.PostRepository

class MainViewModel(val context: Application) : AndroidViewModel(context) {
    /**
     * ListFragment表示用のPost一覧
     */
    var posts = MutableLiveData<List<Post>>()

    /**
     * MyPageFragment表示用のPost一覧
     */
    var myPosts = MutableLiveData<List<Post>>()

    /**
     * ユーザーの表示名
     */
    var userDisplayName = MutableLiveData<String>()

    /**
     * ユーザーのアイコンのUri
     */
    var userPhotoUri = MutableLiveData<String>()

    /**
     * post用レポジトリ
     */
    private val postRepository = PostRepository()

    /**
     * 投稿画像のUriをゲットする
     */
    fun getPostImageUri(requestCode: Int, resultCode: Int, data: Intent?) : String? {
        postRepository.getPostImageUri(requestCode, resultCode, data)
        return postRepository.postImageUriFromDevice
    }

    /**
     * 投稿する
     */
    fun post(message: String, currentDisplayPhotoUri: String) {
        postRepository.post(message, currentDisplayPhotoUri)
    }

    fun setHomeData() {
        postRepository.setHomeData()
    }

    fun observeFields() {
        postRepository.homeListData.observeForever { postList ->
            posts.value = postList
        }
    }
}