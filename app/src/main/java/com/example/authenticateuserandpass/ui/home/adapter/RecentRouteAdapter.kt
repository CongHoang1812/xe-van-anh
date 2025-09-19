package com.example.authenticateuserandpass.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.RecentRoute

class RecentRouteAdapter(private val routes: List<RecentRoute>) :
    RecyclerView.Adapter<RecentRouteAdapter.RecentRouteViewHolder>() {

    class RecentRouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date_recent)
        val tvOrigin: TextView = itemView.findViewById(R.id.tv_origin_recent)
        val tvDestination: TextView = itemView.findViewById(R.id.tv_destination_recent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentRouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_search, parent, false)
        return RecentRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentRouteViewHolder, position: Int) {
        val route = routes[position]
        holder.tvDate.text = route.date
        holder.tvOrigin.text = route.origin
        holder.tvDestination.text = route.destination
    }

    override fun getItemCount(): Int = routes.size
}
