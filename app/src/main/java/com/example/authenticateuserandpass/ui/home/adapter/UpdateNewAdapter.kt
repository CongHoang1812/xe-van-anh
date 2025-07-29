package com.example.authenticateuserandpass.ui.home.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.data.firebaseModel.HotService
import com.example.authenticateuserandpass.data.firebaseModel.UpdateNews
import com.example.authenticateuserandpass.databinding.ItemHotServiceBinding
import com.example.authenticateuserandpass.databinding.ItemUpdateNewBinding

class UpdateNewAdapter : RecyclerView.Adapter<UpdateNewAdapter.ViewHolder>()  {
    private val updateNews : MutableList<UpdateNews> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUpdateNewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(updateNews[position])
    }

    override fun getItemCount(): Int {
        return updateNews.size
    }

    fun setData(list: List<UpdateNews>) {
        updateNews.clear()
        updateNews.addAll(list)
        notifyDataSetChanged()
    }



    class ViewHolder ( private val binding: ItemUpdateNewBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(updateNews: UpdateNews){
            binding.tvTitleUpdateNew.text = updateNews.Title
            binding.tvContentUpdateNew.text = updateNews.Content
            Glide.with(binding.root.context)
                .load(updateNews.ImagePath)
                .into(binding.imgUpdateNew)
            binding.root.setOnClickListener {
                val url = updateNews.Url
                if (url.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    binding.root.context.startActivity(intent)
                }

            }
        }



    }


}