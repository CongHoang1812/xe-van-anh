package com.example.authenticateuserandpass.ui.a_admin_ui.ticket

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.source.remote.TripDetails
import com.example.authenticateuserandpass.databinding.ItemAdminTicketBinding
import com.google.android.material.chip.Chip

class TicketAdapter(
    private val onEditClick: (TripDetails) -> Unit,
    private val onDeleteClick: (TripDetails) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    private var tickets = listOf<TripDetails>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTickets: List<TripDetails>) {
        tickets = newTickets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemAdminTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    override fun getItemCount() = tickets.size

    inner class TicketViewHolder(
        private val binding: ItemAdminTicketBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tripDetails: TripDetails) {
            with(binding) {
                tvBookingId.text = "Mã vé: ${tripDetails.booking.id}"
                chipStatus.text = tripDetails.payment?.status

                tvCustomer.text = "Khách: ${tripDetails.user.name} - ${tripDetails.user.phone}"

                tvDateTime.text = "Khởi hành: ${tripDetails.trip.departure_time} ${tripDetails.trip.trip_date}"

                tvBusType.text = "Xe: ${tripDetails.bus.license_plate} (${tripDetails.bus.seat_count} ghế)"

                tvSeat.text = "Ghế: ${tripDetails.booking.seat_id}"

                tvRoute.text = "${tripDetails.route.origin} - ${tripDetails.route.destination}"

                tvPrice.text = "Giá: ${tripDetails.trip.ticket_price}đ"

                setChipColors(chipStatus, tripDetails.payment?.status)

                btnEdit.setOnClickListener {
                    onEditClick(tripDetails)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(tripDetails)
                }
            }


        }
        private fun setChipColors(chip: Chip, status: String?) {
            when(status) {
                "completed", "Đã thanh toán" -> {
                    chip.setChipBackgroundColorResource(R.color.green_light)
                    chip.setTextColor(ContextCompat.getColor(chip.context, R.color.green_dark))
                }
                "pending", "Chưa thanh toán" -> {
                    chip.setChipBackgroundColorResource(R.color.orange_light)
                    chip.setTextColor(ContextCompat.getColor(chip.context, R.color.orange_dark))
                }
                "cancelled", "refunded" -> {
                    chip.setChipBackgroundColorResource(R.color.red_light)
                    chip.setTextColor(ContextCompat.getColor(chip.context, R.color.red_dark))
                }
                else -> {
                    chip.setChipBackgroundColorResource(R.color.gray_light)
                    chip.setTextColor(ContextCompat.getColor(chip.context, R.color.gray_dark))
                }
            }
        }
    }
}