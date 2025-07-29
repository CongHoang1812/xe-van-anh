package com.example.authenticateuserandpass.ui.chooseSeat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.data.model.feedback.Feedback
import com.example.authenticateuserandpass.databinding.ItemFeedbackBinding

class FeedbackAdapter(
    private val items: List<Feedback>
) : RecyclerView.Adapter<FeedbackAdapter.VH>() {

    inner class VH(val b: ItemFeedbackBinding)
        : RecyclerView.ViewHolder(b.root) {
        fun bind(f: Feedback) {
            b.tvUserId.text    = f.user_id
            b.ratingBar.rating = f.rating.toFloat()
            b.tvComment.text   = f.comment
            b.tvTime.text      = f.cteate_at
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(
            ItemFeedbackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
