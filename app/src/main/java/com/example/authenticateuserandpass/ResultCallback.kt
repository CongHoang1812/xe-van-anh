package com.example.authenticateuserandpass

interface ResultCallback<T> {
    fun onResult(result: T)
}