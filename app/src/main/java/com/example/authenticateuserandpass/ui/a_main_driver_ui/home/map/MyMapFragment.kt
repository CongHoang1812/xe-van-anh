package com.example.authenticateuserandpass.ui.a_main_driver_ui.home.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.authenticateuserandpass.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions



class MyMapFragment : Fragment(), OnMapReadyCallback{
    private lateinit var googleMap: GoogleMap



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        // Ví dụ: Thêm marker tại Hà Nội
        val hanoi = LatLng(21.028511, 105.804817)
        googleMap.addMarker(MarkerOptions().position(hanoi).title("Hà Nội"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 12f))
    }


}