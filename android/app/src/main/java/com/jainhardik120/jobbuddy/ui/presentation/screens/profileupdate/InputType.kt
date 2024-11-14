package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

import kotlinx.serialization.Serializable
import java.util.Calendar
import java.text.DateFormatSymbols


data class InputFieldType(
    val name: String,
    val type: InputType
)

sealed class InputType {
    abstract fun displayName(): String

    data class Text(val value: String = "") : InputType() {
        override fun displayName(): String {
            return value
        }
    }

    data class Number(val value: Int = 0) : InputType() {
        override fun displayName(): String {
            return "$value"
        }
    }

    @Serializable
    data class Date(
        val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
    ) : InputType() {
        override fun displayName(): String {
            val monthNames = DateFormatSymbols().months
            val monthName = if (month in 1..12) monthNames[month - 1] else "Invalid Month"
            return "$monthName, $year"
        }
    }

}
