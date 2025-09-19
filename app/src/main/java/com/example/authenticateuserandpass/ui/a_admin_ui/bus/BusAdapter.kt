package com.example.authenticateuserandpass.ui.a_admin_ui.bus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.bus.Bus

class BusAdapter(
    private val buses: List<Bus>,
    private val listener: OnBusMenuClickListener
) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    interface OnBusMenuClickListener {
        fun onView(bus: Bus)
        fun onEdit(bus: Bus)
        fun onDelete(bus: Bus)
    }

    inner class BusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBusType: TextView = itemView.findViewById(R.id.tvBusType)
        val tvSeatCount: TextView = itemView.findViewById(R.id.tvSeatCount)
        val tvLicensePlate: TextView = itemView.findViewById(R.id.tvLicensePlate)
        val btnBusMenu: ImageButton = itemView.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]
        holder.tvBusType.text = "Loại xe: ${bus.type}"
        holder.tvSeatCount.text = "Số ghế: ${bus.seat_count}"
        holder.tvLicensePlate.text = "Biển số: ${bus.license_plate}"

        holder.btnBusMenu.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnBusMenu)
            popup.inflate(R.menu.bus_menu) // menu resource
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_view -> listener.onView(bus)
                    R.id.action_edit -> listener.onEdit(bus)
                    R.id.action_delete -> listener.onDelete(bus)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = buses.size
}