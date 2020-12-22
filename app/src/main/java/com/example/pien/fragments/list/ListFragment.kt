package com.example.pien.fragments.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pien.MainViewModel
import com.example.pien.R
import com.example.pien.data.model.SignInMethod
import com.example.pien.util.SIGNIN_METHOD
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.twitter.sdk.android.core.SessionManager
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlin.math.sign

class ListFragment : Fragment() {
    private lateinit var logTag: String

    private val homeListAdapter: HomeListAdapter by lazy { HomeListAdapter() }
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mLoginManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTag = javaClass.name
        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser

        //googleAPIクライアントの取得
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        //facebookのログインマネージャ
        mLoginManager = LoginManager.getInstance()

        loginCheck()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        setHasOptionsMenu(true)
        val list = view.findViewById<RecyclerView>(R.id.home_list)
        list.adapter = homeListAdapter
        list.layoutManager = LinearLayoutManager(requireContext())
        mainViewModel.observeFields()
        mainViewModel.setHomeData()
        mainViewModel.posts.observe(requireActivity(), Observer { posts ->
            homeListAdapter.setHomeData(posts)
        })
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> {
                val prefs = requireContext().getSharedPreferences(SIGNIN_METHOD, Context.MODE_PRIVATE)
                val signInMethod = prefs.getString("method", "logout now")
                prefs.edit().putString("method", "logout now")
                when (SignInMethod.valueOf(signInMethod!!)) {
                    SignInMethod.GOOGLE -> googleSignOut()
                    SignInMethod.FACEBOOK -> facebookSignOut()
                    SignInMethod.TWITTER -> twitterSignOut()
                    SignInMethod.EMAIL -> emailSignOut()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * ログインチェックメソッド
     */
    private fun loginCheck() {
        if (firebaseUser == null) {
            findNavController().navigate(R.id.action_listFragment_to_loginFragment)
            return
        }
        Log.d("LoginUserName", "name: ${firebaseUser?.displayName}, uid: ${firebaseUser?.uid}")
    }

    /**
     * Google Logout メソッド
     */
    private fun googleSignOut() {
        //firebaseからのサインアウト
        auth.signOut()
        //googleからのサインアウト
        mGoogleSignInClient.signOut()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_listFragment_to_loginFragment)
                }
            }
    }

    /**
     * Facebook Logout メソッド
     */
    private fun facebookSignOut() {
        //firebaseからのサインアウト
        auth.signOut()
        //facebookからのサインアウト
        mLoginManager.logOut()
        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
    }

    /**
     * Twitter Logout メソッド
     */
    private fun twitterSignOut() {
        //firebaseからのサインアウト
        auth.signOut()
        //twitterからのサインアウト
        val sessionManager: SessionManager<TwitterSession> = TwitterCore.getInstance().sessionManager
        if (sessionManager.activeSession != null) {
            sessionManager.clearActiveSession()
        }
        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
    }

    /**
     * Email Logout メソッド (Firebaseのみのログアウト)
     */
    private fun emailSignOut() {
        auth.signOut()
    }
}