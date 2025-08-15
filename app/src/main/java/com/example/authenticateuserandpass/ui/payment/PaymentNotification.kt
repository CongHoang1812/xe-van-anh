package com.example.authenticateuserandpass.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityPaymentNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class PaymentNotification : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupPaymentNotification()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnReturnHome.setOnClickListener {
            finish()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.btnViewTicket.setOnClickListener {
            // TODO: Navigate to ticket view screen
            finish()
        }
    }

    private fun setupPaymentNotification() {
        val paymentStatus = intent.getStringExtra("payment_status")
        val paymentMethod = intent.getStringExtra("payment_method")
        val amount = intent.getStringExtra("amount")
        val transactionId = intent.getStringExtra("transaction_id")
        val errorMessage = intent.getStringExtra("error_message")

        // Fallback cho result cũ
        val result = intent.getStringExtra("result")

        when (paymentStatus ?: getStatusFromResult(result)) {
            "success" -> {
                setupSuccessUI(paymentMethod, amount, transactionId)
            }
            "pending" -> {
                setupPendingUI(paymentMethod, amount)
            }
            "canceled" -> {
                setupCanceledUI(paymentMethod, amount)
            }
            "error" -> {
                setupErrorUI(paymentMethod, amount, errorMessage)
            }
            "timeout" -> {
                setupTimeoutUI(paymentMethod, amount, errorMessage)
            }
            else -> {
                // Fallback cho format cũ
                setupDefaultUI(result)
            }
        }
    }

    private fun getStatusFromResult(result: String?): String {
        return when {
            result?.contains("thành công") == true -> "success"
            result?.contains("thất bại") == true -> "error"
            result?.contains("Lỗi") == true -> "error"
            else -> "unknown"
        }
    }

    private fun setupSuccessUI(paymentMethod: String?, amount: String?, transactionId: String?) {
        // Icon và màu sắc thành công
        binding.imageView6.apply {
            setImageResource(R.drawable.ic_check_circle)
            setColorFilter(ContextCompat.getColor(this@PaymentNotification, R.color.success_color))
        }

        // Tiêu đề và phụ đề
        binding.tvNotification.apply {
            text = "Thanh toán thành công!"
            setTextColor(ContextCompat.getColor(this@PaymentNotification, R.color.success_color))
        }

        binding.tvSubtitle.text = "Giao dịch đã được xử lý thành công"

        // Hiển thị thông tin chi tiết
        setupTransactionDetails(amount, paymentMethod, transactionId, true)

        // Nội dung thông báo
        val message = buildString {
            append("Vé của bạn đã được xác nhận thành công.")
            if (!transactionId.isNullOrEmpty()) {
                append(" Mã giao dịch: $transactionId")
            }
            append("\n\nVui lòng đến điểm đón xe đúng giờ. Cảm ơn quý khách đã sử dụng dịch vụ!")
        }

        binding.textView23.text = message
        binding.btnReturnHome.text = "Về trang chủ"
        binding.btnViewTicket.visibility = View.VISIBLE
    }

    private fun setupPendingUI(paymentMethod: String?, amount: String?) {
        // Icon và màu sắc chờ xử lý
        binding.imageView6.apply {
            setImageResource(R.drawable.ic_check_circle)
            setColorFilter(ContextCompat.getColor(this@PaymentNotification, R.color.warning_color))
        }

        binding.tvNotification.apply {
            text = "Đặt vé thành công!"
            setTextColor(ContextCompat.getColor(this@PaymentNotification, R.color.warning_color))
        }

        binding.tvSubtitle.text = "Thanh toán tiền mặt khi lên xe"

        setupTransactionDetails(amount, paymentMethod, null, false)

        val message = buildString {
            append("Bạn đã đặt vé thành công với phương thức thanh toán tiền mặt.")
            if (!amount.isNullOrEmpty()) {
                append(" Số tiền cần thanh toán: $amount VNĐ")
            }
            append("\n\nVui lòng chuẩn bị tiền mặt và thanh toán trực tiếp khi lên xe.")
        }

        binding.textView23.text = message
        binding.btnReturnHome.text = "Về trang chủ"
        binding.btnViewTicket.visibility = View.VISIBLE
    }

    private fun setupCanceledUI(paymentMethod: String?, amount: String?) {
        binding.imageView6.apply {
            setImageResource(android.R.drawable.ic_delete)
            setColorFilter(ContextCompat.getColor(this@PaymentNotification, R.color.error_color))
        }

        binding.tvNotification.apply {
            text = "Thanh toán bị hủy"
            setTextColor(ContextCompat.getColor(this@PaymentNotification, R.color.error_color))
        }

        binding.tvSubtitle.text = "Giao dịch đã bị hủy bởi người dùng"

        setupTransactionDetails(amount, paymentMethod, null, false)

        val message = buildString {
            append("Thanh toán bằng $paymentMethod đã bị hủy.")
            if (!amount.isNullOrEmpty()) {
                append(" Số tiền: $amount VNĐ")
            }
            append("\n\nVé chưa được đặt. Bạn có thể thử lại hoặc chọn phương thức thanh toán khác.")
        }

        binding.textView23.text = message
        binding.btnReturnHome.text = "Thử lại"
        binding.btnViewTicket.visibility = View.GONE
    }

    private fun setupErrorUI(paymentMethod: String?, amount: String?, errorMessage: String?) {
        binding.imageView6.apply {
            setImageResource(android.R.drawable.stat_notify_error)
            setColorFilter(ContextCompat.getColor(this@PaymentNotification, R.color.error_color))
        }

        binding.tvNotification.apply {
            text = "Lỗi thanh toán"
            setTextColor(ContextCompat.getColor(this@PaymentNotification, R.color.error_color))
        }

        binding.tvSubtitle.text = "Đã xảy ra lỗi trong quá trình xử lý"

        setupTransactionDetails(amount, paymentMethod, null, false)

        val message = buildString {
            append("Đã xảy ra lỗi khi thanh toán bằng $paymentMethod.")
            if (!amount.isNullOrEmpty()) {
                append(" Số tiền: $amount VNĐ")
            }
            if (!errorMessage.isNullOrEmpty()) {
                append("\n\nChi tiết lỗi: $errorMessage")
            }
            append("\n\nVui lòng thử lại sau hoặc liên hệ hỗ trợ: 1900 xxxx")
        }

        binding.textView23.text = message
        binding.btnReturnHome.text = "Thử lại"
        binding.btnViewTicket.visibility = View.GONE
    }

    private fun setupTimeoutUI(paymentMethod: String?, amount: String?, errorMessage: String?) {
        binding.imageView6.apply {
            setImageResource(android.R.drawable.ic_dialog_alert)
            setColorFilter(ContextCompat.getColor(this@PaymentNotification, R.color.warning_color))
        }

        binding.tvNotification.apply {
            text = "Hết thời gian chờ"
            setTextColor(ContextCompat.getColor(this@PaymentNotification, R.color.warning_color))
        }

        binding.tvSubtitle.text = "Giao dịch đã vượt quá thời gian cho phép"

        setupTransactionDetails(amount, paymentMethod, null, false)

        val message = buildString {
            append("Thời gian thanh toán đã hết (5 phút).")
            if (!amount.isNullOrEmpty()) {
                append(" Số tiền $amount VNĐ sẽ được hoàn trả (nếu có).")
            }
            append("\n\nBooking đã bị hủy tự động. Vui lòng thử đặt vé lại.")
        }

        binding.textView23.text = message
        binding.btnReturnHome.text = "Đặt vé lại"
        binding.btnViewTicket.visibility = View.GONE
    }

    private fun setupDefaultUI(result: String?) {
        binding.textView23.text = result ?: "Trạng thái thanh toán không xác định"
        binding.btnViewTicket.visibility = View.GONE
    }

    private fun setupTransactionDetails(
        amount: String?,
        paymentMethod: String?,
        transactionId: String?,
        showTransactionId: Boolean
    ) {
        // Hiển thị số tiền
        binding.tvAmount.text = if (!amount.isNullOrEmpty()) {
            formatAmount(amount)
        } else {
            "N/A"
        }

        // Hiển thị phương thức thanh toán
        binding.tvPaymentMethod.text = when (paymentMethod) {
            "ZaloPay" -> "ZaloPay"
            "Cash" -> "Tiền mặt"
            else -> paymentMethod ?: "N/A"
        }

        // Hiển thị mã giao dịch nếu có
        if (showTransactionId && !transactionId.isNullOrEmpty()) {
            binding.layoutTransactionId.visibility = View.VISIBLE
            binding.tvTransactionId.text = transactionId
        } else {
            binding.layoutTransactionId.visibility = View.GONE
        }

        // Hiển thị thời gian
        binding.tvTime.text = getCurrentDateTime()
    }

    private fun formatAmount(amount: String): String {
        return try {
            val numericAmount = amount.replace("[^\\d]".toRegex(), "")
            val formattedAmount = String.format("%,d", numericAmount.toLong())
            "$formattedAmount VNĐ"
        } catch (e: Exception) {
            "$amount VNĐ"
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}