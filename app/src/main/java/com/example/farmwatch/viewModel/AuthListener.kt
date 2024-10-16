package com.example.farmwatch.viewModel

import androidx.lifecycle.LiveData

interface AuthListener {
    fun onStarted()
    fun onSuccess(authRepo: LiveData<String>)
    fun onFailure(message: String)
}