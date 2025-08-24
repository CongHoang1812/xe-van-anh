package com.example.authenticateuserandpass.ui.a_admin_ui.user

import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ItemUserBinding


class UserAdapter(
    private val users: MutableList<User> = mutableListOf(),
    private val menuListener: UserManagementActivity.OptionMenuClickListener,
    private val clickListener: UserManagementActivity.OnClickListener
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemUserBinding = ItemUserBinding
            .inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root, binding, menuListener, clickListener)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bindData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun updateData(users: List<User>) {
        this.users.clear()
        this.users.addAll(users)
    }

    class ViewHolder(
        view: View,
        binding: ItemUserBinding,
        private val menuListener: UserManagementActivity.OptionMenuClickListener,
        private val clickListener: UserManagementActivity.OnClickListener
    )  :RecyclerView.ViewHolder(view){
        private val binding : ItemUserBinding
        private lateinit var user: User

        init {
            this.binding = binding
            this.binding.btnOption.setOnClickListener { onOptionMenuClick() }
        }
        fun bindData(user: User) {
            this.user = user
            binding.textPhoneNumber.text = user.phone
            binding.textFullName.text = "${user.name} (${user.role})"
            Glide.with(binding.imgAvatar.context)
                .load(user.avatarUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_account)
                .into(binding.imgAvatar)
            binding.root.setOnClickListener {
                clickListener.onCLick(user.uid)
            }
        }
        private fun onOptionMenuClick() {
            val popupMenu = PopupMenu(binding.root.context, binding.btnOption)
            popupMenu.menuInflater.inflate(R.menu.user_item_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_view_detail -> {
                        menuListener.viewDetail(user.uid)
                        true
                    }
                    R.id.menu_update -> {
                        menuListener.update(user)
                        true
                    }
                    R.id.menu_delete -> {
                        menuListener.delete(user)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }


}