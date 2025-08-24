package com.example.authenticateuserandpass.ui.a_admin_ui.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.databinding.ItemRouteAdminBinding

class RouteAdapter(
    private var routes: MutableList<Route>,
    private val onEditClick: (Route) -> Unit,
    private val onDeleteClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(private val binding: ItemRouteAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(route: Route, onEditClick: (Route) -> Unit, onDeleteClick: (Route) -> Unit) {
            binding.apply {
                tvStartPoint.text = route.origin
                tvEndPoint.text = route.destination
                tvDistance.text = route.distance
                tvDuration.text = route.duration
                tvTripsPerDay.text = route.tripsPerDay.toString()

                btnEdit.setOnClickListener { onEditClick(route) }
                btnDelete.setOnClickListener { onDeleteClick(route) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position], onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = routes.size

    fun updateRoutes(newRoutes: List<Route>) {
        routes.clear()
        routes.addAll(newRoutes)
        notifyDataSetChanged()
    }
}