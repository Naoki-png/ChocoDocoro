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
import com.example.pien.fragments.list.HomeListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*

class MyPageFragment : Fragment() {

    private var firebaseUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var logTag: String

    private val listAdapter : HomeListAdapter by lazy { HomeListAdapter() }
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTag = javaClass.name
        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)
        if ("null" == firebaseUser!!.photoUrl.toString()) {
            view.profile_Image.setImageResource(R.drawable.ic_baseline_account_circle_24)
        } else {
            Glide.with(this).load(firebaseUser!!.photoUrl).into(view.profile_Image)
        }
        view.profile_name.text = firebaseUser!!.displayName
        val list = view.mypage_list
        list.adapter = listAdapter
        list.layoutManager = LinearLayoutManager(requireContext())
        mainViewModel.observeFields()
        mainViewModel.setMypageData()
        mainViewModel.myPosts.observe(requireActivity(), Observer { posts ->
            listAdapter.setHomeData(posts)
        })
        view.edit_prof_btn.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_editAccountFragment)
        }
        return view
    }
}