package com.example.pien.ui.fragments.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.util.SignInMethod
import com.example.pien.util.METHOD
import com.example.pien.util.REQUEST_SIGN_IN_WITH_GOOGLE
import com.example.pien.util.SIGNIN_METHOD
import com.example.pien.util.makeToast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.view.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mFacebookCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        mFacebookCallbackManager = CallbackManager.Factory.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.INVISIBLE

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.login_google_login_btn.setOnClickListener {
            googleSignIn()
        }

        view.login_facebook_login_btn.setReadPermissions("email", "public_profile")
        //fragment内で使う場合
        view.login_facebook_login_btn.fragment = this
        view.login_facebook_login_btn.registerCallback(mFacebookCallbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("Facebook Login", "facebook:onSuccess:$loginResult")
                firebaseAuthWithFacebook(loginResult.accessToken!!)
            }
            override fun onCancel() {
                Log.d("Facebook Login", "facebook:onCancel")
            }
            override fun onError(error: FacebookException) {
                Log.d("Facebook Login", "facebook:onError", error)
            }
        })


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN_WITH_GOOGLE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (result.isSuccess) {
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account!!)
                } else {
                    makeToast(requireContext(), "google login failed")
                    return
                }
            }
        }

        // Pass the activity result back to the Facebook SDK
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * GoogleSignInApiにリクエストを投げるメソッド
     */
    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_SIGN_IN_WITH_GOOGLE)
    }

    /**
     * googleアカウントでfirebaseへログインするメソッド
     * 認証成功後、auth.currentUserが更新される
     * @param account ユーザーのgoogleアカ
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val prefs = requireContext().getSharedPreferences(SIGNIN_METHOD, Context.MODE_PRIVATE)
                prefs.edit().putString(METHOD, SignInMethod.GOOGLE.name).apply()
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener {exception ->
                Log.e("LoginFragment", "firebase auth woth google login failed: ${exception.localizedMessage}")
            }
    }

    /**
     * facebookアカウントでfirebaseへログインするメソッド
     * 認証成功後、auth.currentUserが更新される
     * @param accessToken facebookのアクセストークン
     */
    private fun firebaseAuthWithFacebook(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val prefs = requireContext().getSharedPreferences(SIGNIN_METHOD, Context.MODE_PRIVATE)
                prefs.edit().putString(METHOD, SignInMethod.FACEBOOK.name).apply()
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener {exception ->
                Log.e("Facebook Login", "firebase auth with facebook login failed: ${exception.localizedMessage}")
            }
    }

}