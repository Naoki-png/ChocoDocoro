package com.example.pien.data.repository

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.pien.data.model.Post
import com.example.pien.data.model.State
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await


class PostRepository {
    private val logTag = javaClass.name
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val postDatabaseRef = FirebaseFirestore.getInstance().collection(POST_REF)

    var postImageUriFromDevice: String? = null
    var userImageUriFromDevice: String? = null

    /**
     * 画像のUriをゲットする
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
     * ユーザープロフィール変更
     */
    fun editUserInfo(userName: String, userImage: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .setPhotoUri(Uri.parse(userImage))
            .build()
        currentUser.updateProfile(changeRequest)
            .addOnSuccessListener {
                Log.d(logTag, "updating profile is completed")
                updateProfileInDB(userName, userImage)
            }
            .addOnFailureListener { exception ->
                Log.e(logTag, "updating profile is failed: ${exception.localizedMessage}")
            }
    }

    /**
     * DB内のプロフィール情報を変更
     */
    private fun updateProfileInDB(userName: String, userImage: String) {
        postDatabaseRef.whereEqualTo(USERID, currentUser.uid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(logTag, "Couldn't get data for updating: ${exception.localizedMessage}")
                }

                if (snapshot != null) {
                    for (document in snapshot) {
                        postDatabaseRef.document(document.id)
                            .update(USERNAME, userName, USERIMAGE, userImage)
                            .addOnFailureListener { exception ->
                                Log.e(logTag, "Couldn't update profile data in DB: ${exception.localizedMessage}")
                            }
                    }
                }
            }
    }

    /**
     * Home表示用の全件データ取得
     */
    fun getAllPosts() = flow<State<List<Post>>> {

        // Emit loading state
        emit(State.loading())

        val snapshot = postDatabaseRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING).get().await()
        // parse data to java object
        val posts: List<Post> = snapshot.toObjects(Post::class.java)

        // Emit success state with data
        emit(State.success(posts))

    }.catch { exception ->
        // If exception is thrown, emit failed state along with message.
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * MyPage表示用のデータ取得
     */
    fun getMyPosts() = flow<State<List<Post>>> {

        // Emit loading state
        emit(State.loading())

        val snapshot = postDatabaseRef
            .whereEqualTo(USERID, currentUser.uid)
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .get()
            .await()
        // parse data to java object
        val posts: List<Post> = snapshot.toObjects(Post::class.java)

        // Emit success state with data
        emit(State.success(posts))

    }.catch { exception ->
        // If exception is thrown, emit failed state along with message.
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * 投稿する
     */
    fun addPost(post: Post) = flow<State<List<Post>>> {

        // Emit loading state
        emit(State.loading())

        val docRef = postDatabaseRef.document()
        docRef.set(post).await()
        storeImage(docRef.id, Uri.parse(post.postImage))

        val postsSnapshot = postDatabaseRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING).get().await()
        val posts: List<Post> = postsSnapshot.toObjects(Post::class.java)
        emit(State.success(posts))

    }.catch { exception ->
        // If exception is thrown, emit failed state along with message.
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * storageに画像を保存する
     */
    private suspend fun storeImage(documentId: String, photoUri: Uri?) {
        val storageRef = FirebaseStorage.getInstance().getReference(currentUser.uid)
            .child(documentId).child(photoUri?.lastPathSegment!!)

        storageRef.putFile(photoUri).await()
        val downloadUrl: Uri = storageRef.downloadUrl.await()
        postDatabaseRef.document(documentId).update(POSTIMAGE, downloadUrl.toString()).await()
    }
}