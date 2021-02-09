package com.example.pien.ui.fragments.myPage

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pien.viewmodels.MainViewModel
import com.example.pien.R
import com.example.pien.databinding.FragmentMyPageBinding
import com.example.pien.ui.fragments.list.ListAdapter
import com.example.pien.util.State
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyPageFragment : Fragment() {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val listAdapter : ListAdapter by lazy { ListAdapter() }
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMyPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        mainViewModel.myPosts.observe(viewLifecycleOwner, { posts ->
            listAdapter.setData(posts)
        })
        mainViewModel.state.observe(viewLifecycleOwner, { currentState ->
            when (currentState) {
                State.StateConst.LOADING -> {
                    binding.mypageList.showShimmer()
                }
                State.StateConst.SUCCESS -> {
                    binding.mypageList.hideShimmer()
                }
                State.StateConst.FAILED -> {
                    binding.mypageList.hideShimmer()
                }
                else -> {
                    //error 処理
                }
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setRecyclerView() {
        binding.mypageList.adapter = listAdapter
        binding.mypageList.layoutManager = LinearLayoutManager(requireContext())
        binding.mypageList.showShimmer()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.mypage_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_account) {
            DeleteAccountDialog().show(parentFragmentManager, "")
        }
        return super.onOptionsItemSelected(item)
    }
}