package com.example.authenticateuserandpass.ui.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.ui.MainActivity
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.FragmentAccountBinding
import com.example.authenticateuserandpass.ui.account.viewprofile.ViewProfileActivity
import com.example.authenticateuserandpass.ui.feedback.FeedbackActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java


class AccountFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private  var user : User = User()
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        setOnClickListener()
        loadUserData()
    }

    private fun setOnClickListener() {
        binding.tvUpdate.setOnClickListener {
            var intent = Intent(requireContext(), ViewProfileActivity::class.java)
            startActivity(intent)
        }
        binding.layoutAboutUs.setOnClickListener {
            val url = "https://xevananh.com/vi/gioi-thieu-nha-xe-van-anh.html?id=4f4ec44b-2920-4c3f-8227-d8dca47f0271" // URL bạn muốn mở
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.bookingIns.setOnClickListener {
            val url = "https://xevananh.com/vi/1huong-dan-dat-ve.html?id=772a23af-93fd-4767-b394-e9b34286a829" // URL bạn muốn mở
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.Support.setOnClickListener {
            val url = "https://xevananh.com/vi/contact" // URL bạn muốn mở
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.TripReview.setOnClickListener {
            val intent = Intent(requireContext(), FeedbackActivity::class.java)
            startActivity(intent)
        }
        binding.DeleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
        binding.Logout.setOnClickListener {

            auth.signOut()
            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            var intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    user = document.toObject(User::class.java) ?: User()
                    showUserData()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUserData() {
        // Hiển thị ảnh đại diện
        if (user.avatarUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.avatarUrl)
                .into(binding.imgAvatar)
        }
        binding.tvName.text = user.name
    }


}