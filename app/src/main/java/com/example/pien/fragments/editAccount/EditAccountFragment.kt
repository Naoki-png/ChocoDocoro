package com.example.pien.fragments.editAccount

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pien.MainViewModel
import com.example.pien.MyApplication
import com.example.pien.R
import com.example.pien.util.REQUEST_GET_USER_IMAGE
import com.example.pien.util.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_edit_account.*
import kotlinx.android.synthetic.main.fragment_edit_account.view.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*
import kotlinx.android.synthetic.main.fragment_post.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EditAccountFragment : Fragment() {

    private lateinit var currentUser: FirebaseUser

    private val mainViewModel: MainViewModel by activityViewModels()

    private var currentDisplayPhotoUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_edit_account, container, false)
        view.edit_userName.setText(currentUser.displayName)
        if (currentUser.photoUrl == null) {
            view.edit_userImage.setImageResource(R.drawable.ic_baseline_account_circle_24)
        } else {
            Glide.with(this).load(currentUser.photoUrl).into(view.edit_userImage)
            currentDisplayPhotoUri = currentUser.photoUrl.toString()
        }
        view.edit_userImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_GET_USER_IMAGE)
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = mainViewModel.getImageUriFromDevice(requestCode, resultCode, data)
        uri?.let { uri ->
            Glide.with(this).load(uri).into(edit_userImage)
            currentDisplayPhotoUri = uri
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_account_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            if (!TextUtils.isEmpty(edit_userName.text)) {
                mainViewModel.editUserInfo(edit_userName.text.toString(), currentDisplayPhotoUri)
                findNavController().navigate(R.id.action_editAccountFragment_to_myPageFragment)
            } else {
                makeToast(MyApplication.appContext, "user name can't be empty")
            }
        }
        return super.onOptionsItemSelected(item)
    }

}