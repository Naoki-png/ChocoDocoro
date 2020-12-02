package com.example.pien.fragments.createUser

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pien.R
import com.example.pien.util.makeToast
import kotlinx.android.synthetic.main.fragment_create_user.view.*

class CreateUserFragment : Fragment() {

    private var userName: String? = null
    private var userEmail: String? = null
    private var userPassword: String? = null
    private var passwordAgain: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_user, container, false)
        userName = view.userName_et.text.toString()
        userEmail = view.email_et.text.toString()
        userPassword = view.password_et.text.toString()
        passwordAgain = view.checkPassword_et.text.toString()

        view.register_btn.setOnClickListener {
            if (userPassword.equals(passwordAgain)) {
                registerNewUser(userName, userEmail, userPassword)
            } else {
                makeToast(requireContext(), getString(R.string.checkPassword))
            }
        }
        return view
    }

    private fun registerNewUser(userName: String?, userEmail: String?, userPassword: String?) {

    }
}