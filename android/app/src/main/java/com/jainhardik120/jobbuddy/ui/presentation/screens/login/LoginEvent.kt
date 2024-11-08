package com.jainhardik120.gatepay.ui.presentation.screens.login

import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest

sealed class LoginEvent {
    data class GoogleSignInButtonClick(
        val context: Context,
        val launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) : LoginEvent()
}