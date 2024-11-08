package com.jainhardik120.jobbuddy.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class KeyValueStorage(
    private val sharedPreferences: SharedPreferences
) {
    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isLoggedIn: Flow<Boolean>
        get() = _isLoggedIn.asStateFlow()

    private val sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                TOKEN_KEY -> {
                    _isLoggedIn.value = (sharedPreferences.getString(key, null) != null)
                }
            }
        }


    init {
        _isLoggedIn.value = checkToken()
        startListening()
    }

    private fun startListening() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    fun stopListening() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }


    companion object {
        const val TOKEN_KEY = "TOKEN"
    }

    fun getValue(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun storeValue(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    fun storeValue(key: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    fun removeValue(key: String) {
        sharedPreferences.edit()
            .remove(key)
            .apply()
    }

    fun clear() {
        sharedPreferences
            .edit()
            .clear()
            .apply()
    }


    fun checkToken(): Boolean {
        return contains(TOKEN_KEY)
    }

    fun getToken(): String? {
        return getValue(TOKEN_KEY)
    }

    fun saveLoginResponse(token: String) {
        storeValue(TOKEN_KEY, token)
    }

    fun contains(key: String): Boolean = sharedPreferences.contains(key)
}