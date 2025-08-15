package com.example.authenticateuserandpass.ui.myticket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.UserTicket

class UserTicketAdapter(
    private var tickets: List<UserTicket>,
    private val clickListener: UserTicketClickListener
) : RecyclerView.Adapter<UserTicketAdapter.TicketViewHolder>() {

    fun updateTickets(newTickets: List<UserTicket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position], position)
    }

    override fun getItemCount(): Int = tickets.size

    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTicketCode: TextView = itemView.findViewById(R.id.tv_ticket_code)
        private val tvRouteName: TextView = itemView.findViewById(R.id.tv_route_name_my_ticket)
        private val tvDepartureDateTime: TextView = itemView.findViewById(R.id.tv_de_time_1)
        private val tvDepartureDateTime2: TextView = itemView.findViewById(R.id.tv_de_time_2)
//        private val tvPickupDropoff: TextView = itemView.findViewById(R.id.tvPickupDropoff)
//        private val tvSeatNumbers: TextView = itemView.findViewById(R.id.tvSeatNumbers)
        private val tvTripDate: TextView = itemView.findViewById(R.id.tv_trip_date_my_ticket)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_ticket_price_my_ticket)
       private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tv_payment_status_my_ticket)
        private val tvTripStatus: TextView = itemView.findViewById(R.id.tv_trip_status)

        fun bind(ticket: UserTicket, position: Int) {
            tvTicketCode.text = "Mã vé: ${ticket.ticketCode}"
            tvRouteName.text = ticket.routeName
            tvDepartureDateTime.text = ticket.departureTime
            tvDepartureDateTime2.text = ticket.departureTime
//            tvPickupDropoff.text = "${ticket.pickupPoint} → ${ticket.dropoffPoint}"
//            tvSeatNumbers.text = "Ghế: ${ticket.seatNumbers}"
            tvTripDate.text = "${ticket.departureDate}"
            tvPrice.text = ticket.price + ".000VNĐ"

            // Đổi màu theo trạng thái thanh toán
            when (ticket.paymentStatus.lowercase()) {
                "đã thanh toán", "completed", "success" -> {
                    tvPaymentStatus.text = "Đã thanh toán"
                    tvPaymentStatus.setBackgroundColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                else -> {
                    tvPaymentStatus.text = "Chưa thanh toán"
                    tvPaymentStatus.setBackgroundColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
            }

            // Đổi màu theo trạng thái chuyến đi
            when (ticket.tripStatus.lowercase()) {
                "đã đi", "completed" -> {
                    tvTripStatus.text = "🚌 Đã đi"
                    tvTripStatus.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
                }
                "đã hủy", "cancelled" -> {
                    tvTripStatus.text = "❌ Đã hủy"
                    tvTripStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
                else -> {
                    tvTripStatus.text = "⏳ Chưa đi"
                    //tvTripStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
            }

            // Xử lý click event
            itemView.setOnClickListener {
                clickListener.onTicketClick(ticket, position)
            }
        }
    }
}
interface UserTicketClickListener {
    fun onTicketClick(ticket: UserTicket, position: Int)
}