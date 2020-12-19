package com.example.pien.fragments.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTag = javaClass.name
        currentUser = FirebaseAuth.getInstance().currentUser!!
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
        setHasOptionsMenu(true)
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val postImageUri = mainViewModel.getPostImageUri(requestCode, resultCode, data)
        Glide.with(this).load(postImageUri).centerCrop().into(postImage)
        deletePhoto_btn.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.post) {
            sendPost()
            findNavController().navigate(R.id.listFragment)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendPost() {
        val view = requireView()
        val userName = currentUser.displayName.toString()
        val userPhotoUri = currentUser.photoUrl.toString()
        val message = view.postMessage_et.text.toString()
        val newPost = HashMap<String, Any>()
        newPost.put(USERNAME, userName)
        newPost.put(USERIMAGE, userPhotoUri)
        newPost.put(POSTIMAGE, "")
        newPost.put(POSTMESSAGE, message)
        val document = FirebaseFirestore.getInstance().collection(POST_REF).document()
        document.set(newPost)
            .addOnSuccessListener {
                Log.d("New Post", "new post succeeded")
            }
            .addOnFailureListener { exception ->
                Log.e("New Post", "new post failed: ${exception.localizedMessage}")
            }
        storeImage(document.id)
    }

    private fun storeImage(id: String) {
        val pref = requireContext().getSharedPreferences("post_pref", Context.MODE_PRIVATE)
        val imageUri = pref.getString("postImage", "")
        val storageRef = FirebaseStorage.getInstance().getReference(currentUser!!.uid)
            .child(id).child(imageUri!!)
        putImageStorage(storageRef, imageUri, id)
    }

    private fun putImageStorage(storageRef: StorageReference, imageUri: String, id: String) {
        storageRef.putFile(Uri.parse(imageUri)).continueWithTask { task ->
            if (!task.isSuccessful) {
                Log.e("firebaseStorage", "Couldn't store file into storage 1")
            }
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("firebaseStorage", "Couldn't store file into storage 2")
                return@addOnCompleteListener
            }
            FirebaseFirestore.getInstance().collection(POST_REF).document(id).update(POSTIMAGE, task.result.toString())
                .addOnSuccessListener {
                    makeToast(requireContext(), "posted!")
                }
                .addOnFailureListener { excception ->
                    makeToast(requireContext(), "error!")
                }
        }
    }


}