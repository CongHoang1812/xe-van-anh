package com.example.authenticateuserandpass.ui.a_main_driver_ui.home.map

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.StopPoint
import com.example.authenticateuserandpass.ui.a_main_driver_ui.home.BookingViewModel
import com.example.authenticateuserandpass.ui.a_main_driver_ui.home.MainDriverHomeFragment
import com.example.authenticateuserandpass.ui.a_main_driver_ui.home.MainDriverHomeFragment.Companion.currentTripId
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.concurrent.thread

class MyMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val apiKey = "AIzaSyCmpgkCXqYgETy-C1hAtYq5zFJ84afjOzs" // <-- Thay bằng API Key của bạn
    private lateinit var bookingViewModel: BookingViewModel


    // Các điểm: origin, destination, điểm dừng
    private val origin = LatLng(21.028511, 105.804817)
    private val destination = LatLng(19.8075, 105.7769)
    private val waypoints = listOf(
        LatLng(20.545, 105.91),
        LatLng(20.254, 105.975)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookingViewModel = ViewModelProvider(this).get(BookingViewModel::class.java)
        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)


        val btnUpdateBooking = view.findViewById<Button>(R.id.button3)
        val tripId = MainDriverHomeFragment.currentTripId
        if (tripId != null) {
            if (isTripUpdated(tripId)) {
                // Nếu trip hiện tại đã hoàn tất -> disable nút
                btnUpdateBooking.isEnabled = false
                btnUpdateBooking.alpha = 0.5f
            } else {
                // Nếu là trip mới hoặc chưa hoàn tất -> enable nút
                btnUpdateBooking.isEnabled = true
                btnUpdateBooking.alpha = 1f
            }
        }
        if (tripId != null && isTripUpdated(tripId)) {
            btnUpdateBooking.isEnabled = false
            btnUpdateBooking.alpha = 0.5f
        }

        btnUpdateBooking.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn cập nhật tất cả vé thành 'Đã đi'?")
                .setPositiveButton("Đồng ý") { _, _ ->
                    val tripId = MainDriverHomeFragment.currentTripId
                    if (tripId != null) {
                        bookingViewModel.markAllBookingsAsCompleted(tripId)
                        btnUpdateBooking.isEnabled = false
                        btnUpdateBooking.alpha = 0.5f
                        setTripUpdated(currentTripId)
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy tripId", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        // Timeline
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTimeline)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val stops = listOf(
            StopPoint("BX Nước Ngầm", "08:00"),
            StopPoint("TP Phủ Lý, Hà Nam", "09:30"),
            StopPoint("Trạm dừng nghỉ CT Ninh Bình", "11:00"),
            StopPoint("TP Thanh Hóa", "13:00")
        )

        recyclerView.adapter = TimelineAdapter(stops)
    }
    private fun isTripUpdated(tripId: String): Boolean {
        val prefs = requireContext().getSharedPreferences("BookingPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("updated_$tripId", false)
    }

    private fun setTripUpdated(tripId: String?) {
        val prefs = requireContext().getSharedPreferences("BookingPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("updated_$tripId", true).apply()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Thêm marker cho origin, destination và điểm dừng
        googleMap.addMarker(MarkerOptions().position(origin).title("Điểm xuất phát"))
        waypoints.forEachIndexed { index, latLng ->
            googleMap.addMarker(MarkerOptions().position(latLng).title("Điểm dừng ${index + 1}"))
        }
        googleMap.addMarker(MarkerOptions().position(destination).title("Điểm đến"))

        // Vẽ tuyến đường thực tế từ Google Directions API
        drawRoute(origin, destination, waypoints)
    }

    private fun drawRoute(origin: LatLng, destination: LatLng, waypoints: List<LatLng>) {
        thread {
            try {
                val waypointsStr = waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
                val url =
                    "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                            "&destination=${destination.latitude},${destination.longitude}" +
                            "&waypoints=$waypointsStr" +
                            "&key=$apiKey"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@thread

                val json = JSONObject(body)
                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) return@thread

                val overviewPolyline = routes.getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val path = PolyUtil.decode(overviewPolyline)
                Log.d("MyMapFragment", "Polyline points count: ${path.size}")

                // Vẽ Polyline trên UI thread
                activity?.runOnUiThread {
                    googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(path)
                            .width(12f)
                            .color(Color.BLUE)
                    )

                    // Zoom để thấy toàn tuyến
                    val builder = LatLngBounds.builder()
                    path.forEach { builder.include(it) }
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(), 150)
                    )
                }

            } catch (e: Exception) {
                Log.e("MyMapFragment", "Error drawing route: ${e.message}")
            }
        }
    }
}