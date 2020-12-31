package com.example.pien.util

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.example.pien.R
import com.example.pien.data.model.Post
import com.example.pien.fragments.list.ListFragmentDirections
import com.example.pien.fragments.myPage.MyPageFragmentDirections

class BindingAdapter {

    companion object {
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