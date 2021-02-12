package com.example.pien.repository

import android.content.Context
import android.util.Log
import com.example.pien.util.METHOD
import com.example.pien.util.SIGN_IN_METHOD
import com.example.pien.util.SignInMethod
import com.example.pien.util.State
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dataStoreRepository: DataStoreRepository
) {

    /**
     * googleアカウントでfirebaseへログインするメソッド
     * 認証成功後、auth.currentUserが更新される
     * @param account ユーザーのgoogleアカ
     */
    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount) = flow<State<State.StateConst>> {
        emit(State.loading())
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
        dataStoreRepository.saveSignInMethod(SignInMethod.GOOGLE.name)
        emit(State.success(State.StateConst.SUCCESS))
    }.catch { exception->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)
}