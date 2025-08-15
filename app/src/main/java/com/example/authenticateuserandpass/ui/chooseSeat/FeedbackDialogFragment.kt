package com.example.authenticateuserandpass.ui.chooseSeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.data.model.feedback.Feedback
import com.example.authenticateuserandpass.databinding.BottomSheetFeedbackBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FeedbackDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetFeedbackBinding
    private val feedbackList = mutableListOf<Feedback>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1) Load dữ liệu mẫu hoặc thực fetch từ Firestore/API
        feedbackList.clear()
        //feedbackList += loadFakeFeedback()

        // 2) Setup RecyclerView
        binding.rvFeedback.layoutManager = LinearLayoutManager(context)
        binding.rvFeedback.adapter    = FeedbackAdapter(feedbackList)

        // 3) Đóng dialog
        binding.btnCloseFeedback.setOnClickListener { dismiss() }
    }

//    private fun loadFakeFeedback(): List<Feedback> {
//        val tripId = arguments?.getString("trip_id") ?: "T001"
//        return listOf(
//            Feedback("F001", "User001", tripId, 5,
//                "Xe sạch sẽ, lái xe thân thiện", "2025-07-12 14:03"),
//            Feedback("F002", "User002", tripId, 4,
//                "Trung chuyển hơi lâu nhưng vẫn ổn", "2025-07-11 20:27"),
//            Feedback("F003", "User003", tripId, 5,
//                "Nhân viên hỗ trợ rất nhiệt tình!", "2025-07-11 09:12"),
//            Feedback("F004", "User004", tripId, 2,
//                "Xe chật và trễ giờ", "2025-07-10 22:45")
//        )
//    }

    companion object {
        fun newInstance(tripId: String) = FeedbackDialogFragment().apply {
            arguments = Bundle().apply { putString("trip_id", tripId) }
        }
    }
}
