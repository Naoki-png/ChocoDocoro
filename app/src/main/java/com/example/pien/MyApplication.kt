package com.example.pien

import android.content.Context
import androidx.multidex.MultiDexApplication

class MyApplication: MultiDexApplication() {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}