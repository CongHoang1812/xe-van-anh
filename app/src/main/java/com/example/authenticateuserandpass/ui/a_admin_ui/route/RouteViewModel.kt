package com.example.authenticateuserandpass.ui.a_admin_ui.route


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.data.repository.route.RouteRepository
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {
    private val repository = RouteRepository()

    private val _routes = MutableLiveData<List<Route>>()
    val routes: LiveData<List<Route>> = _routes

    private val _addSuccess = MutableLiveData<Boolean>()
    val addSuccess: LiveData<Boolean> = _addSuccess

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    fun loadRoutes() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val routeList = repository.getAllRoutes()
                _routes.value = routeList
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.deleteRoute(routeId)
                if (success) {
                    _deleteSuccess.value = true
                    loadRoutes() // Reload data after delete
                } else {
                    _error.value = "Không thể xóa tuyến đường"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xóa: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addRoute(route: Route) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.addRoute(route)
                if (success) {
                    _addSuccess.value = true
                } else {
                    _error.value = "Không thể thêm tuyến đường"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi thêm tuyến đường: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateRoute(route: Route) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.updateRoute(route.id, route)
                if (success) {
                    _updateSuccess.value = true
                } else {
                    _error.value = "Không thể cập nhật tuyến đường"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi cập nhật tuyến đường: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = ""
    }
}