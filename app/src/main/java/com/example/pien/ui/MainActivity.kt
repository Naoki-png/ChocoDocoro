package com.example.pien.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.pien.R
import com.example.pien.ui.fragments.login.LoginFragment
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Activityに戻ってくる結果を、LoginFragmentのonActivityResult()に渡す。
        // （そして、LoginFragmentのonActivityResult()では、twitterLoginButtonのonActivityResult()に渡す。）
        val childFragments = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.childFragmentManager.fragments
            childFragments.forEach { fragment ->
                when (fragment) {
                    is LoginFragment -> {
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
    }
}