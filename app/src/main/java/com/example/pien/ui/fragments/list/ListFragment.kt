package com.example.pien.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.pien.R
import com.example.pien.login.BaseLoginCallback
import com.example.pien.login.LoginRepository
import com.example.pien.util.*
import com.example.pien.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ListFragment : Fragment(), SearchView.OnQueryTextListener, BaseLoginCallback.LogoutListener {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewPagerAdapter: ListViewPagerAdapter by lazy { ListViewPagerAdapter() }
    private  var firebaseUser: FirebaseUser? = null

    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser = firebaseAuth.currentUser
        loginCheck(firebaseUser)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> {
                lifecycleScope.launch() {
                    loginRepository.signOut()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * ログインチェックメソッド
     */
    private fun loginCheck(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            findNavController().navigate(R.id.action_listFragment_to_loginFragment)
            return
        }
        Log.d("LoginUserName", "name: ${this.firebaseUser?.displayName}, uid: ${this.firebaseUser?.uid}")
    }

    override fun onLogoutCompleted() {
        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
    }

    override fun onStart() {
        super.onStart()
        loginRepository.myFaceBookCallback.registerLogoutListener(this)
    }

    override fun onStop() {
        super.onStop()
        loginRepository.myFaceBookCallback.unregisterLogoutListener()
    }
}