package com.example.authenticateuserandpass.ui.payment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityChosePaymentBinding
import com.example.authenticateuserandpass.ui.findticket.FindTicketViewModel
import com.example.authenticateuserandpass.utils.zalopay.Api.CreateOrder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                    Toast.makeText(this, "Bạn chọn thanh toán tiền mặt", Toast.LENGTH_SHORT).show()
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
                            sendSuccessNotification(
                                totalPrice.toString(),
                                departureTime.toString(),
                                tripDate.toString(),
                                origin.toString(),
                                destination.toString(),
                                selectedSeats.toString()
                            )
                            var intent =
                                Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                            createBookingAfterPayment(
                                userId.toString(),
                                selectedTrip as Trip,
                                selectedSeats.toString(),
                                pickUpLocation.toString(), dropOffLocation.toString(), ""
                            )
                            intent.putExtra("result", "Thanh toán thành công")
                            startActivity(intent)

                        }

                        override fun onPaymentCanceled(p0: String?, p1: String?) {
                            var intent =
                                Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                            intent.putExtra("result", "Thanh toán thất bại")
                            startActivity(intent)
                        }

                        override fun onPaymentError(
                            p0: ZaloPayError?,
                            p1: String?,
                            p2: String?
                        ) {
                            var intent =
                                Intent(this@ChoosePaymentActivity, PaymentNotification::class.java)
                            intent.putExtra("result", " Lỗi Thanh toán")
                            startActivity(intent)
                        }

                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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


    }







