package com.example.pien.ui.fragments.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.util.*
import com.example.pien.viewmodels.LoginViewModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject lateinit var googleSignInClient: GoogleSignInClient
    lateinit var mFacebookCallbackManager: CallbackManager

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    lifecycleScope.launch {
                        loginViewModel.firebaseAuthWithGoogle(account!!).collect { currentState ->
                            when (currentState) {
                                is State.Loading -> {
                                    //処理なし
                                }
                                is State.Success -> {
                                    findNavController().navigate(R.id.action_loginFragment_to_listFragment)
                                }
                                is State.Failed -> {
                                    //todo
                                }
                            }

                        }
                    }
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
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_SIGN_IN_WITH_GOOGLE)
    }

    /**
     * facebookアカウントでfirebaseへログインするメソッド
     * 認証成功後、auth.currentUserが更新される
     * @param accessToken facebookのアクセストークン
     */
    private fun firebaseAuthWithFacebook(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener {
                val prefs = requireContext().getSharedPreferences(SIGN_IN_METHOD, Context.MODE_PRIVATE)
                prefs.edit().putString(METHOD, SignInMethod.FACEBOOK.name).apply()
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener {exception ->
                Log.e("Facebook Login", "firebase auth with facebook login failed: ${exception.localizedMessage}")
            }
    }

}