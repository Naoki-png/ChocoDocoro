package com.example.pien

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(
                TwitterAuthConfig(
                    getString(R.string.twitter_consumer_key),
                    getString(R.string.twitter_consumer_secret)
                )
            )
            .debug(true)
            .build()
        Twitter.initialize(config)

        setContentView(R.layout.activity_main)

        //setup navcontroller with bottom navigation
        val navController = findNavController(R.id.nav_host_fragment)
        setupWithNavController(bottomNavigationView, navController)

        clearPostPreference()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Activityに戻ってくる結果を、LoginFragmentのonActivityResult()に渡す。
        // （そして、LoginFragmentのonActivityResult()では、twitterLoginButtonのonActivityResult()に渡す。）
        val childFragments = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.childFragmentManager.fragments
            childFragments.forEach {fragment ->
                fragment?.onActivityResult(requestCode, resultCode, data)
            }
    }


    private fun clearPostPreference() {
        val prefEditor = getSharedPreferences("post_pref", Context.MODE_PRIVATE).edit()
        prefEditor.putString("postImage", "").apply()
        prefEditor.putString("postMessage", "").apply()
    }
}