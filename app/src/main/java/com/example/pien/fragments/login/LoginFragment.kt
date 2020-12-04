package com.example.pien.fragments.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.util.makeToast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*


class LoginFragment : Fragment() {

    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.login_btn.setOnClickListener {
            loginWithEmailAndPassword()
        }

        view.createUser_btn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createUserFragment)
        }
        return view
    }

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