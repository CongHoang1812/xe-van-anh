package com.example.authenticateuserandpass.ui.payment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.MainActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.payment.Payment
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityChosePaymentBinding
import com.example.authenticateuserandpass.ui.findticket.FindTicketViewModel
import com.example.authenticateuserandpass.utils.zalopay.Api.CreateOrder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChoosePaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChosePaymentBinding
    private lateinit var rbZaloPay: RadioButton
    private lateinit var rbCash: RadioButton
    private lateinit var btnPay: Button
    private lateinit var cardZaloPay: CardView
    private lateinit var cardCash: CardView
    private lateinit var totalPrice: String
    private val viewModel: FindTicketViewModel by viewModels {
        FindTicketViewModel.Factory(TripRepositoryImpl())
    }

    // Thêm biến để quản lý timeout và tracking booking
    private var paymentTimeoutHandler: Handler? = null
    private var paymentTimeoutRunnable: Runnable? = null
    private var tempBookingIds = mutableListOf<String>()
    private var tempPaymentIds = mutableListOf<String>()
    private var isPaymentCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChosePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rbZaloPay = findViewById(R.id.rbZaloPay)
        rbCash = findViewById(R.id.rbCash)
        btnPay = findViewById(R.id.btnPay)
        cardZaloPay = findViewById(R.id.cardZaloPay)
        cardCash = findViewById(R.id.cardCash)
        rbZaloPay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) rbCash.isChecked = false
        }
        rbCash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) rbZaloPay.isChecked = false
        }

        // Cho phép click cả cardView để chọn
        cardZaloPay.setOnClickListener { rbZaloPay.isChecked = true }
        cardCash.setOnClickListener { rbCash.isChecked = true }

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        // Init ZaloPay SDK (App ID test)
        ZaloPaySDK.init(553, Environment.SANDBOX)
        btnPay.setOnClickListener {
            when {
                rbZaloPay.isChecked -> {
                    Log.d("Payment", "Chọn ZaloPay")
                    payWithZaloPay()
                }
                rbCash.isChecked -> {
                    Log.d("Payment", "Chọn thanh toán tiền mặt")
                    payWithCash()
                }
                else -> {
                    Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
    private fun payWithZaloPay() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var totalPrice = intent.getStringExtra("total_price")
        var pickUpLocation = intent.getStringExtra("location_pickup")
        var dropOffLocation = intent.getStringExtra("location_dropoff")
        var origin = intent.getStringExtra("origin")
        var destination = intent.getStringExtra("destination")
        var departureTime = intent.getStringExtra("departureTime")
        var tripDate = intent.getStringExtra("tripDate")
        var selectedTrip = intent.getSerializableExtra("trip")
        var selectedSeats = intent.getStringExtra("seats_selected")
        var total_price_1 = totalPrice?.toString()?.replace(".", "")

        // Reset trạng thái thanh toán
        isPaymentCompleted = false
        tempBookingIds.clear()
        tempPaymentIds.clear()

        // Tạo booking và payment tạm thời trước khi thanh toán
        createTempBookingAndPayment(
            userId.toString(),
            selectedTrip as Trip,
            selectedSeats.toString(),
            pickUpLocation.toString(),
            dropOffLocation.toString(),
            "",
            totalPrice.toString()
        )

        // Bắt đầu timer 5 phút để tự động hủy booking nếu thanh toán không thành công
        startPaymentTimeout()

        val orderApi = CreateOrder()
        try {
            val data: JSONObject = orderApi.createOrder(total_price_1)
            Log.d("ZALO_PAY_RESPONSE", data.toString())
            Log.d("Amount", totalPrice.toString())

            val code = data.getString("returncode")
            if (code == "1") {
                val token = data.getString("zptranstoken")
                Log.d("ZALO_PAY_TOKEN", token)
                ZaloPaySDK.getInstance().payOrder(
                    this@ChoosePaymentActivity,
                    token,
                    "demozpdk://app",
                    object : PayOrderListener {
                        override fun onPaymentSucceeded(
                            p0: String?,
                            p1: String?,
                            p2: String?
                        ) {
                            Log.d("ZALO_PAY", "✅ Thanh toán thành công")
                            isPaymentCompleted = true
                            cancelPaymentTimeout()

                            // Cập nhật trạng thái payment thành "Đã thanh toán"
                            updatePaymentStatus("Đã thanh toán", p1 ?: "")

                            sendSuccessNotification(
                                totalPrice.toString(),
                                departureTime.toString(),
                                tripDate.toString(),
                                origin.toString(),
                                destination.toString(),
                                selectedSeats.toString()
                            )

                            val intent = Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                            intent.putExtra("payment_status", "success")
                            intent.putExtra("payment_method", "ZaloPay")
                            intent.putExtra("amount", totalPrice)
                            intent.putExtra("transaction_id", p1)
                            startActivity(intent)
                        }

                        override fun onPaymentCanceled(p0: String?, p1: String?) {
                            Log.d("ZALO_PAY", "❌ Thanh toán bị hủy")
                            isPaymentCompleted = true
                            cancelPaymentTimeout()

                            // Xóa booking và payment tạm thời
                            deleteTempBookingAndPayment()

                            val intent = Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                            intent.putExtra("payment_status", "canceled")
                            intent.putExtra("payment_method", "ZaloPay")
                            intent.putExtra("amount", totalPrice)
                            startActivity(intent)
                        }

                        override fun onPaymentError(
                            p0: ZaloPayError?,
                            p1: String?,
                            p2: String?
                        ) {
                            Log.e("ZALO_PAY", "❌ Lỗi thanh toán: ${p0?.toString()}")
                            isPaymentCompleted = true
                            cancelPaymentTimeout()

                            // Xóa booking và payment tạm thời
                            deleteTempBookingAndPayment()

                            val intent = Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                            intent.putExtra("payment_status", "error")
                            intent.putExtra("payment_method", "ZaloPay")
                            intent.putExtra("amount", totalPrice)
                            intent.putExtra("error_message", p0?.toString())
                            startActivity(intent)
                        }
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Nếu có lỗi tạo order, cũng xóa booking tạm thời
            deleteTempBookingAndPayment()
        }
    }

    private fun payWithCash() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val totalPrice = intent.getStringExtra("total_price")
        val pickUpLocation = intent.getStringExtra("location_pickup")
        val dropOffLocation = intent.getStringExtra("location_dropoff")
        val selectedTrip = intent.getSerializableExtra("trip")
        val selectedSeats = intent.getStringExtra("seats_selected")

        Toast.makeText(this, "Đã chọn thanh toán tiền mặt khi lên xe", Toast.LENGTH_SHORT).show()

        // Tạo booking và payment với trạng thái chưa thanh toán
        createBookingAndPayment(
            userId.toString(),
            selectedTrip as Trip,
            selectedSeats.toString(),
            pickUpLocation.toString(),
            dropOffLocation.toString(),
            "",
            "Cash",
            "Chưa thanh toán",
            "", // không có transaction ID
            totalPrice.toString()
        )

        val intent = Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
        intent.putExtra("payment_status", "pending")
        intent.putExtra("payment_method", "Cash")
        intent.putExtra("amount", totalPrice)
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }

    fun createBookingAfterPayment(
        userId: String,
        trip: Trip,
        selectedSeats: String,
        pickupLocation: String,
        dropoffLocation: String,
        note: String
    ) {

        var origin = intent.getStringExtra("origin")
        var destination = intent.getStringExtra("destination")
        var tripDate = intent.getStringExtra("tripDate")
        var selectedSeats = intent.getStringExtra("seats_selected")

        val db = FirebaseFirestore.getInstance()
        val bookingsRef = db.collection("bookings")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val seatList = selectedSeats?.split(",")?.map { it.trim() }
        if (seatList != null) {
            for (seat in seatList) {
                val bookingId = bookingsRef.document().id
                val booking = Booking(
                    id = bookingId,
                    user_id = userId,
                    trip_id = trip.id,
                    seat_id = seat,
                    status = "Chưa đi",
                    pickup_location = pickupLocation,
                    dropoff_location = dropoffLocation,
                    note = note,
                    book_at = timestamp
                )
                // Bước 2: Lưu từng booking riêng biệt
                bookingsRef.document(bookingId).set(booking)
                    .addOnSuccessListener {
                        Log.d("Booking", "✔ Đặt ghế $seat thành công")
                    }
                    .addOnFailureListener {
                        Log.e("Booking", "❌ Lỗi khi đặt ghế $seat: ${it.message}")
                    }
                viewModel.loadTrips(origin.toString(), destination.toString(), tripDate.toString())
            }
        }
    }
    private fun sendSuccessNotification(
        totalPrice: String,
        departureTime: String,
        tripDate: String,
        origin: String,
        destination: String,
        selectedSeats: String
    ) {
        // Android 13+ yêu cầu xin quyền
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("Notification", "Permission not granted")
                return
            }
        }
        val channelId = "booking_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo đặt vé",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi đặt vé thành công"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Đặt vé thành công!"
        val message = "Quý khách đã thanh toán thành công số tiền là $totalPrice vnđ. " +
                "Chuyến $departureTime $tripDate tuyến $origin - $destination " +
                "Ghế $selectedSeats. Hotline 02378888888"

        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_check_circle) // icon trắng nền trong suốt
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(1001, notification)
        }


    private fun createBookingAndPayment(
        userId: String,
        trip: Trip,
        selectedSeats: String,
        pickupLocation: String,
        dropoffLocation: String,
        note: String,
        paymentMethod: String,
        paymentStatus: String,
        transactionId: String,
        totalAmount: String
    ) {
        val origin = intent.getStringExtra("origin")
        val destination = intent.getStringExtra("destination")
        val tripDate = intent.getStringExtra("tripDate")

        val db = FirebaseFirestore.getInstance()
        val bookingsRef = db.collection("bookings")
        val paymentsRef = db.collection("payments")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val seatList = selectedSeats.split(",").map { it.trim() }

        // Tạo 1 payment duy nhất cho toàn bộ booking
        val paymentId = paymentsRef.document().id
        val payment = Payment(
            id = paymentId,
            bookingId = "", // Sẽ cập nhật với danh sách booking IDs
            amount = totalAmount,
            paymentMethod = paymentMethod,
            status = paymentStatus,
            transactionId = transactionId,
            paidAt = if (paymentStatus == "Đã thanh toán") timestamp else "",
            createdAt = timestamp
        )

        val bookingIds = mutableListOf<String>()

        // Tạo booking cho từng ghế
        for (seat in seatList) {
            val bookingId = bookingsRef.document().id
            bookingIds.add(bookingId)

            val booking = Booking(
                id = bookingId,
                user_id = userId,
                trip_id = trip.id,
                seat_id = seat,
                status = "Chưa đi",
                pickup_location = pickupLocation,
                dropoff_location = dropoffLocation,
                note = note,
                book_at = timestamp
            )

            // Lưu booking
            bookingsRef.document(bookingId).set(booking)
                .addOnSuccessListener {
                    Log.d("Booking", "✔ Đặt ghế $seat thành công")
                }
                .addOnFailureListener {
                    Log.e("Booking", "❌ Lỗi khi đặt ghế $seat: ${it.message}")
                }
        }

        // Cập nhật payment với danh sách booking IDs
        val updatedPayment = payment.copy(bookingId = bookingIds.joinToString(","))

        // Lưu payment (chỉ 1 payment cho toàn bộ booking)
        paymentsRef.document(paymentId).set(updatedPayment)
            .addOnSuccessListener {
                Log.d("Payment", "✔ Tạo payment thành công cho ${seatList.size} ghế")
            }
            .addOnFailureListener {
                Log.e("Payment", "❌ Lỗi khi tạo payment: ${it.message}")
            }

        // Load lại trips để cập nhật UI
        viewModel.loadTrips(origin.toString(), destination.toString(), tripDate.toString())
    }

    // Thêm các phương thức xử lý timeout và quản lý booking tạm thời
    private fun createTempBookingAndPayment(
        userId: String,
        trip: Trip,
        selectedSeats: String,
        pickupLocation: String,
        dropoffLocation: String,
        note: String,
        totalAmount: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val bookingsRef = db.collection("bookings")
        val paymentsRef = db.collection("payments")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val seatList = selectedSeats.split(",").map { it.trim() }

        // Tạo 1 payment duy nhất cho toàn bộ booking tạm thời
        val paymentId = paymentsRef.document().id
        val bookingIds = mutableListOf<String>()

        // Tạo booking cho từng ghế
        for (seat in seatList) {
            val bookingId = bookingsRef.document().id
            bookingIds.add(bookingId)
            tempBookingIds.add(bookingId)

            val booking = Booking(
                id = bookingId,
                user_id = userId,
                trip_id = trip.id,
                seat_id = seat,
                status = "Chờ thanh toán", // Trạng thái tạm thời
                pickup_location = pickupLocation,
                dropoff_location = dropoffLocation,
                note = note,
                book_at = timestamp
            )

            // Lưu booking tạm thời
            bookingsRef.document(bookingId).set(booking)
                .addOnSuccessListener {
                    Log.d("TempBooking", "✔ Tạo booking tạm thời cho ghế $seat")
                }
                .addOnFailureListener {
                    Log.e("TempBooking", "❌ Lỗi tạo booking tạm thời cho ghế $seat: ${it.message}")
                }
        }

        // Tạo 1 payment duy nhất với danh sách booking IDs
        val payment = Payment(
            id = paymentId,
            bookingId = bookingIds.joinToString(","), // Nối tất cả booking IDs
            amount = totalAmount,
            paymentMethod = "ZaloPay",
            status = "Đang xử lý", // Trạng thái tạm thời
            transactionId = "",
            paidAt = "",
            createdAt = timestamp
        )

        // Lưu ID payment để có thể xóa/cập nhật sau
        tempPaymentIds.add(paymentId)

        // Lưu payment tạm thời
        paymentsRef.document(paymentId).set(payment)
            .addOnSuccessListener {
                Log.d("TempPayment", "✔ Tạo payment tạm thời cho ${seatList.size} ghế")
            }
            .addOnFailureListener {
                Log.e("TempPayment", "❌ Lỗi tạo payment tạm thời: ${it.message}")
            }
    }

    private fun startPaymentTimeout() {
        paymentTimeoutHandler = Handler(Looper.getMainLooper())
        paymentTimeoutRunnable = Runnable {
            if (!isPaymentCompleted) {
                Log.w("PaymentTimeout", "⏰ Thanh toán ZaloPay timeout sau 5 phút - Xóa booking tạm thời")
                deleteTempBookingAndPayment()

                // Hiển thị thông báo timeout
                Toast.makeText(this, "Thanh toán đã hết thời gian. Booking đã bị hủy.", Toast.LENGTH_LONG).show()

                // Chuyển về màn hình thông báo lỗi
                val intent = Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                intent.putExtra("payment_status", "timeout")
                intent.putExtra("payment_method", "ZaloPay")
                intent.putExtra("error_message", "Thanh toán đã hết thời gian 5 phút")
                startActivity(intent)
            }
        }

        // 5 phút = 5 * 60 * 1000 milliseconds
        paymentTimeoutHandler?.postDelayed(paymentTimeoutRunnable!!, 5 * 60 * 1000L)
        Log.d("PaymentTimeout", "🕐 Bắt đầu timer 5 phút cho thanh toán ZaloPay")
    }

    private fun cancelPaymentTimeout() {
        paymentTimeoutRunnable?.let { runnable ->
            paymentTimeoutHandler?.removeCallbacks(runnable)
            Log.d("PaymentTimeout", "✅ Hủy timer thanh toán")
        }
        paymentTimeoutHandler = null
        paymentTimeoutRunnable = null
    }

    private fun deleteTempBookingAndPayment() {
        val db = FirebaseFirestore.getInstance()

        // Xóa tất cả booking tạm thời
        tempBookingIds.forEach { bookingId ->
            db.collection("bookings").document(bookingId).delete()
                .addOnSuccessListener {
                    Log.d("DeleteTemp", "✔ Xóa booking tạm thời: $bookingId")
                }
                .addOnFailureListener {
                    Log.e("DeleteTemp", "❌ Lỗi xóa booking tạm thời $bookingId: ${it.message}")
                }
        }

        // Xóa tất cả payment tạm thời
        tempPaymentIds.forEach { paymentId ->
            db.collection("payments").document(paymentId).delete()
                .addOnSuccessListener {
                    Log.d("DeleteTemp", "✔ Xóa payment tạm thời: $paymentId")
                }
                .addOnFailureListener {
                    Log.e("DeleteTemp", "❌ Lỗi xóa payment tạm thời $paymentId: ${it.message}")
                }
        }

        // Clear danh sách
        tempBookingIds.clear()
        tempPaymentIds.clear()
    }

    private fun updatePaymentStatus(status: String, transactionId: String) {
        val db = FirebaseFirestore.getInstance()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Cập nhật trạng thái tất cả payment
        tempPaymentIds.forEach { paymentId ->
            val updates = mapOf(
                "status" to status,
                "transactionId" to transactionId,
                "paidAt" to timestamp
            )

            db.collection("payments").document(paymentId).update(updates)
                .addOnSuccessListener {
                    Log.d("UpdatePayment", "✔ Cập nhật payment thành công: $paymentId")
                }
                .addOnFailureListener {
                    Log.e("UpdatePayment", "❌ Lỗi cập nhật payment $paymentId: ${it.message}")
                }
        }

        // Cập nhật trạng thái booking từ "Chờ thanh toán" thành "Chưa đi"
        tempBookingIds.forEach { bookingId ->
            db.collection("bookings").document(bookingId).update("status", "Chưa đi")
                .addOnSuccessListener {
                    Log.d("UpdateBooking", "✔ Cập nhật booking thành công: $bookingId")
                }
                .addOnFailureListener {
                    Log.e("UpdateBooking", "❌ Lỗi cập nhật booking $bookingId: ${it.message}")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy timer khi activity bị destroy
        cancelPaymentTimeout()
    }
}
