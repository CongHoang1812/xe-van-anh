package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.tabFragments.DaHoanThanhFragment
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.tabFragments.KhachDonFragment
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.tabFragments.KhachTraFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> KhachDonFragment()
            1 -> KhachTraFragment()
            2 -> DaHoanThanhFragment()
            else -> KhachDonFragment()
        }
    }
}