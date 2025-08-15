package com.example.authenticateuserandpass.ui.a_main_driver_ui.journey

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.FragmentMainDriverJourneyBinding
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripDetailsUI
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripViewModel
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripViewModel.Factory


class MainDriverJourneyFragment : Fragment() {
    private lateinit var binding: FragmentMainDriverJourneyBinding
    private lateinit var viewModel: TripViewModel
    private val todayAdapter = TripAdapter { trip -> navigateToTripDetails(trip) }
    private val tomorrowAdapter = TripAdapter2 { trip -> navigateToTripDetails(trip) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainDriverJourneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, Factory(TripRepositoryImpl()))[TripViewModel::class.java]
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        viewModel.fetchTrips()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchTrips()
        }
    }

    private fun setupRecyclerViews() {
        binding.rvTodayTrips.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todayAdapter

        }

        binding.rvTomorrowTrips.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tomorrowAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.todayTrips.observe(viewLifecycleOwner) { trips ->
            todayAdapter.updateTrips(trips)
            updateTodayTripsVisibility(trips)
            Log.d("MainDriverJourneyFragment", "Có ${trips.size} chuyến đi hôm nay")
        }

        viewModel.tomorrowTrips.observe(viewLifecycleOwner) { trips ->
            tomorrowAdapter.updateTrips(trips)
            updateTomorrowTripsVisibility(trips)
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

    private fun updateTodayTripsVisibility(trips: List<TripDetailsUI>) {
        if (trips.isEmpty()) {
            binding.tvTodayTitle.visibility = View.GONE
            binding.rvTodayTrips.visibility = View.GONE
            binding.tvTodayNoTrips.visibility = View.VISIBLE
        } else {
            binding.tvTodayTitle.visibility = View.VISIBLE
            binding.rvTodayTrips.visibility = View.VISIBLE
            binding.tvTodayNoTrips.visibility = View.GONE
        }
    }

    private fun updateTomorrowTripsVisibility(trips: List<TripDetailsUI>) {
        if (trips.isEmpty()) {
            binding.tvTomorrowTitle.visibility = View.GONE
            binding.rvTomorrowTrips.visibility = View.GONE
            binding.tvTomorrowNoTrips.visibility = View.VISIBLE
        } else {
            binding.tvTomorrowTitle.visibility = View.VISIBLE
            binding.rvTomorrowTrips.visibility = View.VISIBLE
            binding.tvTomorrowNoTrips.visibility = View.GONE
        }
    }




    private fun navigateToTripDetails(trip: TripDetailsUI) {
        // Chuyển đến màn hình chi tiết chuyến đi
        val action =  MainDriverJourneyFragmentDirections.actionNav2ToPassengerListFragment2(trip.id)
        findNavController().navigate(action)
    }

}