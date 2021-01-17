package com.example.pien.ui.fragments.detail

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pien.R
import com.example.pien.databinding.FragmentDetailBinding
import com.example.pien.models.Favorite
import com.example.pien.viewmodels.MainViewModel
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@ExperimentalCoroutinesApi
class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private val args by navArgs<DetailFragmentArgs>()
    private val mainViewModel: MainViewModel by viewModels()

    private var favoriteFlag = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        binding.args = args
        Glide.with(this).load(args.post.postImage).into(binding.detailProductImage)

        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_fragment_menu, menu)
        checkIfFavorite(menu)
        mainViewModel.getFavorite()
    }

    private fun checkIfFavorite(menu: Menu) {
        mainViewModel.favoritePosts.observe(viewLifecycleOwner, { favoriteList ->
            for (favorite in favoriteList) {
                if (args.post.documentId == favorite.documentId) {
                    menu.findItem(R.id.favorite).icon.setTint(ContextCompat.getColor(requireContext(), R.color.yellow))
                    favoriteFlag = true
                }  else {
                    favoriteFlag = false
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.favorite && !favoriteFlag) {
            mainViewModel.addFavorite(Favorite(
                favoriteId = "",
                documentId = args.post.documentId,
                timeStamp = Date()
            ))
            item.icon.setTint(ContextCompat.getColor(requireContext(), R.color.yellow))
            favoriteFlag = true
        } else if (item.itemId == R.id.favorite && favoriteFlag) {
            //todo remove favorite
        }
        return super.onOptionsItemSelected(item)
    }
}