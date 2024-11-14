package com.jainhardik120.jobbuddy.ui.presentation

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
        viewModelScope.launch {
            keyValueStorage.isLoggedIn.collect {
                isLoggedIn = it
            }
        }
    }

    fun logOut() {
        keyValueStorage.removeValue(KeyValueStorage.TOKEN_KEY)
    }

    override fun onCleared() {
        super.onCleared()
        keyValueStorage.stopListening()
    }
}