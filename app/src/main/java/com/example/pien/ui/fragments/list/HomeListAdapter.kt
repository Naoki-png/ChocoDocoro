package com.example.pien.ui.fragments.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pien.MyApplication
import com.example.pien.R
import com.example.pien.models.Post
import com.example.pien.databinding.PostRowBinding

class HomeListAdapter: RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder>() {
    private val appContext = MyApplication.appContext
    private var homeListData = emptyList<Post>()

    class HomeListViewHolder(private val binding: PostRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val userImage = binding.rowUserImage
        val postImage = binding.rowProductImage

        fun bind(post: Post) {
            binding.post = post
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PostRowBinding.inflate(layoutInflater, parent, false)
        return HomeListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
        holder.bind(homeListData[position])
        if ("null" == homeListData[position].userImage) {
            holder.userImage.setImageResource(R.drawable.ic_baseline_account_circle_24)
        } else {
            Glide.with(appContext).load(homeListData[position].userImage).into(holder.userImage)
        }
        Glide.with(appContext).load(homeListData[position].postImage).into(holder.postImage)
    }

    override fun getItemCount() = homeListData.size

    fun setHomeData(listData: List<Post>) {
        homeListData = listData
        notifyDataSetChanged()
    }
}