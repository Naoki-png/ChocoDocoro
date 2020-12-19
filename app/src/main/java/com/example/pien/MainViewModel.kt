package com.example.pien

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pien.data.model.Post
import com.example.pien.data.repository.PostRepository

class MainViewModel : ViewModel() {
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

    private val postRepository = PostRepository()

    fun getPostImageUri(requestCode: Int, resultCode: Int, data: Intent?) : String? {
        postRepository.getPostImageUri(requestCode, resultCode, data)
        return postRepository.postImageUriFromDevice
    }

}