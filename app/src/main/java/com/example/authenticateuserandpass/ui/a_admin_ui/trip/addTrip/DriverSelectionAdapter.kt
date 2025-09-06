package com.example.authenticateuserandpass.ui.a_admin_ui.trip.addTrip

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ItemChooseMainDriverBinding


class DriverSelectionAdapter(
    private val onDriverClick: (User) -> Unit
) : RecyclerView.Adapter<DriverSelectionAdapter.DriverViewHolder>() {

    private val drivers = mutableListOf<User>()
    private var selectedPosition = -1

    fun updateDrivers(newDrivers: List<User>) {
        Log.d("DriverAdapter", "updateDrivers: ${newDrivers.size} tài xế")
        drivers.clear()
        drivers.addAll(newDrivers)
        notifyDataSetChanged()
        Log.d("DriverAdapter", "Adapter có ${drivers.size} tài xế sau update")
    }

    override fun getItemCount(): Int {
        Log.d("DriverAdapter", "getItemCount: ${drivers.size}")
        return drivers.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        Log.d("DriverAdapter", "onCreateViewHolder được gọi")
        val binding = ItemChooseMainDriverBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DriverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        Log.d("DriverAdapter", "onBindViewHolder position: $position")
        holder.bind(drivers[position], position == selectedPosition)
    }

    fun getSelectedDriver(): User? {
        return if (selectedPosition >= 0) drivers[selectedPosition] else null
    }

    inner class DriverViewHolder(private val binding: ItemChooseMainDriverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(driver: User, isSelected: Boolean) {
            Log.d("DriverAdapter", "Binding driver: ${driver.name}")
            // Cập nhật theo các TextView ID trong item_choose_main_driver.xml
            binding.textFullName.text = driver.name
            binding.textPhoneNumber.text = driver.phone
            Glide.with(binding.root.context)
                .load(driver.avatarUrl)
                .circleCrop() // Làm ảnh tròn
                 // Ảnh placeholder khi đang load
                .placeholder(com.example.authenticateuserandpass.R.drawable.ic_driver)
                .error(com.example.authenticateuserandpass.R.drawable.ic_driver) // Ảnh hiển thị khi lỗi
                .into(binding.imgAvatar) // Thay ima

            //binding.tvDriverRole.text = "Tài xế chính"

            binding.root.isSelected = isSelected
            binding.root.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onDriverClick(driver)
            }
        }
    }
}