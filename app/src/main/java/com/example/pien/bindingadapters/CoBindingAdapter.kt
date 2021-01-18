package com.example.pien.bindingadapters

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.pien.FavoriteFragmentDirections
import com.example.pien.R
import com.example.pien.models.Post
import com.example.pien.ui.fragments.list.ListFragmentDirections
import com.example.pien.ui.fragments.myPage.MyPageFragmentDirections

class CoBindingAdapter {

    companion object {

        @BindingAdapter("loadImage")
        @JvmStatic
        fun loadImage(imageView: ImageView, uri: MutableLiveData<String>) {
            if (!uri.value.isNullOrEmpty()) {
                Glide.with(imageView.rootView).load(uri.value).into(imageView)
            }
        }

        @BindingAdapter("android:navigateToDetailFragment")
        @JvmStatic
        fun navigateToDetailFragment(view: View, currentItem: Post) {
            view.setOnClickListener {
                if (view.findNavController().currentDestination?.id == R.id.listFragment) {
                    val action = ListFragmentDirections.actionListFragmentToDetailFragment(currentItem)
                    view.findNavController().navigate(action)

                } else if (view.findNavController().currentDestination?.id == R.id.myPageFragment) {
                    val action = MyPageFragmentDirections.actionMyPageFragmentToDetailFragment(currentItem)
                    view.findNavController().navigate(action)

                } else if (view.findNavController().currentDestination?.id == R.id.favoriteFragment) {
                    val action = FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(currentItem)
                    view.findNavController().navigate(action)

                }
            }
        }
    }
}