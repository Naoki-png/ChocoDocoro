package com.example.pien.ui.fragments.post

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pien.viewmodels.MainViewModel
import com.example.pien.R
import com.example.pien.databinding.FragmentPostBinding
import com.example.pien.models.Post
import com.example.pien.util.*
import com.example.pien.viewmodels.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_post.*
import kotlinx.android.synthetic.main.fragment_post.view.*
import java.util.*

class PostFragment : Fragment() {
    private val currentUser: FirebaseUser by lazy { FirebaseAuth.getInstance().currentUser!! }
    private val mainViewModel: MainViewModel by activityViewModels()
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var binding: FragmentPostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        binding.postViewModel = postViewModel
        binding.lifecycleOwner = this

        binding.postAddPhotoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_GET_POST_IMAGE)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = getImageUriFromDevice(requestCode, resultCode, data)
        postViewModel.setCurrentDisplayPhotoUri(uri)
    }

    /**
     * 画像のUriをゲットする
     */
    private fun getImageUriFromDevice(requestCode: Int, resultCode: Int, data: Intent?): String {
        if (resultCode != Activity.RESULT_OK ||
            requestCode != REQUEST_GET_POST_IMAGE ||
            data == null) {
            Log.e("PostFragment", "Post image couldn't be loaded.")
            return ""
        } else {
            return data.data.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.post) {

            if (validateInput(requireView())) {
                mainViewModel.addPost(
                    Post(
                    userId = currentUser.uid,
                    userName = currentUser.displayName,
                    userImage = currentUser.photoUrl.toString(),
                    postImage = postViewModel.currentDisplayPhotoUri.value,
                    productName = post_product_name_et.text.toString(),
                    brandName = post_brand_name_et.text.toString(),
                    productPrice = post_product_price_et.text.toString(),
                    productType = post_product_type_spinner.selectedItem.toString(),
                    postMessage = post_product_description.text.toString(),
                    timeStamp = Date()
                )
                )

                hideKeyboard(requireActivity())
                findNavController().navigate(R.id.listFragment)
            } else {
                makeToast(requireContext(), "All entries must be filled out")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 入力項目のvalidation。一つでも空ならfalse
     */
    private fun validateInput(view: View): Boolean {
        if (
            TextUtils.isEmpty(postViewModel.currentDisplayPhotoUri.value)
            || TextUtils.isEmpty(view.post_product_name_et.text.toString())
            || TextUtils.isEmpty(view.post_brand_name_et.text.toString())
            || TextUtils.isEmpty(view.post_product_price_et.text.toString())
            || TextUtils.isEmpty(view.post_product_type_spinner.selectedItem.toString())
            || TextUtils.isEmpty(view.post_product_description.text.toString())
        ) {
            return false
        }
        return true
    }
}