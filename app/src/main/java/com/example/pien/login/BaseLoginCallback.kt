package com.example.pien.login

open class BaseLoginCallback {

    interface LoginListener {
        fun onLoginCompleted()
    }

    interface LogoutListener {
        fun onLogoutCompleted()
    }

    protected var loginListener: LoginListener? = null
    protected var logoutListener: LogoutListener? = null

    fun registerLoginListener(loginListener: LoginListener) {
        this.loginListener = loginListener
    }

    fun unregisterLoginListener() {
        loginListener = null
    }

    fun registerLogoutListener(logoutListener: LogoutListener) {
        this.logoutListener = logoutListener
    }

    fun unregisterLogoutListener() {
        logoutListener = null
    }
}