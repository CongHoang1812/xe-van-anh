package com.example.authenticateuserandpass.ui.a_admin_ui.ticket

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.source.remote.TripDetails
import com.example.authenticateuserandpass.databinding.ActivityTicketManagementBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.user.UserManagementActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketManagementActivity : AppCompatActivity(), MenuProvider{
    private lateinit var binding: ActivityTicketManagementBinding
    private lateinit var viewModel: TicketViewModel
    private lateinit var ticketAdapter: TicketAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTicketManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbarUserManagement)
        supportActionBar?.title = "Quản lý vé"
        val menuHost : MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        setupUI()
        setupViewModel()
        setupRecyclerView()
        setupDatePicker()
        observeData()
        viewModel.loadAllTickets()
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbarUserManagement)
        supportActionBar?.title = "Quản lý vé"
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
    }
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[TicketViewModel::class.java]
    }
    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter(
            onEditClick = { tripDetails ->
                showUpdatePaymentStatusDialog(tripDetails)
            },
            onDeleteClick = { tripDetails ->
                AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa vé ${tripDetails.booking.id}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        viewModel.deleteTicket(tripDetails)
                        Toast.makeText(this, "Đã xóa vé: ${tripDetails.booking.id}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        )

        binding.recyclerViewTickets.apply {
            adapter = ticketAdapter
            layoutManager = LinearLayoutManager(this@TicketManagementActivity)
        }
    }
    private fun showUpdatePaymentStatusDialog(tripDetails: TripDetails) {
        val currentStatus = tripDetails.payment?.status ?: "Chưa thanh toán"
        val isAlreadyPaid = currentStatus == "Đã thanh toán"

        if (isAlreadyPaid) {
            Toast.makeText(this, "Vé này đã được thanh toán", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Vé: ${tripDetails.booking.id}\nKhách: ${tripDetails.user.name}\n\nXác nhận đã thanh toán?")
            .setPositiveButton("Đã thanh toán") { _, _ ->
                updatePaymentStatus(tripDetails, "Đã thanh toán")
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updatePaymentStatus(tripDetails: TripDetails, newStatus: String) {
        viewModel.updatePaymentStatus(tripDetails.booking.id, newStatus) { success ->
            if (success) {
                // Update local object
                tripDetails.payment?.status = newStatus

                // Force update adapter ngay lập tức
                ticketAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Đã cập nhật: Thanh toán thành công", Toast.LENGTH_SHORT).show()
                //refreshCurrentList()
            } else {
                Toast.makeText(this, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshCurrentList() {
        val selectedDate = binding.etSelectedDate.text.toString()
        if (selectedDate.isNotEmpty()) {
            // Nếu đang filter theo ngày, reload lại theo ngày
            viewModel.loadTicketsByDate(selectedDate)
        } else {
            // Nếu không, reload tất cả
            viewModel.loadAllTickets()
        }
    }

    private fun observeData() {
        viewModel.filteredTickets.observe(this) { tickets ->
            ticketAdapter.submitList(tickets)
            updateUIState(tickets)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Show/hide loading indicator if needed
            showLoading(isLoading)
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerViewTickets.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun updateUIState(tickets: List<TripDetails>) {
        // Đảm bảo loading indicator ẩn khi có dữ liệu
        binding.loadingIndicator.visibility = View.GONE
        binding.recyclerViewTickets.visibility = View.VISIBLE

        if (tickets.isEmpty()) {
            // Có thể thêm empty state nếu cần
            Toast.makeText(this, "Không có vé nào", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDatePicker() {
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.etSelectedDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày vé")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = dateFormat.format(Date(selection))
            binding.etSelectedDate.setText(selectedDate)
            viewModel.loadTicketsByDate(selectedDate)
            Toast.makeText(this, "Ngày đã chọn: $selectedDate", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu_4, menu)
        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Nhập số điện thoại..."
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@TicketManagementActivity, "Tìm: $query", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTickets(newText)
                return true
            }
        })
    }

//    private fun filterTickets(query: String?) {
//        if(query.isNullOrBlank()){
//            viewModel.allTickets.value?.let { originalList ->
//                ticketAdapter.submitList(originalList)
//                ticketAdapter.notifyDataSetChanged()
//            }
//        }else{
//            viewModel.allTickets.value?.let { originalList ->
//                val filteredList = originalList.filter { tripDetails ->
//                    tripDetails.user.phone.contains(query, ignoreCase = true)
//                }
//                ticketAdapter.submitList(filteredList)
//                ticketAdapter.notifyDataSetChanged()
//            }
//        }
//    }
private fun filterTickets(query: String?) {
    // Lấy danh sách hiện tại (có thể là tất cả vé hoặc vé theo ngày)
    val currentList = viewModel.filteredTickets.value ?: return

    if (query.isNullOrBlank()) {
        // Không có search query → hiển thị danh sách hiện tại
        ticketAdapter.submitList(currentList)
    } else {
        // Có search query → filter danh sách hiện tại theo số điện thoại
        val filteredList = currentList.filter { tripDetails ->
            tripDetails.user.phone.contains(query, ignoreCase = true)
        }
        ticketAdapter.submitList(filteredList)
    }
}







    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}