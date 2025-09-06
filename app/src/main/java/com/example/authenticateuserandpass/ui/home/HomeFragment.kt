package com.example.authenticateuserandpass.ui.home

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.firebaseModel.HotService
import com.example.authenticateuserandpass.data.firebaseModel.RouteCardData
import com.example.authenticateuserandpass.data.firebaseModel.SliderModel
import com.example.authenticateuserandpass.data.firebaseModel.SliderModel2
import com.example.authenticateuserandpass.data.firebaseModel.UpdateNews
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.FragmentHomeBinding
import com.example.authenticateuserandpass.ui.chatbot.ChatbotBottomSheetFragment
import com.example.authenticateuserandpass.ui.dialog.BottomSheetLocationFragment
import com.example.authenticateuserandpass.ui.dialog.OnLocationSelectedListener
import com.example.authenticateuserandpass.ui.findticket.FindTicketActivity
import com.example.authenticateuserandpass.ui.home.adapter.HotServiceAdapter
import com.example.authenticateuserandpass.ui.home.adapter.IntroduceAdapter
import com.example.authenticateuserandpass.ui.home.adapter.RouteCardAdapter
import com.example.authenticateuserandpass.ui.home.adapter.SliderAdapter
import com.example.authenticateuserandpass.ui.home.adapter.UpdateNewAdapter
import com.example.authenticateuserandpass.ui.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


class HomeFragment : Fragment() {
    private var user : User = User()
    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var sliderRunnable: Runnable
    private var sliderItems: List<SliderModel> = emptyList()
    private var introduceItems : List<SliderModel2> = emptyList()
    private var hotServiceItems : List<HotService> = emptyList()
    private var updateNewsItems : List<UpdateNews> = emptyList()
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by viewModels()
    val routeList = listOf(
        RouteCardData("TP Thanh Hóa", "Hà Nội", R.drawable.ht_hn),
        RouteCardData("Triệu Sơn", "Hà Nội", R.drawable.ts_hn),
        RouteCardData("TP Thanh Hóa", "Hà Nội", R.drawable.ht_hn),
        RouteCardData("Triệu Sơn", "Hà Nội", R.drawable.ts_hn),
        RouteCardData("TP Thanh Hóa", "Hà Nội", R.drawable.ht_hn),
        RouteCardData("Triệu Sơn", "Hà Nội", R.drawable.ts_hn),
        RouteCardData("TP Thanh Hóa", "Hà Nội", R.drawable.ht_hn),
        RouteCardData("Triệu Sơn", "Hà Nội", R.drawable.ts_hn),
        RouteCardData("BX Nước Ngầm", "Thiệu Hóa", R.drawable.ts_hn)
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        initBanner()
        initIntroduce()
        initHotService()
        initUpdateNews()
        setupListener()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvRoute.adapter = RouteCardAdapter(requireContext(), routeList)
        binding.rvRoute1.adapter = RouteCardAdapter(requireContext(), routeList)

    }

    private fun initHotService(){
        val adapter = HotServiceAdapter()
        binding.rvHostService.adapter = adapter

        viewModel.hotService.observe(viewLifecycleOwner) { items ->
            hotServiceItems = items
            adapter.setData(items) // Bạn cần có hàm setData trong adapter
        }

        viewModel.loadHotServices()
    }

    private fun initUpdateNews(){
        val adapter = UpdateNewAdapter()
        binding.rvUpdateNew.adapter = adapter

        viewModel.updateNews.observe(viewLifecycleOwner) { items ->
            updateNewsItems = items
            adapter.setData(items) // Bạn cần có hàm setData trong adapter
        }

        viewModel.loadUpdateNew()
    }

    private fun initBanner() {

        binding.progressBarSlider.visibility = View.VISIBLE
        viewModel.banners.observe(viewLifecycleOwner, Observer {
            banners(it)
            binding.progressBarSlider.visibility = View.GONE

        })
        viewModel.loadBanners()
    }

    private fun initIntroduce() {

        //binding.progressBarSlider.visibility = View.VISIBLE
        viewModel.banners2.observe(viewLifecycleOwner, Observer {
            introduces(it)
            //binding.progressBarSlider.visibility = View.GONE

        })
        viewModel.loadBanners2()
    }

    private fun introduces(items: List<SliderModel2>) {
        introduceItems = items
        binding.rvIntroduce.adapter = IntroduceAdapter(items)
    }


