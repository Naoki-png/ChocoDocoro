package com.example.pien.ui.fragments.myPage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DeleteAccountDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Delete Account")
            .setMessage("Are you sure to delete your account? \r" +
                    "Once you've done never restore your account")
            .setPositiveButton("Yes") { dialog, id ->
                // delete account, postdb and favoritedb
            }
            .setNegativeButton("Cancel") { dialog, id ->

            }
        return builder.create()
    }
}