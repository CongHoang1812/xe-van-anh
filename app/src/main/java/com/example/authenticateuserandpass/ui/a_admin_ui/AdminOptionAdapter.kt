package com.example.authenticateuserandpass.ui.a_admin_ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.AdminOption

class AdminOptionAdapter(
    private val adminOptions: List<AdminOption>,
    private val onItemClick: (AdminOption) -> Unit
) : RecyclerView.Adapter<AdminOptionAdapter.AdminOptionViewHolder>() {

    class AdminOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_option, parent, false)
        return AdminOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminOptionViewHolder, position: Int) {
        val adminOption = adminOptions[position]

        holder.ivIcon.setImageResource(adminOption.iconResId)
        holder.tvTitle.text = adminOption.title

        // Thêm hiệu ứng click cho card
        holder.itemView.setOnClickListener {
            Log.d("AdminDashboard", "Clicked on: ${adminOption.title}")
            onItemClick(adminOption)
        }
    }

    override fun getItemCount(): Int = adminOptions.size
}
