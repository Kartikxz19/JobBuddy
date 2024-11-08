package com.jainhardik120.jobbuddy.ui.presentation.screens.login

import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.jainhardik120.gatepay.ui.presentation.screens.login.LoginEvent
import com.jainhardik120.jobbuddy.Constants
import com.jainhardik120.jobbuddy.data.remote.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.data.KeyValueStorage
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: JobBuddyAPI,
    private val keyValueStorage: KeyValueStorage
) : BaseViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private lateinit var oneTapClient: SignInClient

    private var _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    override fun apiPreExecuting() {
        _state.value = _state.value.copy(loading = true)
    }

    override fun apiDoneExecuting() {
        _state.value = _state.value.copy(loading = false)
    }


    private fun launchOneTapIntent(
        context: Context,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        viewModelScope.launch {
            oneTapClient = Identity.getSignInClient(context)
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(Constants.GOOGLE_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()
            oneTapClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    launcher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    sendUiEvent(UiEvent.ShowToast("Error while logging in using Google : ${e.localizedMessage}"))
                }
            }.addOnFailureListener { e ->
                sendUiEvent(UiEvent.ShowToast("Error while logging in using Google : ${e.localizedMessage}"))
            }
        }
    }

    fun handleIntentResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }
        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
        val idToken = credential.googleIdToken
        if (idToken != null) {
            makeApiCall({
                api.loginGoogle(GoogleLoginRequest(idToken))
            }, onSuccess = { response ->
                keyValueStorage.saveLoginResponse(response.token)
            })
        } else {
            sendUiEvent(UiEvent.ShowToast("Error while logging in using Google"))
        }
    }


    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.GoogleSignInButtonClick -> {
                launchOneTapIntent(event.context, event.launcher)
            }
        }
    }
}


