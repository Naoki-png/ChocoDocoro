package com.example.pien.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.pien.R
import com.example.pien.util.*
import com.example.pien.viewmodels.MainViewModel
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewPagerAdapter: ListViewPagerAdapter by lazy { ListViewPagerAdapter() }

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
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.VISIBLE

        mainViewModel.cheapPosts.observe(viewLifecycleOwner, { posts ->
            viewPagerAdapter.setRecyclerViewListData(posts)
        })
        mainViewModel.luxuryPosts.observe(viewLifecycleOwner, { posts ->
            viewPagerAdapter.setRecyclerViewListData(posts)
        })

        val view = inflater.inflate(R.layout.fragment_list, container, false)
        val viewPager2 = view.findViewById<ViewPager2>(R.id.list_viewPager)
        viewPager2.adapter = viewPagerAdapter
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val tabLayout = view.findViewById<TabLayout>(R.id.list_tabLayout)
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    "cheap" -> {
                        mainViewModel.getCheapPosts()
                        val prefs = requireContext().getSharedPreferences(CURRENT_TAB, Context.MODE_PRIVATE)
                        prefs.edit().putString(TAB, CHEAP).apply()
                    }
                    "luxury" -> {
                        mainViewModel.getLuxuryPosts()
                        val prefs = requireContext().getSharedPreferences(CURRENT_TAB, Context.MODE_PRIVATE)
                        prefs.edit().putString(TAB, LUXURY).apply()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        TabLayoutMediator(
            tabLayout,
            viewPager2
        ) { tab, position ->
            when (position) {
                0 -> tab.text = "cheap"
                1 -> tab.text = "luxury"
            }
        }.attach()

        setHasOptionsMenu(true)
        return view
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
                val prefs = requireContext().getSharedPreferences(
                    SIGNIN_METHOD,
                    Context.MODE_PRIVATE
                )
                val signInMethod = prefs.getString(METHOD, "logout now")
                prefs.edit().putString(METHOD, "logout now").apply()
                when (SignInMethod.valueOf(signInMethod!!)) {
                    SignInMethod.GOOGLE -> googleSignOut()
                    SignInMethod.FACEBOOK -> facebookSignOut()
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
        val prefs = requireContext().getSharedPreferences(CURRENT_TAB, Context.MODE_PRIVATE)
        val currentTab = prefs.getString(TAB, null)
        mainViewModel.searchedPosts.observe(viewLifecycleOwner, { searchedPosts ->
            viewPagerAdapter.setRecyclerViewListData(searchedPosts)
        })
        mainViewModel.getSearchedPosts(query, currentTab)
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


}