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
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_post.view.*
import java.util.*
import kotlin.collections.HashMap

class PostRepository {
    private val logTag = javaClass.name
    private val appContext = MyApplication.appContext
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val postDatabaseRef = FirebaseFirestore.getInstance().collection(POST_REF)
    private val userDatabaseRef = FirebaseFirestore.getInstance().collection(USERS_REF)
    var postImageUriFromDevice: String? = null

    var homeListData = MutableLiveData<List<Post>>()
    var mypageListData = MutableLiveData<List<Post>>()

    /**
     * 投稿したい画像のUriをセット
     */
    fun getPostImageUri(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_GET_IMAGE) {
            Log.e(logTag, "requestCode is inaccurate")
        } else if (resultCode != Activity.RESULT_OK) {
            Log.e(logTag, "getting image result is not OK")
        } else if (data == null) {
            Log.e(logTag, "image result is null")
        } else {
            postImageUriFromDevice = data.data.toString()
        }
    }

    /**
     * DBに投稿を保存する
     */
    fun post(message: String, currentDisplayPhotoUri: String) {
        val userName = currentUser?.displayName.toString()
        val userPhotoUri = currentUser?.photoUrl.toString()
        val newPost = HashMap<String, Any>()
        newPost.put(USERNAME, userName)
        newPost.put(USERIMAGE, userPhotoUri)
        newPost.put(POSTIMAGE, currentDisplayPhotoUri)
        newPost.put(POSTMESSAGE, message)
        newPost.put(TIMESTAMP, FieldValue.serverTimestamp())
        val document = postDatabaseRef.document()
        document.set(newPost)
            .addOnSuccessListener {
                Log.d(logTag, "new post succeeded")
                if (!TextUtils.isEmpty(currentDisplayPhotoUri)) {
                    storeImage(document.id, Uri.parse(currentDisplayPhotoUri))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(logTag, "new post failed: ${exception.localizedMessage}")
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
                val userName = data?.get(USERNAME) as String
                val userImageUri = data?.get(USERIMAGE) as String
                val postImageUri = data?.get(POSTIMAGE) as String
                val postMsg = data?.get(POSTMESSAGE) as String
                val timestamp = data?.get(TIMESTAMP) as Timestamp
                val post = Post(
                    userName = userName,
                    userPhotoUri = userImageUri,
                    postPhotoUri = postImageUri,
                    postMessage = postMsg,
                    timestamp = timestamp.toDate()
                )
                listData.add(post)
            }
        }
        return listData
    }
}