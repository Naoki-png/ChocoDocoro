package com.example.pien.fragments.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pien.R
import com.example.pien.data.model.Post
import com.example.pien.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_post.view.*
import java.io.BufferedInputStream

class PostFragment : Fragment() {

    lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_post, container, false)
        view.addPhoto_btn.setOnClickListener {
            postImage()
        }
        getPostContents(view)
        setHasOptionsMenu(true)
        return view
    }

    private fun getPostContents(view: View) {
        val pref = requireContext().getSharedPreferences("post_pref", Context.MODE_PRIVATE)
        val userName = pref.getString("userName", currentUser.displayName)
        val userImage = pref.getString("userImage", currentUser.photoUrl.toString())
        val postImage = pref.getString("postImage", "")
        val postMessage = pref.getString("postMessage", "")

        Glide.with(requireContext()).load(postImage).into(view.postImage)
        view.postMessage_et.setText(postMessage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_GET_IMAGE) {
            getImageResult(resultCode, data)
        }
    }

    private fun postImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_GET_IMAGE)
    }

    private fun getImageResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            Log.e("Add Picture", "unable to add picture")
            return
        }
        if (data == null) return
        val uriFromDevice = data.data
        Glide.with(requireContext()).load(uriFromDevice).centerCrop().into(requireView().postImage)
        val prefEditor = requireContext().getSharedPreferences("post_pref", Context.MODE_PRIVATE).edit()
        prefEditor.putString("postImage", uriFromDevice.toString()).apply()
        Log.d("uri", "${uriFromDevice.toString()}")
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