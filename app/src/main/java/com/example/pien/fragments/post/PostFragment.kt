package com.example.pien.fragments.post

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pien.MainViewModel
import com.example.pien.R
import com.example.pien.util.*
import kotlinx.android.synthetic.main.fragment_post.*
import kotlinx.android.synthetic.main.fragment_post.view.*

class PostFragment : Fragment() {
    private lateinit var logTag: String
    private var currentDisplayPhotoUri: String = ""
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logTag = javaClass.name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_post, container, false)
        view.addPhoto_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_GET_POST_IMAGE)
        }

        view.deletePhoto_btn.setOnClickListener {
            currentDisplayPhotoUri = ""
            postImage.visibility = View.GONE
            it.visibility = View.GONE
            addPhoto_btn.visibility = View.VISIBLE
        }
        setHasOptionsMenu(true)
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = mainViewModel.getImageUriFromDevice(requestCode, resultCode, data)
        if (uri != null) {
            addPhoto_btn.visibility = View.GONE
            postImage.visibility = View.VISIBLE
            deletePhoto_btn.visibility = View.VISIBLE
            Glide.with(this).load(uri).centerCrop().into(postImage)
            uri?.let { uri ->
                currentDisplayPhotoUri = uri
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.post) {

            if (validateInput(requireView())) {
                mainViewModel.post(
                    currentDisplayPhotoUri,
                    product_name_et.text.toString(),
                    brand_name_et.text.toString(),
                    product_price_et.text.toString(),
                    chocolate_type_spinner.selectedItem.toString(),
                    postMessage.text.toString()
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
            TextUtils.isEmpty(currentDisplayPhotoUri)
            || TextUtils.isEmpty(view.product_name_et.text.toString())
            || TextUtils.isEmpty(view.brand_name_et.text.toString())
            || TextUtils.isEmpty(view.product_price_et.text.toString())
            || TextUtils.isEmpty(view.chocolate_type_spinner.selectedItem.toString())
            || TextUtils.isEmpty(view.postMessage.text.toString())
        ) {
            return false
        }
        return true
    }


}