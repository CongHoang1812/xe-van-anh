package com.example.authenticateuserandpass.ui.home.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.data.firebaseModel.HotService
import com.example.authenticateuserandpass.databinding.ItemHotServiceBinding
import androidx.core.net.toUri

class HotServiceAdapter : RecyclerView.Adapter<HotServiceAdapter.ViewHolder>()  {
    private val hotServices : MutableList<HotService> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemHotServiceBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(hotServices[position])
    }

    override fun getItemCount(): Int {
        return hotServices.size
    }

    fun setData(list: List<HotService>) {
        hotServices.clear()
        hotServices.addAll(list)
        notifyDataSetChanged()
    }



    class ViewHolder ( private val binding: ItemHotServiceBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(hotService: HotService){
            binding.tvTitleHotService.text = hotService.Title
            binding.tvContentHotService.text = hotService.Content
            Glide.with(binding.root.context)
                .load(hotService.ImagePath)
                .into(binding.imgHotService)
            Log.d("HotServiceBind", "Binding item : ${hotService.Title}, ${hotService.Content}, ${hotService.ImagePath}, ${hotService.Url}")
            binding.root.setOnClickListener {
                val url = hotService.Url
                if (url.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    binding.root.context.startActivity(intent)
                }

            }
        }



    }


}