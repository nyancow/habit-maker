package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.ui.components.common.LARGE_PADDING
import com.dessalines.habitmaker.ui.components.common.SimpleTopAppBar
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.toBool
import kotlin.collections.orEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitsPane(
    habits: List<Habit>?,
    onHabitClick: (habitId: Int) -> Unit,
    onHabitCheck: (habitId: Int) -> Unit,
    onCreateHabitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    selectionState: SelectionVisibilityState<Int>,
    isListAndDetailVisible: Boolean,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val title =
        if (!isListAndDetailVisible) stringResource(R.string.app_name) else stringResource(R.string.habits)

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = title,
                scrollBehavior = scrollBehavior,
                actions = {
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.settings))
                        },
                    ) {
                        IconButton(
                            onClick = onSettingsClick,
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        modifier = Modifier.Companion.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { padding ->
            Box(
                modifier =
                    Modifier.Companion
                        .padding(padding)
                        .imePadding(),
            ) {
                LazyColumn(
                    state = listState,
                ) {
                    items(habits.orEmpty()) { habit ->
                        val selected =
                            when (selectionState) {
                                is SelectionVisibilityState.ShowSelection -> selectionState.selectedItem == habit.id
                                else -> false
                            }

                        HabitRow(
                            habit = habit,
                            onClick = { onHabitClick(habit.id) },
                            onCheck = { onHabitCheck(habit.id) },
                            selected = selected,
                        )
                    }
                    item {
                        if (habits.isNullOrEmpty()) {
                            Text(
                                text = stringResource(R.string.no_habits),
                                modifier = Modifier.Companion.padding(horizontal = LARGE_PADDING),
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            BasicTooltipBox(
                positionProvider = tooltipPosition,
                state = rememberBasicTooltipState(isPersistent = false),
                tooltip = {
                    ToolTip(stringResource(R.string.create_habit))
                },
            ) {
                FloatingActionButton(
                    modifier = Modifier.Companion.imePadding(),
                    onClick = onCreateHabitClick,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.create_habit),
                    )
                }
            }
        },
    )
}

@Composable
fun HabitRow(
    habit: Habit,
    selected: Boolean = false,
    onCheck: () -> Unit,
    onClick: () -> Unit,
) {
    val containerColor =
        if (!selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    val (icon, tint) =
        if (habit.completed.toBool()) {
            Pair(Icons.Outlined.Check, MaterialTheme.colorScheme.onSurface)
        } else {
            Pair(Icons.Outlined.Close, MaterialTheme.colorScheme.outline)
        }

    ListItem(
        headlineContent = {
            Text(habit.name)
        },
        supportingContent = {
            Text("completed = ${habit.completed}")
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier =
            Modifier.Companion.clickable {
                onClick()
            },
        trailingContent = {
            IconButton(
                onClick = onCheck,
            ) {
                Icon(
                    imageVector = icon,
                    tint = tint,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
@Preview
fun HabitRowPreview() {
    HabitRow(
        habit = sampleHabit,
        onCheck = {},
        onClick = {},
    )
}
