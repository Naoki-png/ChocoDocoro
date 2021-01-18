package com.example.pien.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class Favorite(
    val postId: String? = null,
    @ServerTimestamp
    val timeStamp: Date? = null
)