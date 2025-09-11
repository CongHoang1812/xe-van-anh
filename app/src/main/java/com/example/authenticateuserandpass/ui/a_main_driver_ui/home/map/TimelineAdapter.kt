package com.example.authenticateuserandpass.ui.a_main_driver_ui.home.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.StopPoint

class TimelineAdapter(private val stops: List<StopPoint>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View, viewType: Int) : RecyclerView.ViewHolder(view) {
        val timelineView: com.github.vipulasri.timelineview.TimelineView =
            view.findViewById(R.id.time_line)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvTime: TextView = view.findViewById(R.id.tvTime)

        init {
            timelineView.initLine(viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return com.github.vipulasri.timelineview.TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val stop = stops[position]
        holder.tvLocation.text = stop.location
        holder.tvTime.text = stop.time
    }

    override fun getItemCount(): Int = stops.size
}