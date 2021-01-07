package com.example.pien.ui.fragments.myPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.pien.viewmodels.MainViewModel
import com.example.pien.R
import com.example.pien.databinding.FragmentMyPageBinding
import com.example.pien.ui.fragments.list.HomeListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_my_page.view.*

class MyPageFragment : Fragment() {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val listAdapter : HomeListAdapter by lazy { HomeListAdapter() }
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentMyPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPageBinding.inflate(layoutInflater, container, false)
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this

        currentUser.photoUrl?.let { uri ->
            mainViewModel.setUserProfileImage(uri.toString())
        }
        mainViewModel.setUserProfileName(currentUser.displayName)
        binding.mypageEditBtn.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_editAccountFragment)
        }
        setRecyclerView()

        mainViewModel.getMyPosts()
        mainViewModel.myPosts.observe(requireActivity(), { posts ->
            listAdapter.setHomeData(posts)
        })

        return binding.root
    }

    private fun setRecyclerView() {
        binding.mypageList.adapter = listAdapter
        binding.mypageList.layoutManager = LinearLayoutManager(requireContext())
    }
}