package com.example.pien.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.pien.R
import com.example.pien.util.*
import com.example.pien.viewmodels.LoginViewModel
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val viewPagerAdapter: ListViewPagerAdapter by lazy { ListViewPagerAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            loginViewModel.loginCheck().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        //処理なし
                    }
                    is State.Success -> {
                        //処理なし
                    }
                    is State.Failed -> {
                        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
                    }
                }
            }
        }
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
            R.id.logout -> signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        lifecycleScope.launch {
            loginViewModel.signOut().collect { currentState ->
                when (currentState) {
                    is State.Loading -> {
                        //todo
                    }
                    is State.Success -> {
                        findNavController().navigate(R.id.action_listFragment_to_loginFragment)
                    }
                    is State.Failed -> {
                        //todo
                    }
                }
            }
        }
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
}