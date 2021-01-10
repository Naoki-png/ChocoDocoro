package com.example.pien.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pien.viewmodels.MainViewModel
import com.example.pien.R
import com.example.pien.databinding.FragmentListBinding
import com.example.pien.util.*
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.todkars.shimmer.ShimmerRecyclerView
import com.twitter.sdk.android.core.SessionManager
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession

class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private val homeListAdapter: HomeListAdapter by lazy { HomeListAdapter() }
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentListBinding

    private lateinit var auth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mLoginManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        mLoginManager = LoginManager.getInstance()

        loginCheck()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setRecyclerView(binding.listList)

        mainViewModel.getAllPosts()
        mainViewModel.state.observe(requireActivity(), { currentState ->
            when (State.StateConst.valueOf(currentState)) {
                State.StateConst.LOADING -> {
                    binding.listList.showShimmer()
                }
                State.StateConst.SUCCESS -> {
                    binding.listList.hideShimmer()
                }
                State.StateConst.FAILED -> {
                    binding.listList.hideShimmer()
                }
            }
        })
        mainViewModel.posts.observe(requireActivity(), { posts ->
            homeListAdapter.setHomeData(posts)
        })

    }

    private fun setRecyclerView(list: ShimmerRecyclerView) {
        list.adapter = homeListAdapter
        list.layoutManager = GridLayoutManager(requireContext(), 2)
        list.showShimmer()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val searchView = menu.findItem(R.id.search).actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.queryHint = getString(R.string.search_hint)
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> {
                val prefs = requireContext().getSharedPreferences(SIGNIN_METHOD, Context.MODE_PRIVATE)
                val signInMethod = prefs.getString(METHOD, "logout now")
                prefs.edit().putString(METHOD, "logout now")
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
        auth.signOut()
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
        auth.signOut()
        mLoginManager.logOut()
        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
    }

    /**
     * Twitter Logout メソッド
     */
    private fun twitterSignOut() {
        auth.signOut()
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
        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            searchDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    private fun searchDatabase(query: String) {
        mainViewModel.getSearchedPosts(query)
        mainViewModel.searchedPosts.observe(viewLifecycleOwner, { searchedPosts ->
            homeListAdapter.setHomeData(searchedPosts)
        })
    }
}