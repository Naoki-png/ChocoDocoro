package com.example.pien.fragments.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.util.REQUEST_SIGN_IN
import com.example.pien.util.makeToast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*


class LoginFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        auth = FirebaseAuth.getInstance()
        //googleサインインの設定
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //googleサインインAPIのアクセス権取得
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        view.sign_in_button.setOnClickListener {
            signIn()
        }

        view.login_btn.setOnClickListener {
            loginWithEmailAndPassword()
        }

        view.createUser_btn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createUserFragment)
        }
        return view
    }

    /**
     * GoogleSignInApiにリクエストを投げるメソッド
     */
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (!result.isSuccess) {
                    makeToast(requireContext(), "google login failed")
                    return
                }
                //これがユーザーのgoogleアカ
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }
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
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener {exception ->
                Log.e("LoginFragment", "firebase auth woth google login failed: ${exception.localizedMessage}")
            }
    }

    /**
     * emailとpasswordでfirebaseへログインするメソッド
     * 認証成功後、auth.currentUserが更新される
     */
    private fun loginWithEmailAndPassword() {
        val email = login_email_et.text.toString()
        val password = login_password_et.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                findNavController().navigate(R.id.action_loginFragment_to_listFragment)
            }
            .addOnFailureListener { exception ->
                makeToast(requireContext(), "inncorrect email or password!")
                Log.e("LoginFragment", "login with email and password failed: ${exception.localizedMessage}")
            }
    }
}