package com.example.pien.fragments.myPage

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.pien.MainViewModel
import com.example.pien.R
import com.example.pien.data.model.State
import com.example.pien.fragments.list.HomeListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*
import kotlinx.coroutines.flow.collect

class MyPageFragment : Fragment() {

    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private lateinit var logTag: String

    private val listAdapter : HomeListAdapter by lazy { HomeListAdapter() }
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTag = javaClass.name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)
        if ("null" == currentUser.photoUrl.toString()) {
            view.profile_Image.setImageResource(R.drawable.ic_baseline_account_circle_24)
        } else {
            Glide.with(this).load(currentUser.photoUrl).into(view.profile_Image)
        }
        view.profile_name.text = currentUser.displayName
        val list = view.mypage_list
        list.adapter = listAdapter
        list.layoutManager = LinearLayoutManager(requireContext())
        mainViewModel.getMyPosts()
        mainViewModel.myPosts.observe(requireActivity(), Observer { posts ->
            listAdapter.setHomeData(posts)
        })
        view.edit_prof_btn.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_editAccountFragment)
        }
        return view
    }

}