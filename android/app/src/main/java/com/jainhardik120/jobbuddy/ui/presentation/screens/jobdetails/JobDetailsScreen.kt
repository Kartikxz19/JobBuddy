package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun JobDetailsScreen(
    navController: NavController
) {
    val viewModel: JobDetailsViewModel = hiltViewModel()
    val state by viewModel.state

    Scaffold(
        Modifier.Companion.imePadding(), contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .systemBarsPadding()
                .padding(8.dp)
        ) {
            item {
                Button({ viewModel.generateStudyPlan() }) { Text("Generate Study Plan") }
            }
            item {
                Button({
                    viewModel.generateTailoredResume()
                }) { Text("Generate Tailored Resume") }
            }
            itemsIndexed(state.studyPlan) { index, item ->
                Column(Modifier.fillMaxWidth()) {
                    Text(item.skill)
                    item.flashCards.forEach {
                        Text(it.question)
                        Text(it.answer)
                    }
                }
            }
        }
    }
}