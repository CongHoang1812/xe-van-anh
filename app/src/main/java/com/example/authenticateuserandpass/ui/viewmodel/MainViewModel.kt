package com.example.authenticateuserandpass.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.authenticateuserandpass.data.firebaseModel.HotService
import com.example.authenticateuserandpass.data.firebaseModel.SliderModel
import com.example.authenticateuserandpass.data.firebaseModel.SliderModel2
import com.example.authenticateuserandpass.data.firebaseModel.UpdateNews
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel() : ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance("https://auth-user-pass-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val _banner2 = MutableLiveData<List<SliderModel2>>()
    private val _hotService = MutableLiveData<List<HotService>>()
    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _updateNews = MutableLiveData<List<UpdateNews>>()
    val  banners: LiveData<List<SliderModel>>
        get() = _banner
    val banners2: LiveData<List<SliderModel2>>
        get() = _banner2
    val hotService: LiveData<List<HotService>>
        get() = _hotService
    val updateNews: LiveData<List<UpdateNews>>
        get() = _updateNews


    fun loadBanners() {
        val Ref = firebaseDatabase.getReference("Banner")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Banner", "onDataChange called with ${snapshot.childrenCount} items")
                val lists = mutableListOf<SliderModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _banner.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun loadBanners2() {
        val Ref = firebaseDatabase.getReference("Introduce")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Banner", "onDataChange called with ${snapshot.childrenCount} items")
                val lists = mutableListOf<SliderModel2>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderModel2::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _banner2.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun loadHotServices() {
        val Ref = firebaseDatabase.getReference("HotService")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HotService", "onDataChange called with ${snapshot.childrenCount} items")
                val lists = mutableListOf<HotService>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(HotService::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _hotService.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    fun loadUpdateNew() {
        val Ref = firebaseDatabase.getReference("UpdateNews")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<UpdateNews>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(UpdateNews::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _updateNews.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}