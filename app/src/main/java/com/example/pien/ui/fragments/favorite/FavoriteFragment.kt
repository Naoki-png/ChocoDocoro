package com.example.pien.ui.fragments.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pien.databinding.FragmentFavoriteBinding
import com.example.pien.ui.fragments.list.ListAdapter
import com.example.pien.util.State
import com.example.pien.viewmodels.MainViewModel
import com.todkars.shimmer.ShimmerRecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class FavoriteFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this
        setRecyclerView(binding.favoriteRecyclerView)

        mainViewModel.state.observe(viewLifecycleOwner, { currentState ->
            when (currentState) {
                State.StateConst.LOADING -> {
                    binding.favoriteRecyclerView.showShimmer()
                }
                State.StateConst.SUCCESS -> {
                    binding.favoriteRecyclerView.hideShimmer()
                }
                State.StateConst.FAILED -> {
                    binding.favoriteRecyclerView.hideShimmer()
                }
                else -> {
                    //error 処理
                }
            }
        })
        mainViewModel.favoritePosts.observe(viewLifecycleOwner, { favoritePosts ->
            adapter.setData(favoritePosts)
        })
        mainViewModel.getFavorite()

        return binding.root
    }


    private fun setRecyclerView(recyclerView: ShimmerRecyclerView) {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.showShimmer()
    }
}