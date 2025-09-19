package com.example.authenticateuserandpass.ui.pickdroplocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.placeSuggestion.PlaceSuggestion

class PlacesAdapter(private val onClick: (PlaceSuggestion) -> Unit) :
    ListAdapter<PlaceSuggestion, PlacesAdapter.PlaceViewHolder>(DiffCallback()) {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView92)
    }

    class DiffCallback : DiffUtil.ItemCallback<PlaceSuggestion>() {
        override fun areItemsTheSame(old: PlaceSuggestion, new: PlaceSuggestion) = old == new
        override fun areContentsTheSame(old: PlaceSuggestion, new: PlaceSuggestion) = old == new
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)
        holder.textView.text = place.address
        holder.itemView.setOnClickListener { onClick(place) }
    }
}