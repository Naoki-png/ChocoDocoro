package com.example.pien.data.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Post(
    val userName: String,
    val userPhotoUri: String,
    val postPhotoUri: String,
    val postMessage: String,
    val timestamp: Date
)