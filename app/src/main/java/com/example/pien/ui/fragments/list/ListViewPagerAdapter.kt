package com.example.pien.ui.fragments.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pien.R
import com.example.pien.models.Post
import com.example.pien.util.*
import com.todkars.shimmer.ShimmerRecyclerView

class ListViewPagerAdapter :
    RecyclerView.Adapter<ListViewPagerAdapter.ViewHolder>()
{
    private val listAdapter: ListAdapter by lazy { ListAdapter() }
    private lateinit var recyclerView: ShimmerRecyclerView

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerView = view.findViewById<ShimmerRecyclerView>(R.id.list_recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tabView = LayoutInflater.from(parent.context).inflate(R.layout.tab_layout, parent, false)
        return ViewHolder(tabView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        recyclerView = holder.recyclerView

        holder.recyclerView.apply {
            adapter = listAdapter
            layoutManager = GridLayoutManager(holder.itemView.context, 2)
        }
    }

    override fun getItemCount() = 2

    fun setRecyclerViewListData(list: List<Post>) {
        listAdapter.setData(list)
    }
}