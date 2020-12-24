package com.example.pien

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.pien.data.model.Post
import com.example.pien.data.repository.PostRepository
import com.example.pien.util.REQUEST_GET_POST_IMAGE
import com.example.pien.util.REQUEST_GET_USER_IMAGE
import kotlinx.coroutines.runBlocking
import java.util.*

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

    fun observeFields() {
        postRepository.homeListData.observeForever(object : Observer<List<Post>> {
            override fun onChanged(postList: List<Post>?) {
                posts.value = postList
                postRepository.homeListData.removeObserver(this)
            }
        })

        postRepository.mypageListData.observeForever(object : Observer<List<Post>> {
            override fun onChanged(postList: List<Post>?) {
                myPosts.value = postList
                postRepository.mypageListData.removeObserver(this)
            }
        })
    }

    fun setHomeData() {
        postRepository.setHomeData()
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