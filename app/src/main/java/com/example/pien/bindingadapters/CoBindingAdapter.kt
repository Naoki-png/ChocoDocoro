package com.example.pien.bindingadapters

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
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
        fun navigateToDetailFragment(view: ConstraintLayout, currentItem: Post) {
            view.setOnClickListener {
                val action : NavDirections
                if (view.findNavController().currentDestination?.id == R.id.listFragment) {
                    action = ListFragmentDirections.actionListFragmentToDetailFragment(currentItem)
                } else {
                    action = MyPageFragmentDirections.actionMyPageFragmentToDetailFragment(currentItem)
                }
                view.findNavController().navigate(action)
            }
        }
    }
}