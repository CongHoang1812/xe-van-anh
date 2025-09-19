package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.tabFragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.HomeShuttleDriverActivity
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.adapter.BookingAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class DaHoanThanhFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookingAdapter
    private val firestore = Firebase.firestore
    private val shuttleDriverId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private val bookingList = mutableListOf<Booking>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_da_hoan_thanh, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv_complete)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadComplete()
    }
    private fun loadComplete() {
        firestore.collection("bookings")
            .whereEqualTo("pickup_driver_id", shuttleDriverId)
            .whereIn("status", listOf("Đã đón", "Đã trả"))
            .addSnapshotListener { bookingSnap, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (bookingSnap == null || bookingSnap.isEmpty) {
                    adapter = BookingAdapter(emptyList(), { _, _ -> }, {}, {})
                    recyclerView.adapter = adapter
                    return@addSnapshotListener
                }

                val bookings = bookingSnap.toObjects(Booking::class.java)
                val userIds = bookings.mapNotNull { it.user_id }.distinct()

                if (userIds.isEmpty()) {
                    adapter = BookingAdapter(emptyList(), { _, _ -> }, {}, {})
                    recyclerView.adapter = adapter
                    return@addSnapshotListener
                }

                firestore.collection("users")
                    .whereIn("uid", userIds)
                    .get()
                    .addOnSuccessListener { userSnap ->
                        val users = userSnap.toObjects(User::class.java)
                        val userMap = users.associateBy { it.uid }

                        val paired = bookings.mapNotNull { booking ->
                            val user = userMap[booking.user_id]
                            if (user != null) booking to user else null
                        }

                        adapter = BookingAdapter(
                            bookings = paired,
                            onDirectionClick = { booking, _ ->
                                val gmmIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q=${booking.pickup_location}")
                                )
                                gmmIntent.setPackage("com.google.android.apps.maps")
                                startActivity(gmmIntent)
                            },
                            onCallClick = { user ->
                                val callIntent = Intent(Intent.ACTION_DIAL)
                                callIntent.data = Uri.parse("tel:${user.phone}")
                                startActivity(callIntent)
                            },
                            onPickedUpClick = { booking ->
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Xác nhận")
                                    .setMessage("Bạn chắc chắn đã đón khách?")
                                    .setPositiveButton("Đã đón") { _, _ ->
                                        firestore.collection("bookings")
                                            .document(booking.id)
                                            .update("status", "Đã đón")
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Đã cập nhật trạng thái",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Không cần gọi loadKhachDon nữa vì realtime đã lắng nghe
                                                (activity as? HomeShuttleDriverActivity)?.reloadAllTabs()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Lỗi khi cập nhật",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .setNegativeButton("Huỷ", null)
                                    .show()
                            }
                        )

                        recyclerView.adapter = adapter
                    }
            }
    }
    fun loadHoanThanh() {
        firestore.collection("bookings")
            .whereEqualTo("driverId", shuttleDriverId)
            .whereIn("status", listOf("Đã đón", "Đã trả"))  // hoặc "done" tùy bạn
            .get()
            .addOnSuccessListener { querySnapshot ->
                bookingList.clear()
                for (doc in querySnapshot.documents) {
                    val booking = doc.toObject(Booking::class.java)
                    if (booking != null) {
                        bookingList.add(booking)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi tải vé đã hoàn thành: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}