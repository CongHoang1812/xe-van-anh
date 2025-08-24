package com.example.authenticateuserandpass.data.model

data class CreateUserUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)
