package com.example.pien.repository

import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*


class PostRepository {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val postCollectionRef: CollectionReference by lazy { FirebaseFirestore.getInstance().collection(POST_REF) }
    private val favoriteCollectionRef: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection(FAVORITES_REF).document(currentUser.uid).collection(EACH_USER_FAVORITES_REF)
    }
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
        val userImageStorageRef = storageRef.child("userImage").child(Date().toString())
        userImageStorageRef.putFile(Uri.parse(userImage)).await()

        val snapshot = postCollectionRef.whereEqualTo(USERID, currentUser.uid).get().await()
        val downloadUrl: Uri = userImageStorageRef.downloadUrl.await()
        for (document in snapshot) {
            postCollectionRef.document(document.id).update(USERIMAGE, downloadUrl.toString()).await()
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
            val post: List<Post> = postSnapshot.toObjects(Post::class.java)
            postList.add(post[0])
        }
        return postList
    }
}