    private fun banners(image: List<SliderModel>) {
        sliderItems = image
        binding.viewPager2.adapter=SliderAdapter(image,binding.viewPager2)
        binding.viewPager2.clipToPadding=false
        binding.viewPager2.clipChildren=false
        binding.viewPager2.offscreenPageLimit=3
        binding.viewPager2.getChildAt(0).overScrollMode=RecyclerView.OVER_SCROLL_NEVER
        // Auto-slide logic
        sliderRunnable = Runnable {
            binding.viewPager2.currentItem = (binding.viewPager2.currentItem + 1) % sliderItems.size
        }

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000) // 3 giây
            }
        })



        // Bắt đầu slide ban đầu
        sliderHandler.postDelayed(sliderRunnable, 5000)
        val compositePageTransformer=CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }
        binding.viewPager2.setPageTransformer(compositePageTransformer)

        if(image.size>1){
            binding.dotIndicator.visibility=View.VISIBLE
            binding.dotIndicator.attachTo(binding.viewPager2)
        }
    }
    private fun setupListener (){
        binding.btnOneWay.setOnClickListener {
            binding.btnOneWay.setBackgroundResource(R.drawable.btn_active)
            binding.btnRoundTrip.setBackgroundResource(R.drawable.btn_no_active)
            binding.etReturnDate.visibility=View.GONE
        }
        binding.btnRoundTrip.setOnClickListener {
            binding.btnOneWay.setBackgroundResource(R.drawable.btn_no_active)
            binding.btnRoundTrip.setBackgroundResource(R.drawable.btn_active)
            binding.etReturnDate.visibility=View.VISIBLE
        }
        binding.btnChange.setOnClickListener {
            if(binding.etDeparture.text.toString().isEmpty() && binding.etDestination.text.toString().isEmpty()){
                return@setOnClickListener
            }else{
                var temp=binding.etDeparture.text.toString()
                binding.etDeparture.setText(binding.etDestination.text.toString())
                binding.etDestination.setText(temp)
            }
        }
        binding.etDepartureDate.setOnClickListener {
            showDatePicker(binding.etDepartureDate)
        }
        binding.etReturnDate.setOnClickListener {
            showDatePicker(binding.etReturnDate)
        }
        binding.etDeparture.setOnClickListener {
            val dialog = BottomSheetLocationFragment()
            dialog.listener = object : OnLocationSelectedListener{
                override fun onLocationSelected(location: String) {
                    binding.etDeparture.setText(location)
                }
            }
            dialog.show(parentFragmentManager, "ChonDiemDialog")
        }
        binding.etDestination.setOnClickListener {
            val dialog = BottomSheetLocationFragment()
            dialog.listener = object : OnLocationSelectedListener{
                override fun onLocationSelected(location: String) {
                    binding.etDestination.setText(location)
                }
            }
            dialog.show(parentFragmentManager, "ChonDiemDialog")
        }
        binding.hambugerMenu2.setOnClickListener {
            (activity as? HomeActivity)?.openDrawer()
        }
        binding.imageView13.setOnClickListener {
            val chatbotFragment = ChatbotBottomSheetFragment.newInstance()
            chatbotFragment.show(parentFragmentManager, ChatbotBottomSheetFragment.TAG)
        }

        binding.btnSearch.setOnClickListener {
            val departure = binding.etDeparture.text.toString()
            val destination = binding.etDestination.text.toString()
            val departureDate = binding.etDepartureDate.text.toString()
            val returnDate = binding.etReturnDate.text.toString()
            var intent = Intent(requireContext(), FindTicketActivity::class.java)
            intent.putExtra(EDIT_DEPARTURE,departure)
            intent.putExtra(EDIT_DESTINATION,destination)
            intent.putExtra(EDIT_DEPARTURE_DATE,departureDate)
            intent.putExtra(EDIT_RETURN_DATE,returnDate)
            startActivity(intent)
        }
    }



//    btnChonDiem.setOnClickListener {
//        val dialog = BottomSheetLocationFragment()
//        dialog.listener = object : OnLocationSelectedListener {
//            override fun onLocationSelected(location: String) {
//                tvSelectedLocation.text = "Đã chọn: $location"
//            }
//        }
//        dialog.show(supportFragmentManager, "BottomSheetLocation")
//    }
     fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            editText.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(selectedDate)
            },
            year, month, day
        )
        // ✅ Không cho chọn ngày trong quá khứ
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
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
        binding.greetingText.text = "Xin chào: ${user.name}"
        // Hiển thị ảnh đại diện
        if (user.avatarUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.avatarUrl)
                .into(binding.avatarImage)
        }
    }

    companion object{
        const val EDIT_DEPARTURE = "EDIT_DEPARTURE"
        const val EDIT_DESTINATION = "EDIT_DESTINATION"
        const val EDIT_DEPARTURE_DATE = "EDIT_DEPARTURE_DATE"
        const val EDIT_RETURN_DATE = "EDIT_RETURN_DATE"
    }



}