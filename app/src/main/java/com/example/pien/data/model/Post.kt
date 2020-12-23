package com.example.pien.data.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Post(
    val userId: String,
    val userName: String,
    val userPhotoUri: String,
    val postPhotoUri: String,
    val chocolateName: String,
    val chocolateBrand: String,
    val chocolatePrice: String,
    val chocolateType: String,
    val chocolateDescription: String,
    val timestamp: Date
) {
    enum class ChcolateType {
        MILK,
        DARK,
        HIGHCACAO,
        WHITE
    }
}