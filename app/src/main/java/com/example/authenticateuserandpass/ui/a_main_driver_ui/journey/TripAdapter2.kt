package com.example.authenticateuserandpass.ui.a_main_driver_ui.journey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ItemMainDriverJourney2Binding
import com.example.authenticateuserandpass.databinding.ItemMainDriverJourney3Binding
import com.example.authenticateuserandpass.databinding.ItemMainDriverJourneyBinding
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripDetailsUI
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripAdapter2(
    private val trips: MutableList<TripDetailsUI> = mutableListOf(),
    private val onTripClick: (TripDetailsUI) -> Unit
) : RecyclerView.Adapter<TripAdapter2.TripViewHolder>()  {
    fun updateTrips(newTrips: List<TripDetailsUI>) {
        trips.clear()
        trips.addAll(newTrips)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TripViewHolder {
        val binding = ItemMainDriverJourney3Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TripViewHolder(binding, onTripClick)

    }

    override fun onBindViewHolder(
        holder: TripViewHolder,
        position: Int
    ) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int {
        return trips.size
    }


    class TripViewHolder(
        private val binding: ItemMainDriverJourney3Binding,
        private val onTripClick: (TripDetailsUI) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: TripDetailsUI) {
            // Đặt tên tuyến đường
            binding.tvRoute.text= "${trip.origin} - ${trip.destination}"

            // Đặt giờ khởi hành
            binding.tvJourneyTimeDeparture.text = trip.departureTime
            binding.tvJourneyOrigin.text = trip.origin

            // Đặt giờ đến
            binding.tvJourneyTimeEnd.text = trip.arrivalTime
            binding.tvMainDriverDestination.text = trip.destination

            // Đặt thông tin số hành khách và điểm dừng
            binding.tvJourneyBooking.text= "${trip.availableSeats} Passengers"
            //itemView.tvStops.text = "${trip.stops} Stops"

            // Đặt thời gian còn lại đến khi khởi hành
            val remainingTime = calculateRemainingTime(trip.departureTime, trip.isToday)
            if (remainingTime > 0) {
                if (trip.isToday) {
                    binding.tvStatus.text = "IN ${remainingTime} HOURS"
                } else {
                    binding.tvStatus.text = "TOMORROW"
                }
            } else {
                binding.tvStatus.text = "DEPARTED"
            }

            // Đặt sự kiện click
//            itemView.btnViewDetails.setOnClickListener {
//                onTripClick(trip)
//            }

            // Đặt sự kiện click cho toàn bộ item
            itemView.setOnClickListener {
                onTripClick(trip)
            }
        }

        private fun calculateRemainingTime(departureTime: String, isToday: Boolean): Int {
            try {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val departure = format.parse(departureTime) ?: return 0

                val now = Calendar.getInstance()
                val departureCalendar = Calendar.getInstance()

                departureCalendar.time = departure
                departureCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
                departureCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
                departureCalendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

                if (!isToday) {
                    departureCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                val diffMs = departureCalendar.timeInMillis - now.timeInMillis
                return (diffMs / (1000 * 60 * 60)).toInt() // Convert to hours
            } catch (e: Exception) {
                return 0
            }
        }
    }


}

