package com.example.pien.data.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Post(
    val userId: String? = null,
    val userName: String? = null,
    val userImage: String? = null,
    val postImage: String? = null,
    val productName: String? = null,
    val brandName: String? = null,
    val productPrice: String? = null,
    val productType: String? = null,
    val postMessage: String? = null,
    @ServerTimestamp
    val timeStamp: Date? = null
)