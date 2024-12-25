package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import kotlinx.coroutines.launch

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
    val ctx = LocalContext.current

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
                            onHabitClick = { habitId ->
                                selectedHabitId = habitId
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
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

                            // TODO
//                            val habit by habitViewModel.getById(habitId).asLiveData().observeAsState()
//                            val favListItems by encouragementViewModel.getFromList(habitId).asLiveData().observeAsState()

//                            FavListDetailPane(
//                                navController = navController,
//                                favListId = habitId,
//                                habit = habit,
//                                encouragements = favListItems,
//                                isListAndDetailVisible = isListAndDetailVisible,
//                                onBackClick = {
//                                    scope.launch {
//                                        navigator.navigateBack()
//                                    }
//                                },
//                                onDelete = {
//                                    habit?.let {
//                                        habitViewModel.delete(it)
//                                        navController.navigateUp()
//                                        Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
//                                    }
//                                },
//                            )
                        }
                    }
                },
            )
        }
    }
}
