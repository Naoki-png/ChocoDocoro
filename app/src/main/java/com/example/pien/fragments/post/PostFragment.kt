package com.example.pien.fragments.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pien.MainViewModel
import com.example.pien.R
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_post.*
import kotlinx.android.synthetic.main.fragment_post.view.*

class PostFragment : Fragment() {
    private lateinit var logTag: String
    private var currentDisplayPhotoUri: String = ""
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTag = javaClass.name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_post, container, false)
        view.addPhoto_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_GET_IMAGE)
        }

        view.deletePhoto_btn.setOnClickListener {
            currentDisplayPhotoUri = ""
            postImage.visibility = View.GONE
            it.visibility = View.GONE
        }
        setHasOptionsMenu(true)
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = mainViewModel.getPostImageUri(requestCode, resultCode, data)
        Glide.with(this).load(currentDisplayPhotoUri).centerCrop().into(postImage)
        postImage.visibility = View.VISIBLE
        deletePhoto_btn.visibility = View.VISIBLE
        uri?.let { uri ->
            currentDisplayPhotoUri = uri
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.post) {

            if (!TextUtils.isEmpty(postMessage_et.text.toString())) {
                val message = postMessage_et.text.toString()
                mainViewModel.post(message, currentDisplayPhotoUri)
                hideKeyboard(requireActivity())
                findNavController().navigate(R.id.listFragment)
            } else {
                makeToast(requireContext(), "message space must not be empty")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}