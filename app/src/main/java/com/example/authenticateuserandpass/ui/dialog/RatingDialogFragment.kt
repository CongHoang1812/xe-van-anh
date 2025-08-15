package com.example.authenticateuserandpass.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.feedback.Feedback
import com.example.authenticateuserandpass.databinding.FragmentRatingDialogBinding
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RatingDialogFragment : DialogFragment() {

    private var _binding: FragmentRatingDialogBinding? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!

    private var onRatingSubmitted: ((rating: Float, selectedReasons: List<String>) -> Unit)? = null
    private var tripId: String = ""

    // Danh sách lý do theo từng mức đánh giá
    private val ratingReasons = mapOf(
        1f to listOf(
            "Thái độ tổng đài viên",
            "Thái độ lái xe trung chuyển",
            "Lái xe không an toàn",
            "Xe bẩn có mùi",
            "Nhân viên trên xe quát mắng khách"
        ),
        2f to listOf(
            "Tư vấn sai thông tin",
            "Nhân viên trên xe không lịch sự",
            "Xe không đúng giờ"
        ),
        3f to listOf(
            "Ghế ngồi không thoải mái",
            "Xe không sạch sẽ",
            "Điều hòa không mát"
        ),
        4f to listOf(
            "Một vài vấn đề nhỏ",
            "Cần cải thiện dịch vụ",
            "Chờ đợi lâu",
            "Nhân viên chua niềm nở",
            "Thấy phiền với nhân viên tại bến"
        ),
        5f to listOf(
            "Rất hài lòng về tổng đài viên",
            "Rất hài lòng về lái xe trung chuyển",
            "Rất hài lòng về chất lượng xe",
            "Xe sạch - thơm - sang trọng",
            "Trải nghiệm tuyệt vời",
            "Sẽ tiếp tục ủng hộ"
        )
    )

    companion object {
        fun newInstance(tripId: String = ""): RatingDialogFragment {
            val fragment = RatingDialogFragment()
            val args = Bundle()
            args.putString("trip_id", tripId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripId = arguments?.getString("trip_id") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        // Setup RatingBar listener
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            updateChipsForRating(rating)
        }

        // Setup button listeners
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            submitRating()
        }

        // Thiết lập kích thước dialog
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun updateChipsForRating(rating: Float) {
        binding.chipGroup.removeAllViews()

        val reasons = when {
            rating >= 1f && rating < 2f -> ratingReasons[1f]
            rating >= 2f && rating < 3f -> ratingReasons[2f]
            rating >= 3f && rating < 4f -> ratingReasons[3f]
            rating >= 4f && rating < 5f -> ratingReasons[4f]
            rating >= 5f -> ratingReasons[5f]
            else -> emptyList()
        }

        reasons?.forEach { reason ->
            addChipToGroup(reason)
        }
    }

    private fun addChipToGroup(text: String) {
        val themedContext = ContextThemeWrapper(requireContext(), R.style.Widget_App_Chip_RatingReason)
        val chip = LayoutInflater.from(themedContext)
            .inflate(R.layout.item_chip_reason, binding.chipGroup, false) as Chip

        chip.text = text
        chip.isCheckable = true
        chip.isCheckedIconVisible = false

        binding.chipGroup.addView(chip)
    }

    private fun submitRating() {
        val rating = binding.ratingBar.rating
        val selectedReasons = mutableListOf<String>()

        // Lấy danh sách chip được chọn
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedReasons.add(chip.text.toString())
            }
        }

        // Tạo Feedback object
        val feedback = Feedback(
            id = "", // Firestore sẽ tự generate
            user_id = getCurrentUserId(),
            trip_id = tripId,
            rating = rating,
            selectedReasons = selectedReasons,
            create_at = Timestamp.now()
        )

        // Lưu vào Firestore
        firestore.collection("feedbacks")
            .add(feedback)
            .addOnSuccessListener { documentReference ->
                // Update ID của document vừa tạo
                documentReference.update("id", documentReference.id)

                Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()

                // Gọi callback nếu có
                onRatingSubmitted?.invoke(rating.toFloat(), selectedReasons)
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi khi gửi đánh giá: ${e.message}", Toast.LENGTH_SHORT).show()
            }


        // Gọi callback với dữ liệu đánh giá
//        onRatingSubmitted?.invoke(rating, selectedReasons)
//        dismiss()
    }


    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }




    // Method để set callback từ bên ngoài
    fun setOnRatingSubmittedListener(listener: (rating: Float, selectedReasons: List<String>) -> Unit) {
        onRatingSubmitted = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}