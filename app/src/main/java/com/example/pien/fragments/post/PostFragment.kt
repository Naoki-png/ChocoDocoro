package com.example.pien.fragments.post

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pien.R
import com.example.pien.util.REQUEST_GET_IMAGE
import com.example.pien.util.makeToast
import kotlinx.android.synthetic.main.fragment_post.view.*
import java.io.BufferedInputStream

class PostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_post, container, false)
        view.addPhoto_btn.setOnClickListener {
            postImage()
        }
        return view
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
        val inputStream = requireContext().contentResolver.openInputStream(uriFromDevice!!)
        val bitmap = BitmapFactory.decodeStream(BufferedInputStream(inputStream))

        requireView().postImage.setImageBitmap(bitmap)
    }



}