package com.example.pien.repository

import android.content.Context
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.util.METHOD
import com.example.pien.util.SIGN_IN_METHOD
import com.example.pien.util.SignInMethod
import com.example.pien.util.State
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dataStoreRepository: DataStoreRepository,
    private val googleSignInClient: GoogleSignInClient,
    private val loginManager: LoginManager
) {
    /**
     * ログインチェックメソッド
     */
    suspend fun loginCheck() = flow<State<SignInMethod>> {
        emit(State.loading())
        when (determineSignInMethod()) {
            SignInMethod.GOOGLE -> {
                Log.d(this@LoginRepository::class.java.name, "name: ${firebaseAuth.currentUser!!.displayName}, uid: ${firebaseAuth.currentUser!!.uid}")
                emit(State.success(SignInMethod.GOOGLE))
            }
            SignInMethod.FACEBOOK -> {
                Log.d("LoginRepository", "name: ${firebaseAuth.currentUser!!.displayName}, uid: ${firebaseAuth.currentUser!!.uid}")
                emit(State.success(SignInMethod.FACEBOOK))
            }
            SignInMethod.LOGOUT -> {
                Log.d("LoginRepository", "currently there is no login user")
                emit(State.failed("SignInMethodDataStore has value of LOGOUT"))
            }
        }
    }.catch { exception ->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * googleアカウントでfirebaseへログインするメソッド
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

    /**
     * facebookアカウントでfirebaseへログインするメソッド
     */
    fun firebaseAuthWithFacebook(accessToken: AccessToken) = flow<State<State.StateConst>> {
        emit(State.loading())
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential).await()
        dataStoreRepository.saveSignInMethod(SignInMethod.FACEBOOK.name)
        emit(State.success(State.StateConst.SUCCESS))
    }.catch { exception->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * サインアウト
     */
    suspend fun signOut() = flow<State<State.StateConst>> {
        emit(State.loading())
        when (determineSignInMethod()) {
            SignInMethod.GOOGLE -> {
                googleSignOut().collect { currentState ->
                    when (currentState) {
                        is State.Loading -> {}
                        is State.Success -> {
                            dataStoreRepository.saveSignInMethod(SignInMethod.LOGOUT.name)
                            emit(State.success(State.StateConst.SUCCESS))
                        }
                        is State.Failed -> { Log.e("ListFragment", currentState.message) }
                    }
                }
            }
            SignInMethod.FACEBOOK -> {
                facebookSignOut().collect { currentState ->
                    when (currentState) {
                        is State.Loading -> {}
                        is State.Success -> {
                            dataStoreRepository.saveSignInMethod(SignInMethod.LOGOUT.name)
                            emit(State.success(State.StateConst.SUCCESS))
                        }
                        is State.Failed -> { Log.e("ListFragment", currentState.message) }
                    }
                }
            }
            SignInMethod.LOGOUT -> {
                emit(State.failed("SignInMethodDataStore has value of LOGOUT"))
                Log.e("ListFragment", "SignInMethodDataStore has value of LOGOUT")
            }
        }
    }.catch { exception->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Google Logout メソッド
     */
    private suspend fun googleSignOut() = flow<State<State.StateConst>> {
        emit(State.loading())
        firebaseAuth.signOut()
        googleSignInClient.signOut().await()
        emit(State.success(State.StateConst.SUCCESS))
    }.catch { exception->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Facebook Logout メソッド
     */
    private suspend fun facebookSignOut() = flow<State<State.StateConst>> {
        emit(State.loading())
        firebaseAuth.signOut()
        loginManager.logOut()
        emit(State.success(State.StateConst.SUCCESS))
    }.catch { exception->
        emit(State.failed(exception.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * 現在のSignInMethodを特定する
     */
    suspend fun determineSignInMethod(): SignInMethod {
        return dataStoreRepository
            .readSignInMethod
            .first()
    }
}