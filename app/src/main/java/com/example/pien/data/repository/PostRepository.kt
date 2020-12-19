package com.example.pien.data.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.pien.MyApplication
import com.example.pien.data.model.Post
import com.example.pien.util.*
import com.google.firebase.firestore.FirebaseFirestore

class PostRepository {
    private val logTag = javaClass.name
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
}