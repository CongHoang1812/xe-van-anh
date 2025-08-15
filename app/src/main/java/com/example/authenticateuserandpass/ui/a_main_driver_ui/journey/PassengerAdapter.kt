package com.example.authenticateuserandpass.ui.a_main_driver_ui.journey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.firebaseModel.Passenger
import com.example.authenticateuserandpass.databinding.ItemPassengerBinding

class PassengerAdapter(
    private val passengers: MutableList<Passenger> = mutableListOf(),
    private val onCallClick: (Passenger) -> Unit,
    private val onMessageClick: (Passenger) -> Unit
) : RecyclerView.Adapter<PassengerAdapter.PassengerViewHolder>() {

    fun updatePassengers(newPassengers: List<Passenger>) {
        passengers.clear()
        passengers.addAll(newPassengers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassengerViewHolder {
        val binding = ItemPassengerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PassengerViewHolder(binding, onCallClick, onMessageClick)
    }

    override fun onBindViewHolder(holder: PassengerViewHolder, position: Int) {
        holder.bind(passengers[position])
    }

    override fun getItemCount(): Int = passengers.size

    class PassengerViewHolder(
        private val binding : ItemPassengerBinding,
        private val onCallClick: (Passenger) -> Unit,
        private val onMessageClick: (Passenger) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(passenger: Passenger) {
            // Đặt tên hành khách
            binding.tvPassengerName.text = passenger.name

            // Đặt số điện thoại
            binding.tvPhone.text = passenger.phone

            // Đặt vị trí đón và trả khách
            binding.tvPickupLocation.text = "Đón: ${passenger.pickup_location}"
            binding.tvDropoffLocation.text = "Trả: ${passenger.dropoff_location}"

            // Đặt số ghế
            binding.tvSeatNumber.text = "Ghế: ${passenger.seat_id}"

            // Đặt trạng thái đặt chỗ
            val bookingStatusText = when (passenger.booking_status) {
                "confirmed" -> "Đã xác nhận"
                "pending" -> "Chờ xác nhận"
                "cancelled" -> "Đã hủy"
                else -> passenger.booking_status
            }
            binding.tvBookingStatus.text = bookingStatusText

            // Hiển thị ghi chú nếu có
            if (passenger.note.isNotEmpty()) {
                binding.tvNote.visibility = View.VISIBLE
                binding.tvNote.text = "Ghi chú: ${passenger.note}"
            } else {
                binding.tvNote.visibility = View.GONE
            }

            // Đặt sự kiện cho nút gọi điện
            binding.btnCall.setOnClickListener {
                onCallClick(passenger)
            }

            // Đặt sự kiện cho nút nhắn tin
            binding.btnMessage.setOnClickListener {
                onMessageClick(passenger)
            }
        }
    }
}