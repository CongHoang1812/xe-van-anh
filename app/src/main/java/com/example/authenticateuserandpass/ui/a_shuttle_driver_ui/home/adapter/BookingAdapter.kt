package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ItemCustomerBinding

class BookingAdapter(
    private var bookings: List<Pair<Booking, User>>,
    private val onDirectionClick: (Booking, User) -> Unit,
    private val onCallClick: (User) -> Unit,
    private val onPickedUpClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val (booking, user) = bookings[position]
        holder.binding.apply {
            tvCustomerName.text = user.name
            tvPhone.text = user.phone
            tvAddress.text = booking.pickup_location

            btnDirection.setOnClickListener { onDirectionClick(booking, user) }
            btnCall.setOnClickListener { onCallClick(user) }
            btnPickedUp.setOnClickListener { onPickedUpClick(booking) }
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<Pair<Booking, User>>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}