package com.example.pien.fragments.myPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pien.R
import kotlinx.android.synthetic.main.fragment_my_page.view.*

class MyPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my_page, container, false)

        view.edit_prof_btn.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_editAccountFragment)
        }
        return view
    }
}