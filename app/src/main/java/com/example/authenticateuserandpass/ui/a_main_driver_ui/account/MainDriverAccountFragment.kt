package com.example.authenticateuserandpass.ui.a_main_driver_ui.account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.authenticateuserandpass.ui.MainActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.FragmentMainDriverAccountBinding
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.HomeShuttleDriverActivity
import com.google.firebase.auth.FirebaseAuth

class MainDriverAccountFragment : Fragment() {
    private lateinit var binding: FragmentMainDriverAccountBinding
    private  var auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainDriverAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.layoutLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            var intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}