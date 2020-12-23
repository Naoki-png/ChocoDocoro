package com.example.pien.fragments.createUser

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_create_user.*
import kotlinx.android.synthetic.main.fragment_create_user.view.*

class CreateUserFragment : Fragment() {

    lateinit var auth: FirebaseAuth

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

        view.register_btn.setOnClickListener {
            if (password_et.text.toString() == checkPassword_et.text.toString()) {
                registerNewUser()
            } else {
                makeToast(requireContext(), getString(R.string.checkPassword))
            }
        }
        return view
    }

    /**
     * アカウント作成メソッド
     */
    private fun registerNewUser() {
        val userName = userName_et.text.toString()
        val userEmail = email_et.text.toString()
        val userPassword = password_et.text.toString()

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnSuccessListener { result ->
                val changeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build()
                result.user?.updateProfile(changeRequest)
                    ?.addOnFailureListener { exception ->
                        Log.e("CreateUserFragment", "updating user profile failed: ${exception.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("CreateUserFragment", "creating user failed: ${exception.localizedMessage}")
            }
    }
}