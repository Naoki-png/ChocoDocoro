package com.example.pien.data.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.example.pien.MyApplication
import com.example.pien.data.model.Post
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_post.view.*

class PostRepository {
    private val logTag = javaClass.name
    private val appContext = MyApplication.appContext
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val postDatabaseRef = FirebaseFirestore.getInstance().collection(POST_REF)
    private val userDatabaseRef = FirebaseFirestore.getInstance().collection(USERS_REF)
    var postImageUriFromDevice: String? = null

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
        val document = postDatabaseRef.document()
        document.set(newPost)
            .addOnSuccessListener {
                Log.d(logTag, "new post succeeded")
            }
            .addOnFailureListener { exception ->
                Log.e(logTag, "new post failed: ${exception.localizedMessage}")
            }
        if (!TextUtils.isEmpty(currentDisplayPhotoUri)) {
            storeImage(document.id, Uri.parse(currentDisplayPhotoUri))
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
                Log.e(logTag, "Couldn't store file into storage 1")
            }
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(logTag, "Couldn't store file into storage 2")
                return@addOnCompleteListener
            }
            postDatabaseRef.document(documentId).update(POSTIMAGE, task.result.toString())
                .addOnSuccessListener {
                    makeToast(appContext, "posted!")
                    Log.e(logTag, "Couldn't store file into storage 1")
                }
                .addOnFailureListener { exception ->
                    makeToast(appContext, "error!")
                    Log.e(logTag, "Couldn't store file into storage 1: ${exception.localizedMessage}")
                }
        }
    }
}