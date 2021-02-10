package com.example.pien.ui.fragments.myPage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.pien.viewmodels.MyPageViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountDialog: DialogFragment() {
    private val myPageViewModel: MyPageViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Delete Account")
            .setMessage("Are you sure to delete your account? \r" +
                    "Once you've done never restore your account")
            .setPositiveButton("Yes") { dialog, id ->
                // delete account, postdb(by userId), favoritedb(by documentId is userId) and firestorage
                myPageViewModel.deleteAccountCompletely()
            }
            .setNegativeButton("Cancel") { dialog, id ->

            }
        return builder.create()
    }
}