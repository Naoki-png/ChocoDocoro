package com.example.pien.bindingadapters

import android.view.View
import androidx.databinding.BindingAdapter
import com.example.pien.models.Post
import com.google.android.gms.common.util.CollectionUtils

class FavoriteBindingAdapter {
    companion object {
        @BindingAdapter("changeViewVisibility")
        @JvmStatic
        fun changeViewVisibility(view: View, favoritePosts: List<Post>?) {
            if (favoritePosts.isNullOrEmpty()) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }
}