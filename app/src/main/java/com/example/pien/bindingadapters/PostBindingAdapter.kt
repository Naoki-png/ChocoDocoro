package com.example.pien.bindingadapters

import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.pien.R

class PostBindingAdapter {

    companion object {

        @BindingAdapter("changeViewVisibility")
        @JvmStatic
        fun changeViewVisibility(view: View, imageUri: MutableLiveData<String>) {
            if (!TextUtils.isEmpty(imageUri.value)) {
                when (view.id) {
                    R.id.post_add_photo_btn -> view.visibility = View.GONE
                    R.id.post_product_image -> view.visibility = View.VISIBLE
                    R.id.deletePhoto_btn -> view.visibility = View.VISIBLE
                }
            } else {
                when (view.id) {
                    R.id.post_add_photo_btn -> view.visibility = View.VISIBLE
                    R.id.post_product_image -> view.visibility = View.GONE
                    R.id.deletePhoto_btn -> view.visibility = View.GONE
                }
            }
        }

        @BindingAdapter("resetCurrentDisplayPhotoUri")
        @JvmStatic
        fun resetCurrentDisplayPhotoUri(imageView: ImageView, currentDisplayPhotoUri: MutableLiveData<String>) {
            imageView.setOnClickListener {
                currentDisplayPhotoUri.value = ""
            }
        }
    }
}