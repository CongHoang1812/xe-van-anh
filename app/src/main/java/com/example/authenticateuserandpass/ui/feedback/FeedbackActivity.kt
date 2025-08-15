package com.example.authenticateuserandpass.ui.feedback

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityFeedbackBinding
import com.example.authenticateuserandpass.ui.myticket.MyTicketDetailActivity
import com.example.authenticateuserandpass.ui.myticket.MyTicketViewModel
import com.example.authenticateuserandpass.ui.myticket.UserTicketAdapter
import com.google.firebase.auth.FirebaseAuth

class FeedbackActivity : AppCompatActivity(), OnTripFeedbackListener {
    private lateinit var binding : ActivityFeedbackBinding
    private lateinit var viewModel: MyTicketViewModel
    private lateinit var adapter: FeedbackAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadTickets()
    }
    private fun setupViewModel(){
        val repository = TripRepositoryImpl()
        val factory = MyTicketViewModel.MyTicketViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MyTicketViewModel::class.java]
    }
    private fun setupRecyclerView() {
        adapter = FeedbackAdapter(emptyList(), this, supportFragmentManager)
        binding.rvFeedback.adapter = adapter

    }
    private fun observeViewModel(){
        viewModel.tickets.observe(this) { tickets ->
            val completedTickets = tickets.filter { ticket ->
                ticket.tripStatus == "Đã đi"
            }
            adapter.updateTickets(completedTickets)
        }

    }

    private fun loadTickets() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            viewModel.loadUserTickets(userId)
        } ?: run {
            Toast.makeText(this, "Vui lòng đăng nhập để xem vé", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFeedbackClick(
        ticket: UserTicket,
        position: Int
    ) {
        val intent = Intent(this, MyTicketDetailActivity::class.java)
        intent.putExtra("TICKET_DATA", ticket)
        startActivity(intent)
    }


}