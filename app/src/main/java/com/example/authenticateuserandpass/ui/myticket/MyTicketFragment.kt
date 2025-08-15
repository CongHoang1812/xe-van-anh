package com.example.authenticateuserandpass.ui.myticket

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.google.firebase.auth.FirebaseAuth

class MyTicketFragment : Fragment(),UserTicketClickListener {

    private lateinit var viewModel: MyTicketViewModel
    private lateinit var adapter: UserTicketAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoTickets: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_ticket, container, false)

        initViews(view)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadTickets()

        return view
        // Inflate the layout for this fragment

    }

    private fun initViews(view: View)
    {
        recyclerView = view.findViewById(R.id.recyclerViewMyTickets)
        progressBar = view.findViewById(R.id.progressBarMyTickets)
        tvNoTickets = view.findViewById(R.id.tvNoTickets)
    }
    private fun setupViewModel() {
        val repository = TripRepositoryImpl()
        val factory = MyTicketViewModel.MyTicketViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MyTicketViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = UserTicketAdapter(emptyList(), this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
    private fun observeViewModel() {
        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            if (tickets.isEmpty()) {
                recyclerView.visibility = View.GONE
                tvNoTickets.visibility = View.VISIBLE
                tvNoTickets.text = "Bạn chưa có vé nào"
            } else {
                recyclerView.visibility = View.VISIBLE
                tvNoTickets.visibility = View.GONE
                adapter.updateTickets(tickets)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }
    private fun loadTickets() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            viewModel.loadUserTickets(userId)
        } ?: run {
            Toast.makeText(context, "Vui lòng đăng nhập để xem vé", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTicketClick(ticket: UserTicket, position: Int) {
        // Xử lý click vào vé - có thể navigate đến detail fragment
        Toast.makeText(context, "Clicked on ticket: ${ticket.ticketCode}", Toast.LENGTH_SHORT).show()
        val intent = Intent(context, MyTicketDetailActivity::class.java)
        intent.putExtra("TICKET_DATA", ticket)
        startActivity(intent)

        // Uncomment để navigate đến detail fragment nếu có
        // val fragment = TicketDetailFragment.newInstance(ticket)
        // parentFragmentManager.beginTransaction()
        //     .replace(R.id.fragment_container, fragment)
        //     .addToBackStack(null)
        //     .commit()
    }

    fun refreshTickets() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            viewModel.refreshTickets(userId)
        }
    }

    companion object {
        fun newInstance(): MyTicketFragment {
            return MyTicketFragment()
        }
    }

}
