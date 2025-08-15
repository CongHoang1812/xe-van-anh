package com.example.authenticateuserandpass.ui.feedback

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ItemFeedbackBinding
import com.example.authenticateuserandpass.databinding.ItemTripReviewBinding
import com.example.authenticateuserandpass.ui.dialog.RatingDialogFragment

class FeedbackAdapter(
    private var tickets: List<UserTicket>,
    private val onTripFeedbackListener: OnTripFeedbackListener,
    private val fragmentManager: FragmentManager

) : RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTripReviewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, onTripFeedbackListener, fragmentManager)
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

    fun updateTickets(newTickets: List<UserTicket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemTripReviewBinding,
        private val onTripFeedbackListener: OnTripFeedbackListener,
        private val fragmentManager: FragmentManager
    ): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(ticket: UserTicket, position: Int) {
            binding.tvTicketCode.text = "Mã vé: ${ticket.ticketCode}"
            binding.tvRouteNameMyTicket.text = ticket.routeName
            binding.tvDeTime1.text = ticket.departureTime
            binding.tvDeTime2.text = ticket.departureTime
            binding.tvTripDateMyTicket.text = ticket.departureDate
            binding.tvTicketPriceMyTicket.text = "${ticket.price} vnđ"
            binding.tvPaymentStatusMyTicket.text = ticket.tripStatus
            binding.root.setOnClickListener {
                //onTripFeedbackListener.onFeedbackClick(ticket.tripStatus, position)
                onTripFeedbackListener.onFeedbackClick(ticket, position)
                binding.btnRatingTrip.setOnClickListener {
                    RatingDialogFragment.newInstance(ticket.tripId)
                        .show(fragmentManager, "RatingDialog")
                }
            }
        }
    }




}
interface OnTripFeedbackListener {
    fun onFeedbackClick(ticket: UserTicket, position: Int)
}