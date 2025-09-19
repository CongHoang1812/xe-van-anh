package com.example.authenticateuserandpass.ui.chooseSeat

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.ui.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ActivityChooseSeatBinding
import com.example.authenticateuserandpass.ui.account.updateProfile.UpdateProfileActivity
import com.example.authenticateuserandpass.ui.home.HomeFragment
import com.example.authenticateuserandpass.ui.pickdroplocation.PickAnDropActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class ChooseSeatActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityChooseSeatBinding
    private lateinit var adapter: SeatAdapter
    private lateinit var selectedTrip: Trip
    private var ticketPrice   : String = ""
    private lateinit var timer: CountDownTimer
    private var selectedCount = 0
    private val totalTimeMillis = 5 * 60 * 1000L // 5 phút

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChooseSeatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        setSupportActionBar(binding.toolbarHome)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        selectedTrip = intent.getSerializableExtra("trip", Trip::class.java)!!
        setupRecyclerView()
        loadSeats()
        //setupConfirmButton()
        setupView()
        startCountdown()
        setupContinueButton()
    }

    private fun setupView() {
        ticketPrice = intent.getStringExtra("ticket_price").toString()
        var origin = intent.getStringExtra(HomeFragment.EDIT_DEPARTURE)
        var destination = intent.getStringExtra(HomeFragment.EDIT_DESTINATION)
        var departureTime = intent.getStringExtra("time_departure")
        var tripDate = intent.getStringExtra("date_departure")
        binding.tvOrigin.text = origin
        binding.tvDestination.text = destination
        binding.tvTripDate.text = tripDate
        binding.tvDepartureTime.text = departureTime
    }
    private fun setupRecyclerView() {
        adapter = SeatAdapter { selectedIds, count ->
            binding.tvSeatsSelected.text = count.toString()
            binding.tvSelectedSeat.text = selectedIds
            val unitPrice = selectedTrip.ticket_price.toIntOrNull() ?: 0
            val totalPrice = count * unitPrice
            binding.tvTotalPrice.text = "$totalPrice.000"
            binding.btnContinue.alpha = if (count > 0) 1f else 0.5f
        }

        val spanCount = 4
        binding.seatRecyclerview.layoutManager = GridLayoutManager(this, spanCount)
        binding.seatRecyclerview.adapter = adapter

        // Decoration: gap between cols 1–2 and 3–4
        val gap = dpToPx(24)
        binding.seatRecyclerview.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val pos = parent.getChildAdapterPosition(view)
                val column = pos % spanCount
                if (column == 1) {
                    outRect.right = gap
                } else if (column == 2) {
                    outRect.left = gap
                }
            }
        })
        binding.tvPriceCancel.setOnClickListener {
            (binding.root.context as? ChooseSeatActivity)?.showCancelPolicyBottomSheet()

        }
        binding.tvFeedback.setOnClickListener {
            val tripId = selectedTrip.id  // hoặc bất cứ giá trị nào
            FeedbackDialogFragment
                .newInstance(tripId)
                .show(supportFragmentManager, "feedbackDialog")
        }
        binding.toolbarHome.setOnClickListener {
            finish()
        }
    }
    private fun loadSeats() {
        val db = FirebaseFirestore.getInstance()
        val tripId = selectedTrip.id

        // Lấy các ghế đã đặt của chuyến xe này
        db.collection("bookings")
            .whereEqualTo("trip_id", tripId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bookedSeats = mutableSetOf<String>()

                for (document in querySnapshot.documents) {
                    val seatId = document.getString("seat_id")
                    val status = document.getString("status")
                    if (seatId != null ) {
                        bookedSeats.add(seatId)
                    }
                }

                val list = mutableListOf<Booking>()
                for (i in 1..6) {
                    val ids = listOf(
                        "A$i",
                        "A${i + 6}",
                        "A${i + 12}",
                        "A${i + 18}"
                    )
                    for (id in ids) {
                        list += Booking(
                            seat_id = id,
                            status = if (bookedSeats.contains(id)) "booked" else "available"
                        )
                    }
                }

                adapter.setBookings(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi tải ghế: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
    private fun startCountdown() {
        timer = object : CountDownTimer(totalTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvCountdown.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvCountdown.text = "00:00"
                Toast.makeText(this@ChooseSeatActivity, "Hết giờ!", Toast.LENGTH_SHORT).show()
                // Xử lý hết thời gian tại đây
            }
        }
        timer.start()
    }
    private fun showCancelPolicyBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_cancel_policy, null)
        dialog.setContentView(view)

        val btn = view.findViewById<Button>(R.id.btnDismiss)
        btn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
    private fun setupContinueButton(){
        binding.btnContinue.isEnabled = true
        binding.btnContinue.setOnClickListener {
            if (binding.tvSeatsSelected.text != "0") {
                // chuyển màn hình khi đã chọn ghế
                val intent = Intent(this, PickAnDropActivity::class.java)
                intent.putExtra("seats_selected", binding.tvSelectedSeat.text.toString())
                intent.putExtra("origin", binding.tvOrigin.text.toString())
                intent.putExtra("destination", binding.tvDestination.text.toString())
                intent.putExtra("tripDate", binding.tvTripDate.text.toString())
                intent.putExtra("departureTime", binding.tvDepartureTime.text.toString())
                intent.putExtra("total_price", binding.tvTotalPrice.text.toString())
                intent.putExtra("ticket_price", ticketPrice)
                intent.putExtra("trip", selectedTrip)
                intent.putExtra("seat_count", binding.tvSeatsSelected.text)

                startActivity(intent)
            } else {
                // chưa chọn ghế → hiện Toast và đứng yên
                Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu3, menu)
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.navigate_home -> {
                var intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }
}










