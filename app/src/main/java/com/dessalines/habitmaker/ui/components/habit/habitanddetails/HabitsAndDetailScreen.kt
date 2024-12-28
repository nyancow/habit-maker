package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.checkHabitForDay
import com.dessalines.habitmaker.utils.localDateToEpochMillis
import com.dessalines.habitmaker.utils.updateStatsForHabit
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun HabitsAndDetailScreen(
    navController: NavController,
    habitViewModel: HabitViewModel,
    encouragementViewModel: EncouragementViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    id: Int?,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedHabitId: Int? by rememberSaveable { mutableStateOf(id) }
    val habits by habitViewModel.getAll.asLiveData().observeAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Companion.Expanded &&
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Companion.Expanded
    val isDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Companion.Expanded

    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    SharedTransitionLayout {
        AnimatedContent(targetState = isListAndDetailVisible, label = "simple sample") {
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    val currentSelectedHabitId = selectedHabitId
                    val selectionState =
                        if (isDetailVisible && currentSelectedHabitId != null) {
                            SelectionVisibilityState.ShowSelection(currentSelectedHabitId)
                        } else {
                            SelectionVisibilityState.NoSelection
                        }

                    AnimatedPane {
                        HabitsPane(
                            habits = habits,
                            snackbarHostState = snackbarHostState,
                            onHabitClick = { habitId ->
                                selectedHabitId = habitId
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            },
                            onHabitCheck = { habitId ->
                                val checkTime = localDateToEpochMillis(LocalDate.now())
                                val success = checkHabitForDay(habitId, checkTime, habitCheckViewModel)
                                updateStatsForHabit(habitId, habitViewModel, habitCheckViewModel)

                                // If successful, show a random encouragement
                                if (success) {
                                    encouragementViewModel.getRandomForHabit(habitId)?.let { encouragement ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(encouragement.content)
                                        }
                                    }
                                }
                            },
                            selectionState = selectionState,
                            isListAndDetailVisible = isListAndDetailVisible,
                            onCreateHabitClick = {
                                navController.navigate("createHabit")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            },
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        selectedHabitId?.let { habitId ->

                            val habit by habitViewModel
                                .getById(habitId)
                                .asLiveData()
                                .observeAsState()
                            val habitChecks by habitCheckViewModel
                                .listForHabit(habitId)
                                .asLiveData()
                                .observeAsState()

                            habit?.let { habit ->
                                HabitDetailPane(
                                    habit = habit,
                                    habitChecks = habitChecks.orEmpty(),
                                    isListAndDetailVisible = isListAndDetailVisible,
                                    onEditClick = {
                                        navController.navigate("editHabit/${habit.id}")
                                    },
                                    onBackClick = {
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            habitViewModel.delete(habit)
                                            navigator.navigateBack()
//                                        Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onHabitCheck = {
                                        val checkTime = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        checkHabitForDay(habit.id, checkTime, habitCheckViewModel)
                                        updateStatsForHabit(habit.id, habitViewModel, habitCheckViewModel)
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}
