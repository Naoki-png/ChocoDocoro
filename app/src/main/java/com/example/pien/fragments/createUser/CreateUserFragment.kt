package com.example.pien.fragments.createUser

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
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_create_user.view.*

class CreateUserFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_user, container, false)

        val userName = view.userName_et.text.toString()
        val userEmail = view.email_et.text.toString()
        val userPassword = view.password_et.text.toString()
        val passwordAgain = view.checkPassword_et.text.toString()

        view.register_btn.setOnClickListener {
            if (userPassword.equals(passwordAgain)) {
                registerNewUser(userName, userEmail, userPassword)
            } else {
                makeToast(requireContext(), getString(R.string.checkPassword))
            }
        }
        return view
    }

    /**
     * アカウント作成メソッド
     * @param userName 名前
     * @param userEmail email
     * @param userPassword パスワード
     */
    private fun registerNewUser(userName: String, userEmail: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("CREATE USER", "createUserWithEmailAndPassword: succeed")
                    currentUser = auth.currentUser!!

                    //move to login
                    findNavController().navigate(R.id.action_createUserFragment_to_loginFragment)

                } else {
                    Log.w("CREATE USER", "createUserWithEmailAndPassword: failed", task.exception)
                    makeToast(requireContext(), "create user failed.")
                    clearTextFields()
                }
            }
    }

    /**
     * アカウント作成失敗時に、EditTextをクリア
     */
    private fun clearTextFields() {
        requireView().userName_et.setText("")
        requireView().email_et.setText("")
        requireView().userName_et.setText("")
        requireView().userName_et.setText("")
    }
}