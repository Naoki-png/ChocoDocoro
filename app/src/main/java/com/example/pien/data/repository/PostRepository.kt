package com.example.pien.data.repository

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.pien.MyApplication
import com.example.pien.data.model.Post
import com.example.pien.util.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_post.view.*
import java.util.*
import kotlin.collections.HashMap

class PostRepository {
    private val logTag = javaClass.name
    private val appContext = MyApplication.appContext
    private lateinit var currentUser: FirebaseUser
    private val postDatabaseRef = FirebaseFirestore.getInstance().collection(POST_REF)
    private val userDatabaseRef = FirebaseFirestore.getInstance().collection(USERS_REF)
    var postImageUriFromDevice: String? = null
    var userImageUriFromDevice: String? = null

    var homeListData = MutableLiveData<List<Post>>()
    var mypageListData = MutableLiveData<List<Post>>()

    /**
     * 画像のUriをセット
     */
    fun getImageUriFromDevice(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != Activity.RESULT_OK) {
            Log.e(logTag, "getting image result is not OK")
        } else if (data == null) {
            Log.e(logTag, "image result is null")
        } else {
            when (requestCode) {
                REQUEST_GET_POST_IMAGE -> {
                    postImageUriFromDevice = data.data.toString()
                }
                REQUEST_GET_USER_IMAGE -> {
                    userImageUriFromDevice = data.data.toString()
                }
                else -> {
                    Log.e(logTag, "requestCode is inaccurate")
                }
            }
        }
    }



    /**
     * storageに画像を保存する
     */
    private fun storeImage(documentId: String, photoUri: Uri?) {
        val storageRef = FirebaseStorage.getInstance().getReference(currentUser!!.uid)
            .child(documentId).child(photoUri?.lastPathSegment!!)

        storageRef.putFile(photoUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                Log.e(logTag, "Couldn't store file into storage")
            }
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(logTag, "Couldn't store file into storage")
                return@addOnCompleteListener
            }
            postDatabaseRef.document(documentId).update(POSTIMAGE, task.result.toString())
                .addOnSuccessListener {
                    makeToast(appContext, "posted!")
                    Log.e(logTag, "Couldn't store file into storage")
                }
                .addOnFailureListener { exception ->
                    makeToast(appContext, "error!")
                    Log.e(logTag, "Couldn't store file into storage: ${exception.localizedMessage}")
                }
        }
    }

    /**
     * home画面表示用のデータリストを取ってくる
     */
    fun setHomeData() {
        postDatabaseRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(logTag, "Couldn't get posts data: ${exception.localizedMessage}")
            }
            val postDocuments: List<DocumentSnapshot>? = snapshot?.documents
            homeListData.value = parseDataToPost(postDocuments)
        }
    }

    /**
     * データからPostインスタンスのリストにする
     */
    private fun parseDataToPost(postDocuments: List<DocumentSnapshot>?) : List<Post> {
        val listData = mutableListOf<Post>()
        if (postDocuments != null) {
            for (document in postDocuments) {
                val data = document.getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val userId = data?.get(USERID) as String
                val userName = data?.get(USERNAME) as String
                val userImageUri = data?.get(USERIMAGE) as String
                val postImageUri = data?.get(POSTIMAGE) as String
                val chocolateName = data?.get(PRODUCTNAME) as String
                val chocolateBrand = data?.get(BRANDNAME) as String
                val chocolatePrice = data?.get(PRODUCTPRICE) as String
                val chocolateType = data?.get(PRODUCTTYPE) as String
                val postMsg = data?.get(POSTMESSAGE) as String
                val timestamp = data?.get(TIMESTAMP) as Timestamp
                val post = Post(
                    userId = userId,
                    userName = userName,
                    userPhotoUri = userImageUri,
                    postPhotoUri = postImageUri,
                    chocolateName = chocolateName,
                    chocolateBrand = chocolateBrand,
                    chocolatePrice = chocolatePrice,
                    chocolateType = chocolateType,
                    chocolateDescription = postMsg,
                    timestamp = timestamp.toDate()
                )
                listData.add(post)
            }
        }
        return listData
    }

    /**
     * mypage画面表示用のデータリストを取ってくる
     */
    fun setMypageData() {
        currentUser = FirebaseAuth.getInstance().currentUser!!
        postDatabaseRef
            .whereEqualTo(USERID, currentUser!!.uid)
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(logTag, "Couldn't get posts data: ${exception.localizedMessage}")
                }
                val postDocuments: List<DocumentSnapshot>? = snapshot?.documents
                mypageListData.value = parseDataToPost(postDocuments)
            }
    }

    fun editUserInfo(userName: String, userImage: String) {

    }

    fun post(
        postImage: String,
        productName: String,
        brandName: String,
        productPrice: String,
        productType: String,
        postMsg: String
    ) {
        currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = currentUser?.uid
        val userName = currentUser?.displayName.toString()
        val userPhotoUri = currentUser?.photoUrl.toString()
        val newPost = HashMap<String, Any>()
        newPost.put(USERID, userId)
        newPost.put(USERNAME, userName)
        newPost.put(USERIMAGE, userPhotoUri)
        newPost.put(POSTIMAGE, postImage)
        newPost.put(PRODUCTNAME, productName)
        newPost.put(BRANDNAME, brandName)
        newPost.put(PRODUCTPRICE, productPrice)
        newPost.put(PRODUCTTYPE, productType)
        newPost.put(POSTMESSAGE, postMsg)
        newPost.put(TIMESTAMP, FieldValue.serverTimestamp())
        val document = postDatabaseRef.document()
        document.set(newPost)
            .addOnSuccessListener {
                Log.d(logTag, "new post succeeded")
                storeImage(document.id, Uri.parse(postImage))
            }
            .addOnFailureListener { exception ->
                Log.e(logTag, "new post failed: ${exception.localizedMessage}")
            }
    }
}