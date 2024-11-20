package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.data.local.entity.FlashCard
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState

) {
    val viewModel: JobDetailsViewModel = hiltViewModel()
    val state by viewModel.state
    CollectUiEvents(navController, viewModel.uiEvent, snackbarHostState)

    val sheetState = rememberModalBottomSheetState()

    if (state.openEvaluationSheet) {
        ModalBottomSheet({
            viewModel.setEvaluationSheet(false)
        }, sheetState = sheetState, dragHandle = {}, shape = RoundedCornerShape(0.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                MarkdownText(state.profileEvaluation)
            }
        }
    }


    val interviewExperienceSheet = rememberModalBottomSheetState()

    if (state.interviewExperienceSheet) {
        ModalBottomSheet({
            viewModel.setInterviewInsightSheet(false)
        }, sheetState = interviewExperienceSheet, dragHandle = {}, shape = RoundedCornerShape(0.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                MarkdownText(state.interviewExperience)
            }
        }
    }

    val context = LocalContext.current
    LazyColumn(
        Modifier.Companion
            .fillMaxSize()
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomButton(
                    { viewModel.generateTailoredResume(context) },
                    "Generate Resume",
                    icon = Icons.Default.Create
                )
                CustomButton(
                    { viewModel.takeInterviewButton() },
                    "Take virtual interview",
                    Icons.Default.Notifications
                )
                CustomButton(
                    { viewModel.setEvaluationSheet(true) },
                    "Check Profile Score",
                    Icons.Default.Star
                )
                CustomButton(
                    { viewModel.setInterviewInsightSheet(true)},
                    "Interview Experience",
                    Icons.Default.Star
                )
            }
        }
        item {
            HorizontalDivider(Modifier.fillMaxWidth())
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.generateStudyPlan() }) {
                    Text(if (state.studyPlan.isEmpty()) "Generate Flash Cards" else "Regenerate Flash Cards")
                }
            }
        }
        state.studyPlan.forEach { item ->
            itemsIndexed(item.flashCards) { index, it ->
                FlashCard(it = it, bookMarkButton = {
                    IconButton(onClick = {
                        viewModel.bookMarkFlashCard(it)
                    }) {
                        Icon(Icons.Default.AccountBox, "Bookmark")
                    }
                })
            }
        }
    }
}

@Composable
fun FlashCard(
    bookMarkButton: @Composable () -> Unit = {},
    it: FlashCard
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    it.question,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                bookMarkButton()
            }
            Spacer(Modifier.height(8.dp))
            Text(it.answer)
            Spacer(Modifier.height(8.dp))
            Text(
                text = it.skill,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun RowScope.CustomButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector
) {
    Column(
        Modifier.weight(1f),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            Modifier
                .semantics { role = Role.Button }
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize(1f)) {
                Icon(icon, text, Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}