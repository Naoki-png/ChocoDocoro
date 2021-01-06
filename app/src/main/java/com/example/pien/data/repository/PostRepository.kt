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
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*


class PostRepository {
    private val logTag = javaClass.name
    private val currentUser: FirebaseUser? by lazy { FirebaseAuth.getInstance().currentUser }
    private val postDatabaseRef = FirebaseFirestore.getInstance().collection(POST_REF)
    private val storageRef: StorageReference? by lazy { FirebaseStorage.getInstance().getReference(currentUser!!.uid) }

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
    fun editUserInfo(userName: String, userImage: String) = flow<State<List<Post>>> {
        emit(State.loading())

        updateProfileInDB(userName, userImage)
        val snapshot = postDatabaseRef
            .whereEqualTo(USERID, currentUser!!.uid)
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .get()
            .await()
        val posts: List<Post> = snapshot.toObjects(Post::class.java)

        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .setPhotoUri(Uri.parse(posts[0].userImage))
            .build()
        currentUser!!.updateProfile(changeRequest).await()

        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * DB内のプロフィール情報を変更
     */
    private suspend fun updateProfileInDB(userName: String, userImage: String) {
        //new storage file
        val userImageStorageRef = storageRef?.child("userImage")?.child(Date().toString())
        userImageStorageRef!!.putFile(Uri.parse(userImage)).await()

        //更新対象ドキュメントの取得
        val snapshot = postDatabaseRef.whereEqualTo(USERID, currentUser!!.uid).get().await()

        //ドキュメントの更新
        val downloadUrl: Uri = userImageStorageRef.downloadUrl.await()
        for (document in snapshot) {
            postDatabaseRef.document(document.id).update(USERNAME, userName, USERIMAGE, downloadUrl.toString()).await()
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
            .whereEqualTo(USERID, currentUser!!.uid)
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
        val storageRef = FirebaseStorage.getInstance().getReference(currentUser!!.uid)
            .child(documentId).child(photoUri?.lastPathSegment!!)

        storageRef.putFile(photoUri).await()
        val downloadUrl: Uri = storageRef.downloadUrl.await()
        postDatabaseRef.document(documentId).update(POSTIMAGE, downloadUrl.toString()).await()
    }
}