package com.example.authenticateuserandpass.ui.a_main_driver_ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.FragmentMainDriverHomeBinding
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripViewModel
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripViewModel.Factory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class MainDriverHomeFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var binding: FragmentMainDriverHomeBinding
    private lateinit var viewModel: TripViewModel

    companion object {
        @JvmStatic
        var currentTripId: String? = null
            private set

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainDriverHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this, Factory(TripRepositoryImpl()))[TripViewModel::class.java]
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainDriverId =  FirebaseAuth.getInstance().currentUser?.uid
        mainDriverId?.let {
            viewModel.getUpComingTripInfoMainDriver(it)
        }


        viewModel.nextTrip.observe(viewLifecycleOwner) { tripInfo ->
            tripInfo.let {
                Log.d("MainDriverHomeFragment", "tripInfo: $it")
                currentTripId = it.tripId
                binding.tvRoute.text = it.routeName
                binding.tvCountDownTimeHour.text = it.hoursLeft.toString()
                binding.tvMainDriverHomeOrigin.text = it.origin
                binding.tvMainDriverHomeDestination.text = it.destination

            }
        }
        setupTripInfoClick()
        showMap()

    }
    private fun setupTripInfoClick() {
        binding.tripInfo.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_nav_1_to_myMapFragment2)
            } catch (e: Exception) {
                Log.e("MainDriverHomeFragment", "Navigation error: ${e.message}")
            }
        }
    }

    private fun showMap() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val fragmentManager = childFragmentManager
        mapFragment = SupportMapFragment.newInstance()
        fragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { googleMap ->
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Nếu chưa có quyền, bạn nên yêu cầu quyền ở đây
                return@getMapAsync
            }

            googleMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(myLatLng)
                            .title("Vị trí của tôi")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Không thể lấy vị trí hiện tại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val fragmentManager = childFragmentManager
//        mapFragment = SupportMapFragment.newInstance()
//        fragmentManager.beginTransaction()
//            .replace(R.id.map_container, mapFragment)
//            .commit()
//
//        mapFragment.getMapAsync { googleMap ->
//            // Tuỳ chỉnh map ở đây
//            val startLatLng = LatLng(21.006382, 105.854444) // BX Nước Ngầm
//            val endLatLng = LatLng(19.807208, 105.776184)   // VP Đông Minh
//
//            googleMap.addMarker(MarkerOptions().position(startLatLng).title("BX Nước Ngầm"))
//            googleMap.addMarker(MarkerOptions().position(endLatLng).title("VP Đông Minh"))
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 7f))
//
//            // Vẽ đường đi nếu cần (Polyline)
////            googleMap.addPolyline(
////                PolylineOptions()
////                    .add(startLatLng, endLatLng)
////                    .color(Color.BLUE)
////                    .width(8f)
////            )
//        }
//    }