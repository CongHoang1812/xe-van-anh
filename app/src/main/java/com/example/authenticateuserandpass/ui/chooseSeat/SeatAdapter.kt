package com.example.authenticateuserandpass.ui.chooseSeat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.FragmentHomeBinding
import com.example.authenticateuserandpass.databinding.ItemSeatBinding
import com.example.authenticateuserandpass.R


class SeatAdapter(
    private val onSeatClick: (selectedIds: String, count: Int) -> Unit
) : RecyclerView.Adapter<SeatAdapter.ViewHolder>() {
    private val bookings = mutableListOf<Booking>()
    private var selectedIds = mutableListOf<String>()
    private var count = 0

    fun setBookings(list: List<Booking>) {
        bookings.apply {
            clear()
            addAll(list)
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemSeatBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking, pos: Int) {
            // Nếu empty → ẩn cả icon và text
            if (booking.status == "empty") {
                binding.imgSeat.setColorFilter(Color.TRANSPARENT)
                binding.tvSeatId.visibility = View.INVISIBLE
                return
            }

            // Hiển thị seat_id
            binding.tvSeatId.text = booking.seat_id
            binding.tvSeatId.visibility = View.VISIBLE

            // Load icon (vector) và tint theo status
            binding.imgSeat.setImageResource(R.drawable.baseline_chair_24)
            val srcRes  = when (booking.status) {
                "available" -> R.drawable.baseline_chair_24
                "booked"    -> R.drawable.baseline_chair_booked_24
                "selected"  -> R.drawable.baseline_chair_selected_24
                else        -> R.color.bg
            }
            binding.imgSeat.setImageResource(
                srcRes
            )

            // Click để chọn / bỏ chọn
            binding.root.setOnClickListener {
                when (booking.status) {
                    "available" -> booking.status = "selected"
                    "selected"  -> booking.status = "available"
                }
                notifyItemChanged(pos)

                // Tính lại danh sách đã chọn
                val selectedList = bookings
                    .filter { it.status == "selected" }
                    .map { it.seat_id }
                onSeatClick(selectedList.joinToString(","), selectedList.size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSeatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bookings[position], position)
    }

    override fun getItemCount(): Int = bookings.size
}