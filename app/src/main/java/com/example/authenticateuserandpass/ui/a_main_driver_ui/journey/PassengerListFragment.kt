package com.example.authenticateuserandpass.ui.a_main_driver_ui.journey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.firebaseModel.Passenger
import com.example.authenticateuserandpass.databinding.FragmentPassengerListBinding


class PassengerListFragment : Fragment() {

    private lateinit var binding: FragmentPassengerListBinding
    private val viewModel: PassengerListViewModel by viewModels()
    private val args: PassengerListFragmentArgs by navArgs()

    private val passengerAdapter = PassengerAdapter(
        onCallClick = { passenger -> callPassenger(passenger) },
        onMessageClick = { passenger -> messagePassenger(passenger) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPassengerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Lấy tripId từ argument và tải dữ liệu
        val tripId = args.tripId
        viewModel.loadPassengers(tripId)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPassengers(tripId)
        }
    }

    private fun setupRecyclerView() {
        binding.rvPassengers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = passengerAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.passengers.observe(viewLifecycleOwner) { passengers ->
            passengerAdapter.updatePassengers(passengers)
            updatePassengersVisibility(passengers)
        }

        viewModel.tripDetails.observe(viewLifecycleOwner) { tripDetails ->
            binding.tvRouteTitle.text = "${tripDetails.origin} to ${tripDetails.destination}"
            binding.tvDepartureTime.text = tripDetails.departureTime
            binding.tvArrivalTime.text = tripDetails.arrivalTime
            binding.tvPassengerCount.text = "${tripDetails.totalSeats - tripDetails.availableSeats}/${tripDetails.totalSeats} hành khách"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassengersVisibility(passengers: List<Passenger>) {
        if (passengers.isEmpty()) {
            binding.rvPassengers.visibility = View.GONE
            binding.tvNoPassengers.visibility = View.VISIBLE
        } else {
            binding.rvPassengers.visibility = View.VISIBLE
            binding.tvNoPassengers.visibility = View.GONE
        }
    }

    private fun callPassenger(passenger: Passenger) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${passenger.phone}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun messagePassenger(passenger: Passenger) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:${passenger.phone}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show()
        }
    }
}