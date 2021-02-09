package com.example.pien.ui.fragments.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.login.BaseLoginCallback
import com.example.pien.login.LoginRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.view.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(), BaseLoginCallback.LoginListener {
    @Inject lateinit var loginRepository: LoginRepository

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
        view.login_facebook_login_btn.registerCallback(
            loginRepository.facebookCallbackManger,
            loginRepository.myFaceBookCallback
        )

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            loginRepository.signIn(requestCode, resultCode, data)
        }
    }

    override fun onLoginCompleted() {
        findNavController().navigate(R.id.action_loginFragment_to_listFragment)
    }

    override fun onStart() {
        super.onStart()
        loginRepository.myFaceBookCallback.registerLoginListener(this)
    }

    override fun onStop() {
        super.onStop()
        loginRepository.myFaceBookCallback.unregisterLoginListener()
    }
}