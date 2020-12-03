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
     * @param userName 名前
     * @param userEmail email
     * @param userPassword パスワード
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
                        Log.e("CREATE USER", "updating user profile failed: ${exception.localizedMessage}")
                    }

                val data = HashMap<String, Any>()
                data.put(USERNAME, userName)
                data.put(DATE_CREATED, FieldValue.serverTimestamp())

                FirebaseFirestore.getInstance().collection(USERS_REF).document(result.user!!.uid)
                    .set(data)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_createUserFragment_to_loginFragment)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("CREATE USER", "putting userData into users_database failed: ${exception.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("CREATE USER", "creating user failed: ${exception.localizedMessage}")
            }
    }
}