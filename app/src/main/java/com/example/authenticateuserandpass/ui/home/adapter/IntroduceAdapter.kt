package com.example.authenticateuserandpass.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.authenticateuserandpass.data.firebaseModel.SliderModel
import com.example.authenticateuserandpass.data.firebaseModel.SliderModel2
import com.example.authenticateuserandpass.databinding.ItemBanner2Binding
import com.example.authenticateuserandpass.databinding.SliderItemContainerBinding

class IntroduceAdapter(
    private val items: List<SliderModel2>,
) : RecyclerView.Adapter<IntroduceAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemBanner2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.binding.textTitle.text = item.title
        holder.binding.textDesc.text = item.desc
        Glide.with(holder.itemView.context)
            .load(item.url)
            .into(holder.binding.imageIcon)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    class ViewHolder(val binding: ItemBanner2Binding) :
        RecyclerView.ViewHolder(binding.root)




}