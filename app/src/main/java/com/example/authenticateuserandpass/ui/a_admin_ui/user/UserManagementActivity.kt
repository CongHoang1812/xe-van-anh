package com.example.authenticateuserandpass.ui.a_admin_ui.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityUserManagementBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.user.createNew.CreateNewUserActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.user.updateUser.UpdateUserActivity

import kotlin.getValue

class UserManagementActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var adapter: UserAdapter
    private val viewModel: UserViewModel by viewModels{
        Factory(UserRepositoryImpl())
    }
    private var isAscending = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbarUserManagement)
        supportActionBar?.title = "Quản lý người dùng"
        val menuHost : MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        setUpRecyclerView()
        setupListeners()
    }

    private fun setupListeners() {
        binding.cardViewAddNewUser.setOnClickListener {
            val intent = Intent(this, CreateNewUserActivity::class.java)
            startActivity(intent)

        }
        binding.imageButton3.setOnClickListener {
            val intent = Intent(this, CreateNewUserActivity::class.java)
            startActivity(intent)

        }

    }

    private fun setUpRecyclerView() {
        adapter = UserAdapter(menuListener =  object : OptionMenuClickListener {
            override fun update(user: User) {
                val intent = Intent(this@UserManagementActivity, UpdateUserActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }

            override fun delete(user: User) {
                showDeleteConfirmDialog(user)
            }

            override fun viewDetail(id: String) {
                Toast.makeText(this@UserManagementActivity, "Xem chi tiết user với ID: $id", Toast.LENGTH_SHORT).show()
            }
        }, clickListener = object : OnClickListener {
            override fun onCLick(id: String) {
                Toast.makeText(this@UserManagementActivity, "Click vào user với ID: $id", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvUser.adapter = adapter
        val itemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            this,
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        binding.rvUser.addItemDecoration(itemDecoration)
        viewModel.users.observe(this) {
            adapter.updateData(it)
            adapter.notifyDataSetChanged()
            binding.progressBar2.visibility = android.view.View.GONE
        }
        binding.progressBar2.visibility = android.view.View.VISIBLE
    }



    private fun filterUsers(query: String?) {
        if (query.isNullOrBlank()) {
            // Hiển thị tất cả user khi không có từ khóa tìm kiếm
            viewModel.users.value?.let { originalList ->
                adapter.updateData(originalList)
                adapter.notifyDataSetChanged() // Thêm dòng này
            }
        } else {
            // Lọc danh sách theo tên hoặc số điện thoại
            viewModel.users.value?.let { originalList ->
                val filteredList = originalList.filter { user ->
                    user.name.contains(query, ignoreCase = true) ||
                            user.phone.contains(query, ignoreCase = true)
                }
                adapter.updateData(filteredList)
                adapter.notifyDataSetChanged() // Thêm dòng này
            }
        }
    }
    private fun showDeleteConfirmDialog(user: User) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Xác nhận xóa")
        builder.setMessage("Bạn có chắc chắn muốn xóa user \"${user.name}\" không?\n\nHành động này không thể hoàn tác.")

        builder.setPositiveButton("Xóa") { dialog, _ ->
            deleteUser(user)
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
    private fun deleteUser(user: User) {
        // Hiển thị progress bar
        binding.progressBar2.visibility = android.view.View.VISIBLE

        viewModel.deleteUser(user)

        // Observer để theo dõi kết quả xóa
        viewModel.users.observe(this) { userList ->
            binding.progressBar2.visibility = android.view.View.GONE
            Toast.makeText(this, "Đã xóa user \"${user.name}\" thành công", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu_4, menu)
        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Nhập tên user..."
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@UserManagementActivity, "Tìm: $query", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText)
                return true
            }
        })



    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId){
            R.id.menu_sort -> {
                toggleSortUsersByName()
                true
            }
            else ->  false
        }
    }


    private fun toggleSortUsersByName() {
        viewModel.users.value?.let { currentList ->
            val sortedList = if (isAscending) {
                currentList.sortedBy { user -> user.name.lowercase() }
            } else {
                currentList.sortedByDescending { user -> user.name.lowercase() }
            }

            adapter.updateData(sortedList)
            adapter.notifyDataSetChanged()

            val sortOrder = if (isAscending) "A-Z" else "Z-A"
            Toast.makeText(this, "Sắp xếp theo tên: $sortOrder", Toast.LENGTH_SHORT).show()

            isAscending = !isAscending // Đảo ngược thứ tự cho lần sort tiếp theo
        }
    }




    interface OnClickListener {
        fun onCLick(id: String)
    }

    interface OptionMenuClickListener {
        fun update(user: User)
        fun delete(user: User)
        fun viewDetail(id: String)
    }


}