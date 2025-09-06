package com.example.authenticateuserandpass.ui.a_admin_ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ItemTripBinding
import java.text.NumberFormat
import java.util.*

class TripAdapter(
    private var trips: MutableList<Trip> = mutableListOf(),
    private val onEditClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit,
    private val onItemClick: (Trip) -> Unit = {}
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    inner class TripViewHolder(private val binding: ItemTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Trip) {
            binding.apply {
                // Thời gian khởi hành
                tvTime.text = "Khởi hành: ${trip.departure_time}"

                // Tên tài xế
                tvDriverName.text = if (trip.main_driver_name.isNullOrEmpty()) {
                    "Tài xế: Chưa phân công"
                } else {
                    "Tài xế: ${trip.main_driver_name}"
                }

                // Giá vé - format theo định dạng VND

                tvPrice.text = "${trip.ticket_price}.000 vnđ"

                // Số ghế còn lại
                tvSeats.text = "Còn lại: ${trip.availableSeats} chỗ"

                // Click listeners
                root.setOnClickListener { onItemClick(trip) }
                btnEdit.setOnClickListener { onEditClick(trip) }
                btnDelete.setOnClickListener { onDeleteClick(trip) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount() = trips.size

    fun updateTrips(newTrips: List<Trip>) {
        val diffCallback = TripDiffCallback(trips, newTrips)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        trips.clear()
        trips.addAll(newTrips)
        diffResult.dispatchUpdatesTo(this)
    }

    private class TripDiffCallback(
        private val oldList: List<Trip>,
        private val newList: List<Trip>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}