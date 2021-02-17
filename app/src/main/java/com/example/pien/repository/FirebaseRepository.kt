package com.example.pien.repository

import android.net.Uri
import android.util.Log
import com.example.pien.models.Favorite
import com.example.pien.models.Post
import com.example.pien.util.State
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) {
    val currentUser: FirebaseUser by lazy { firebaseAuth.currentUser!! }
    private val postCollectionRef: CollectionReference by lazy { firebaseFireStore.collection(POST_REF) }
    private val favoriteCollectionRef: CollectionReference by lazy {
        firebaseFireStore.collection(FAVORITES_REF).document(currentUser.uid).collection(EACH_USER_FAVORITES_REF)
    }
    private val storageRef: StorageReference by lazy { firebaseStorage.getReference(currentUser.uid) }
    private val userImageStorageRef: StorageReference by lazy { storageRef.child("userImage") }

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
        val snapshot = postCollectionRef
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
        //firebaseStorageは勝手に上書きするため削除の必要ない。
        userImageStorageRef.putFile(Uri.parse(userImage)).await()

        val snapshot: QuerySnapshot? = postCollectionRef.whereEqualTo(USERID, currentUser.uid).get().await()
        val downloadUrl: Uri = userImageStorageRef.downloadUrl.await()
        if (snapshot != null) {
            for (document in snapshot) {
                postCollectionRef.document(document.id).update(USERIMAGE, downloadUrl.toString()).await()
            }
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
        val snapshot = postCollectionRef.whereEqualTo(USERID, currentUser.uid).get().await()
        for (document in snapshot) {
            postCollectionRef.document(document.id).update(USERNAME, userName).await()
        }

        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()
        currentUser.updateProfile(changeRequest).await()
    }

    /**
     * Cheapデータ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getCheapPosts() = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = postCollectionRef
            .whereEqualTo(CHEAP_OR_LUXURY, "cheap")
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
        val posts: List<Post> = snapshot.toObjects(Post::class.java)
        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Luxuryデータ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getLuxuryPosts() = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = postCollectionRef
            .whereEqualTo(CHEAP_OR_LUXURY, "luxury")
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
        val posts: List<Post> = snapshot.toObjects(Post::class.java)
        emit(State.success(posts))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * 検索データ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getSearchedPosts(query: String, currentTab: String?) = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = postCollectionRef
            .whereEqualTo(CHEAP_OR_LUXURY, currentTab)
            .orderBy(BRAND_NAME)
            .startAt(query)
            .endAt(query + '\uf8ff')
            .limit(50)
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

        val snapshot = postCollectionRef
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

        val docRef = postCollectionRef.document()
        docRef.set(post).await()
        //documentIdの更新
        docRef.update(DOCUMENTID, docRef.id).await()
        storeImage(docRef.id, Uri.parse(post.postImage))

        val postsSnapshot = postCollectionRef
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
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
        postCollectionRef.document(documentId).update(POSTIMAGE, downloadUrl.toString()).await()
    }

    @ExperimentalCoroutinesApi
    fun deletePost(post: Post) = flow<State<String>> {
        emit(State.loading())

        firebaseStorage.getReferenceFromUrl(post.postImage!!).delete().await()
        postCollectionRef.document(post.documentId!!).delete().await()
        emit(State.success(State.StateConst.SUCCESS.name))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * お気に入り追加
     */
    @ExperimentalCoroutinesApi
    suspend fun addFavorite(post: Post) = flow<State<String>> {
        emit(State.loading())

        val docRef = favoriteCollectionRef.document(post.documentId!!)
        docRef.set(Favorite(
            postId = post.documentId,
            timeStamp = Date()
        )).await()
        emit(State.success(State.StateConst.SUCCESS.name))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * お気に入りから削除
     */
    @ExperimentalCoroutinesApi
    suspend fun removeFavorite(post: Post) = flow<State<String>> {
        emit(State.loading())

        val docRef = favoriteCollectionRef.document(post.documentId!!)
        docRef.delete().await()
        emit(State.success(State.StateConst.SUCCESS.name))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * お気に入りデータ取得
     */
    @ExperimentalCoroutinesApi
    suspend fun getFavorite() = flow<State<List<Post>>> {
        emit(State.loading())

        val snapshot = favoriteCollectionRef
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
        val favorites: List<Favorite> = snapshot.toObjects(Favorite::class.java)
        val favoritePostList = parseFavoriteToPost(favorites)
        emit(State.success(favoritePostList))

    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Favoriteのモデルから、Postのモデルへ
     */
    private suspend fun parseFavoriteToPost(favorites: List<Favorite>): List<Post> {
        val postList = mutableListOf<Post>()
        for (favorite in favorites) {
            val postSnapshot = postCollectionRef.whereEqualTo(DOCUMENTID, favorite.postId).get().await()
            if (postSnapshot.isEmpty) {
                val snapshot = favoriteCollectionRef.whereEqualTo(POSTID, favorite.postId).get().await()
                favoriteCollectionRef.document(snapshot.documents[0].id).delete().await()
            } else {
                val post: List<Post> = postSnapshot.toObjects(Post::class.java)
                postList.add(post[0])
            }
        }
        return postList
    }
}