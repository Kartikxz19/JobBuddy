package com.jainhardik120.jobbuddy.ui

import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes

sealed class UiEvent {
    data class Navigate(val route: AppRoutes) : UiEvent()
    data class ShowSnackBar(
        val message: String,
        val action: String? = null
    ) : UiEvent()

    data class ShowToast(val message: String) : UiEvent()
    data object NavigateBack : UiEvent()
}