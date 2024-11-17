package com.jainhardik120.jobbuddy.ui.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jainhardik120.jobbuddy.data.KeyValueStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val keyValueStorage: KeyValueStorage
) : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
        private set

    init {
        Log.d("TAG", "Token: ${keyValueStorage.getValue(KeyValueStorage.TOKEN_KEY)}")
        viewModelScope.launch {
            keyValueStorage.isLoggedIn.collect {
                isLoggedIn = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        keyValueStorage.stopListening()
    }
}