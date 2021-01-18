package com.example.pien.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData

class MyPageBindingAdapter {

    companion object {
        @BindingAdapter("displayUserName")
        @JvmStatic
        fun displayUserName(textView: TextView, userName: MutableLiveData<String>) {
            textView.text = userName.value
        }
    }
}