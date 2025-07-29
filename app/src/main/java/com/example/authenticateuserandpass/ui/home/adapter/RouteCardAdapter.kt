package com.example.authenticateuserandpass.ui.home.adapter

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.firebaseModel.RouteCardData
import com.example.authenticateuserandpass.ui.findticket.FindTicketActivity
import java.util.Calendar
import java.util.Locale


class RouteCardAdapter(
    private val context: Context,
    private val routeList: List<RouteCardData>
) : RecyclerView.Adapter<RouteCardAdapter.RouteCardViewHolder>() {

    // Lưu ngày được chọn cho từng item theo vị trí
    private val selectedDates = mutableMapOf<Int, String>()

    inner class RouteCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageRoute: ImageView = itemView.findViewById(R.id.image_route)
        val etDepartureDate: EditText = itemView.findViewById(R.id.etDepartureDate_item)
        val btnBookNow: Button = itemView.findViewById(R.id.btn_book_now)
        val tvOrigin: TextView = itemView.findViewById(R.id.textView11)
        val tvDestination: TextView = itemView.findViewById(R.id.textView19)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_display_route, parent, false)
        return RouteCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteCardViewHolder, position: Int) {
        val item = routeList[position]

        holder.imageRoute.setImageResource(item.imageResId)
        holder.tvOrigin.text = item.origin
        holder.tvDestination.text = item.destination

        // Đặt ngày nếu đã chọn
        holder.etDepartureDate.setText(selectedDates[position] ?: "")

        // Khi nhấn vào EditText chọn ngày
        holder.etDepartureDate.setOnClickListener {
            showDatePicker(holder, position)
        }

        holder.btnBookNow.setOnClickListener {
            val selectedDate = selectedDates[position]
            if (selectedDate.isNullOrEmpty()) {
                Toast.makeText(context, "Vui lòng chọn ngày đi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(context, FindTicketActivity::class.java).apply {
                putExtra("origin", item.origin)
                putExtra("destination", item.destination)
                putExtra("trip_date", selectedDate)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = routeList.size


    @SuppressLint("DefaultLocale")
    private fun showDatePicker(holder: RouteCardViewHolder, position: Int) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(context, { _, y, m, d ->
            val selected = String.format(
                "%02d/%02d/%04d", d, m + 1, y, Locale("vi", "VN")
            )
            selectedDates[position] = selected
            holder.etDepartureDate.setText(selected)
        }, year, month, day)

        datePicker.datePicker.minDate = calendar.timeInMillis // Không chọn ngày trong quá khứ
        datePicker.show()
    }
}
