package com.example.pien.util

//fireStore collection references
const val POST_REF = "posts_collection"
const val FAVORITES_REF = "favorites_collection"
const val EACH_USER_FAVORITES_REF = "each_user_favorites_collection"

//fireStore document keys
//(posts)
const val DOCUMENTID = "documentId"
const val USERID = "userId"
const val USERNAME = "userName"
const val USERIMAGE = "userImage"
const val POSTIMAGE = "postImage"
const val TIMESTAMP = "timeStamp"
const val BRAND_NAME = "brandName"
const val CHEAP_OR_LUXURY = "cheapOrLuxury"
//(favorites)
const val POSTID = "postId"

//request code
const val REQUEST_SIGN_IN_WITH_GOOGLE = 0
const val REQUEST_GET_POST_IMAGE = 1
const val REQUEST_GET_USER_IMAGE = 2

//preference name
const val SIGN_IN_METHOD = "SignInMethod"
const val CURRENT_TAB = "currentTab"
//preference key
const val METHOD = "method"
const val TAB = "tab"

//tab
const val CHEAP = "cheap"
const val LUXURY = "luxury"
