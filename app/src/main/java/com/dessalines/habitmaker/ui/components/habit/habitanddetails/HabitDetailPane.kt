package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Encouragement
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitCheck
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.ui.components.common.AreYouSureDialog
import com.dessalines.habitmaker.ui.components.common.LARGE_PADDING
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING
import com.dessalines.habitmaker.ui.components.common.SimpleTopAppBar
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.calendars.HabitCalendar
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitDetailPane(
    habit: Habit,
    // TODO maybe I don't need these, because the edit is on a different page?
    encouragements: List<Encouragement>,
    habitChecks: List<HabitCheck>,
    isListAndDetailVisible: Boolean,
    onHabitCheck: (LocalDate) -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val ctx = LocalContext.current
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val showDeleteDialog = remember { mutableStateOf(false) }
    var showMoreDropdown by remember { mutableStateOf(false) }

    val (titleText, onBackClick) =
        if (isListAndDetailVisible) {
            Pair(habit.name, null)
        } else {
            Pair(habit.name, onBackClick)
        }

    AreYouSureDialog(
        show = showDeleteDialog,
        title = stringResource(R.string.delete),
        onYes = onDelete,
    )

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = titleText,
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior,
                actions = {
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.more_actions))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                showMoreDropdown = true
                            },
                        ) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.more_actions),
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showMoreDropdown,
                        onDismissRequest = { showMoreDropdown = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_habit)) },
                            onClick = {
                                showMoreDropdown = false
                                showDeleteDialog.value = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.edit_habit),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                showMoreDropdown = false
                                showDeleteDialog.value = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                )
                            },
                        )
                    }
                },
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                HabitCalendar(
                    habitChecks = habitChecks,
                    onClickDay = onHabitCheck,
                )
            }
        },
    )
}

@Composable
fun HabitDetails(habit: Habit) {
    if (!habit.notes.isNullOrBlank()) {
        HorizontalDivider()
        MarkdownText(
            markdown = habit.notes,
            linkColor = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .padding(
                        top = 0.dp,
                        bottom = SMALL_PADDING,
                        start = LARGE_PADDING,
                        end = LARGE_PADDING,
                    ).fillMaxWidth(),
        )
    }
}

@Composable
@Preview
fun HabitDetailsPreview() {
    HabitDetails(sampleHabit)
}
