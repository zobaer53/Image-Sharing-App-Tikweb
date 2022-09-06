package com.zobaer53.imagesharingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zobaer53.imagesharingapp.R
import com.zobaer53.imagesharingapp.model.MyModel

class MyAdapter(val context:Context, private val mList: List<MyModel> ) : RecyclerView.Adapter<com.zobaer53.imagesharingapp.adapter.MyAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]
        Glide.with(context)
            .load(ItemsViewModel.apartmentImage)
            .fitCenter()
            .into(holder.imageView)



    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.apartmentImage)

    }
}
