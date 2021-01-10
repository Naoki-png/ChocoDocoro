package com.example.pien.repository

import android.net.Uri
import com.example.pien.models.Post
import com.example.pien.util.State
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*


class PostRepository {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val postDatabaseRef: CollectionReference by lazy { FirebaseFirestore.getInstance().collection(POST_REF) }
    private val storageRef: StorageReference by lazy { FirebaseStorage.getInstance().getReference(currentUser.uid) }

    /**
     * ユーザープロフィール変更
     */
    @ExperimentalCoroutinesApi
    suspend fun editUserInfo(userName: String, userImage: String) = flow<State<List<Post>>> {
        emit(State.loading())

        if (userImage == currentUser.photoUrl.toString()) {
            updateProfileName(userName)
        } else {
            updateProfileImage(userImage)
            updateProfileName(userName)
        }
        val snapshot = postDatabaseRef
            .whereEqualTo(USERID, currentUser.uid)
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .get()
            .await()
        val posts: List<Post> = snapshot.toObjects(Post::class.java)

        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * DB内とauthのプロフィール写真を変更
     */
    private suspend fun updateProfileImage(userImage: String) {
        val userImageStorageRef = storageRef.child("userImage").child(Date().toString())
        userImageStorageRef.putFile(Uri.parse(userImage)).await()

        val snapshot = postDatabaseRef.whereEqualTo(USERID, currentUser.uid).get().await()
        val downloadUrl: Uri = userImageStorageRef.downloadUrl.await()
        for (document in snapshot) {
            postDatabaseRef.document(document.id).update(USERIMAGE, downloadUrl.toString()).await()
        }

        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(downloadUrl)
            .build()
        currentUser.updateProfile(changeRequest).await()
    }

    /**
     * DB内とauthのプロフィール名を変更
     */
    private suspend fun updateProfileName(userName: String) {
        val snapshot = postDatabaseRef.whereEqualTo(USERID, currentUser.uid).get().await()
        for (document in snapshot) {
            postDatabaseRef.document(document.id).update(USERNAME, userName).await()
        }

        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()
        currentUser.updateProfile(changeRequest).await()
    }

    /**
     * Home表示用の全件データ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getAllPosts() = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = postDatabaseRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING).get().await()
        val posts: List<Post> = snapshot.toObjects(Post::class.java)
        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * 検索データ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getSearchedPosts(query: String) = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = postDatabaseRef
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .whereEqualTo(BRAND_NAME, query)
            .get()
            .await()
        val searchedPosts: List<Post> = snapshot.toObjects(Post::class.java)
        emit(State.success(searchedPosts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * MyPage表示用のデータ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getMyPosts() = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = postDatabaseRef
            .whereEqualTo(USERID, currentUser.uid)
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .get()
            .await()
        val posts: List<Post> = snapshot.toObjects(Post::class.java)
        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * 投稿する
     */
    @ExperimentalCoroutinesApi
    suspend fun addPost(post: Post) = flow<State<List<Post>>> {
        emit(State.loading())

        val docRef = postDatabaseRef.document()
        docRef.set(post).await()
        storeImage(docRef.id, Uri.parse(post.postImage))

        val postsSnapshot = postDatabaseRef.orderBy(TIMESTAMP, Query.Direction.DESCENDING).get().await()
        val posts: List<Post> = postsSnapshot.toObjects(Post::class.java)
        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * storageに画像を保存する
     */
    private suspend fun storeImage(documentId: String, photoUri: Uri?) {
        val newFileRef = storageRef.child(documentId).child(photoUri?.lastPathSegment!!)
        newFileRef.putFile(photoUri).await()
        val downloadUrl: Uri = newFileRef.downloadUrl.await()
        postDatabaseRef.document(documentId).update(POSTIMAGE, downloadUrl.toString()).await()
    }

}