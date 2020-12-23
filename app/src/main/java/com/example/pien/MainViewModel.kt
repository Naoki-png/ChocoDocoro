package com.example.pien

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pien.data.model.Post
import com.example.pien.data.repository.PostRepository
import com.example.pien.util.REQUEST_GET_POST_IMAGE
import com.example.pien.util.REQUEST_GET_USER_IMAGE

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


    fun setHomeData() {
        postRepository.setHomeData()
    }

    fun observeFields() {
        postRepository.homeListData.observeForever { postList ->
            posts.value = postList
        }
        postRepository.mypageListData.observeForever { postList ->
            myPosts.value = postList
        }
    }

    fun setMypageData() {
        postRepository.setMypageData()
    }

    fun editUserInfo(userName: String, userImage: String) {
        postRepository.editUserInfo(userName, userImage)
    }

    /**
     * 投稿する
     */
    fun post(
        postImage: String,
        productName: String,
        brandName: String,
        productPrice: String,
        productType: String,
        postMsg: String
    ) {
        postRepository.post(postImage, productName, brandName, productPrice, productType, postMsg)
    }
}