package com.example.pien.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PostViewModel: ViewModel() {
    var currentDisplayPhotoUri = MutableLiveData<String>()

    fun setCurrentDisplayPhotoUri(uri: String) {
        if (uri.isNotEmpty()) {
            currentDisplayPhotoUri.value = uri
        }
    }
}