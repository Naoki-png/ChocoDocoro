package com.example.pien.ui.fragments.editAccount

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pien.viewmodels.MainViewModel
import com.example.pien.MyApplication
import com.example.pien.R
import com.example.pien.databinding.FragmentEditAccountBinding
import com.example.pien.util.REQUEST_GET_POST_IMAGE
import com.example.pien.util.REQUEST_GET_USER_IMAGE
import com.example.pien.util.makeToast
import com.example.pien.viewmodels.EditAccountViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_edit_account.*
import kotlinx.android.synthetic.main.fragment_edit_account.view.*

class EditAccountFragment : Fragment() {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val mainViewModel: MainViewModel by activityViewModels()
    private val editAccountViewModel: EditAccountViewModel by viewModels()
    lateinit var binding: FragmentEditAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        binding.editAccountViewModel = editAccountViewModel
        binding.lifecycleOwner = this

        binding.editUserName.setText(currentUser.displayName)
        editAccountViewModel.setCurrentDisplayPhotoUri(currentUser.photoUrl.toString())

        binding.editUserImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_GET_USER_IMAGE)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = getImageUriFromDevice(requestCode, resultCode, data)
        editAccountViewModel.setCurrentDisplayPhotoUri(uri)
    }

    /**
     * 画像のUriをゲットする
     */
    private fun getImageUriFromDevice(requestCode: Int, resultCode: Int, data: Intent?): String {
        if (resultCode != Activity.RESULT_OK ||
            requestCode != REQUEST_GET_USER_IMAGE ||
            data == null) {
            Log.e("EditAccountFragment", "User image couldn't be loaded.")
            return ""
        } else {
            return data.data.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_account_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            if (!TextUtils.isEmpty(binding.editUserName.text)) {
                mainViewModel.editUserInfo(binding.editUserName.text.toString(), editAccountViewModel.currentDisplayPhotoUri.value.toString())
                findNavController().navigate(R.id.action_editAccountFragment_to_myPageFragment)
            } else {
                makeToast(MyApplication.appContext, "user name can't be empty")
            }
        }
        return super.onOptionsItemSelected(item)
    }

}