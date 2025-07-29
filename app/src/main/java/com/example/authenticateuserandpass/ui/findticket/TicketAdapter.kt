package com.example.authenticateuserandpass.ui.findticket

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ItemTicketBinding

class TicketAdapter(
    private val onTicketClickListener: OnTicketClickListener
) : RecyclerView.Adapter<TicketAdapter.ViewHolder>() {
    private val tickets : MutableList<Trip> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTicketBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, onTicketClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(tickets[position], position)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }
    fun updateTickets(newTickets: List<Trip>?){
        newTickets?.let {
            val oldSize = tickets.size
            tickets.clear()
            tickets.addAll(newTickets)
            if(oldSize > newTickets.size){
                notifyItemRangeRemoved(tickets.size, oldSize)
            }
            notifyItemRangeChanged(0, tickets.size)
        }
    }

    class ViewHolder(
        private val binding: ItemTicketBinding,
        private val onTicketClickListener: OnTicketClickListener
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(ticket: Trip, index: Int){
            binding.tvPriceTicket.text = ticket.ticket_price
            binding.tvDate.text = ticket.trip_date
            binding.tvTime.text = ticket.departure_time
            binding.tvTime2.text = ticket.departure_time
            binding.tvNumberSeatsAvailable.text = ticket.availableSeats.toString()
            binding.root.setOnClickListener {
                onTicketClickListener.onClick(ticket, index)
            }
            Log.d("TicketAdapter", "Trip $index: ${ticket.trip_date}, ${ticket.departure_time}, ${ticket.ticket_price}")

        }
    }
    interface OnTicketClickListener{
        fun onClick(ticket: Trip, index: Int)
    }
}
