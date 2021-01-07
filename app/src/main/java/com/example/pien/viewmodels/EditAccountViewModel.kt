package com.example.pien.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditAccountViewModel: ViewModel() {
    var currentDisplayPhotoUri = MutableLiveData<String>()

    fun setCurrentDisplayPhotoUri(imageUri: String) {
        currentDisplayPhotoUri.value = imageUri
    }
}