package com.example.authenticateuserandpass.ui.a_admin_ui.trip.addTrip

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.databinding.ItemChooseBusBinding

class BusSelectionAdapter(
    private var buses: List<Bus> = emptyList(),
    private val onBusSelected: (Bus) -> Unit
) : RecyclerView.Adapter<BusSelectionAdapter.BusViewHolder>() {

    private var selectedPosition = -1

    inner class BusViewHolder(private val binding: ItemChooseBusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bus: Bus, position: Int) {
            binding.apply {
                // Hiển thị thông tin xe
                tvBusName.text = "Limousine 24 chỗ ${bus.license_plate}"
                tvBusType.text = "Giường nằm | 34 ghế"
                tvBusStatus.text = "Trạng thái: Chưa gán"

                // Set RadioButton state
                rbSelectBus.isChecked = position == selectedPosition

                // Click listener cho RadioButton
                rbSelectBus.setOnClickListener {
                    selectBus(position)
                }

                // Click listener cho toàn bộ item
                root.setOnClickListener {
                    selectBus(position)
                }
            }
        }

        private fun selectBus(position: Int) {
            val previousPosition = selectedPosition
            selectedPosition = position

            // Update UI
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // Callback với bus được chọn
            onBusSelected(buses[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val binding = ItemChooseBusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        holder.bind(buses[position], position)
    }

    override fun getItemCount() = buses.size

    fun updateBuses(newBuses: List<Bus>) {
        Log.d("BusAdapter", "updateBuses được gọi với ${newBuses.size} xe")
        buses = newBuses
        selectedPosition = -1 // Reset selection
        notifyDataSetChanged()
    }

    fun getSelectedBus(): Bus? {
        return if (selectedPosition >= 0 && selectedPosition < buses.size) {
            buses[selectedPosition]
        } else null
    }
}