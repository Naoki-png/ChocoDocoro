package com.example.pien.ui.fragments.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.login.LoginRepository
import com.example.pien.login.SignInMethod
import com.example.pien.util.METHOD
import com.example.pien.util.SIGNIN_METHOD
import com.example.pien.util.makeToast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var loginRepository: LoginRepository

    lateinit var mFacebookCallbackManager: CallbackManager

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
            loginRepository.requestGoogleSignIn(this)
        }

        view.login_facebook_login_btn.setReadPermissions("email", "public_profile")
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

        lifecycleScope.launch {
            val signInResult = loginRepository.signIn(requestCode, resultCode, data)
            if (signInResult) {
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            } else {
                makeToast(this@LoginFragment.requireContext(), "Login Failed")
            }
        }

        // Pass the activity result back to the Facebook SDK
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                val prefs = requireContext().getSharedPreferences(SIGNIN_METHOD, Context.MODE_PRIVATE)
                prefs.edit().putString(METHOD, SignInMethod.FACEBOOK.name).apply()
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener {exception ->
                Log.e("Facebook Login", "firebase auth with facebook login failed: ${exception.localizedMessage}")
            }
    }

    private fun firebaseAuthWithTwitter(session: TwitterSession) {
        val credential = TwitterAuthProvider.getCredential(
            session.authToken.token,
            session.authToken.secret
        )
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                val prefs = requireContext().getSharedPreferences(SIGNIN_METHOD, Context.MODE_PRIVATE)
                prefs.edit().putString(METHOD, SignInMethod.TWITTER.name).apply()
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener { exception ->
                Log.e("Twitter Login", "firebase auth with twitter login failed: ${exception.localizedMessage}")
            }
    }
}