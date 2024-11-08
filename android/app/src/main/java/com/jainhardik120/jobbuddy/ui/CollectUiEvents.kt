package com.jainhardik120.jobbuddy.ui

import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.Flow

@Composable
fun CollectUiEvents(
    navHostController: NavHostController,
    events: Flow<UiEvent>,
    hostState: SnackbarHostState?
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit, block = {
        events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navHostController.navigate(event.route)
                }

                is UiEvent.ShowSnackBar -> {
                    hostState?.showSnackbar(event.message)
                }

                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                UiEvent.NavigateBack -> {
                    navHostController.navigateUp()
                }
            }
        }
    }
    )
}