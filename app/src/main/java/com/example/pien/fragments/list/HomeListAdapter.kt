package com.example.pien.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pien.MyApplication
import com.example.pien.R
import com.example.pien.data.model.Post

class HomeListAdapter: RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder>() {
    private val appContext = MyApplication.appContext
    /**
     * Home画面に表示するデータリスト
     */
    private var homeListData = emptyList<Post>()

    class HomeListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val userName = view.findViewById<TextView>(R.id.user_name)
        val userImage = view.findViewById<ImageView>(R.id.user_image)
        val postImage = view.findViewById<ImageView>(R.id.post_image)
        val postMsg = view.findViewById<TextView>(R.id.post_msg)
        val likeBtn = view.findViewById<ImageView>(R.id.like_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_card, parent, false)
        return HomeListViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
        holder.userName.text = homeListData[position].userName
        Glide.with(appContext).load(homeListData[position].userPhotoUri).into(holder.userImage)
        Glide.with(appContext).load(homeListData[position].postPhotoUri).into(holder.postImage)
        holder.postMsg.text = homeListData[position].postMessage
    }

    override fun getItemCount() = homeListData.size

    fun setHomeData(listData: List<Post>) {
        homeListData = listData
        notifyDataSetChanged()
    }
}