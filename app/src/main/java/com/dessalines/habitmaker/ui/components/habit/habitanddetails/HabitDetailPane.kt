package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Create
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
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitCheck
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.db.sampleHabit2
import com.dessalines.habitmaker.ui.components.common.AreYouSureDialog
import com.dessalines.habitmaker.ui.components.common.BackButton
import com.dessalines.habitmaker.ui.components.common.HabitChipsFlowRow
import com.dessalines.habitmaker.ui.components.common.HabitInfoChip
import com.dessalines.habitmaker.ui.components.common.LARGE_PADDING
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING
import com.dessalines.habitmaker.ui.components.common.SectionTitle
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.calendars.HabitCalendar
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.epochMillisToLocalDate
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun HabitDetailPane(
    habit: Habit,
    habitChecks: List<HabitCheck>,
    firstDayOfWeek: DayOfWeek,
    isListAndDetailVisible: Boolean,
    onHabitCheck: (LocalDate) -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

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
            MediumTopAppBar(
                title = { Text(titleText) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBackClick !== null) {
                        BackButton(
                            onBackClick = onBackClick,
                        )
                    }
                },
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
                                onEditClick()
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
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                item {
                    SectionTitle(stringResource(R.string.overview))
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
                        modifier = Modifier.padding(horizontal = LARGE_PADDING),
                    ) {
                        HabitTypeInfo(habit)
                        habitChecks.firstOrNull()?.let { HabitStartedInfo(it) }
                    }
                }
                item {
                    HabitChipsFlowRow(
                        habit = habit,
                        // Dont do any settings-related filtering for the detail pane
                        settings = null,
                        modifier = Modifier.padding(horizontal = LARGE_PADDING),
                    )
                }
                item {
                    HorizontalDivider()
                }
                item {
                    SectionTitle(stringResource(R.string.history))
                }
                item {
                    // Force rerender wher first day of week changes
                    key(firstDayOfWeek) {
                        HabitCalendar(
                            habitChecks = habitChecks,
                            firstDayOfWeek = firstDayOfWeek,
                            onClickDay = onHabitCheck,
                        )
                    }
                }
                item {
                    HorizontalDivider()
                }
                if (habit.notes?.isNotBlank() == true) {
                    item {
                        SectionTitle(stringResource(R.string.notes))
                        MarkdownText(
                            markdown = habit.notes,
                            modifier = Modifier.padding(horizontal = LARGE_PADDING),
                        )
                    }
                }
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

@Composable
fun HabitTypeInfo(habit: Habit) {
    val frequency = HabitFrequency.entries[habit.frequency]

    // If its more than 1, do a X times per Interval string
    val text =
        if (habit.timesPerFrequency > 1) {
            val times = habit.timesPerFrequency
            when (frequency) {
                HabitFrequency.Daily -> stringResource(R.string.x_times_per_day, times)
                HabitFrequency.Weekly -> stringResource(R.string.x_times_per_week, times)
                HabitFrequency.Monthly -> stringResource(R.string.x_times_per_month, times)
                HabitFrequency.Yearly -> stringResource(R.string.x_times_per_year, times)
            }
        } else {
            stringResource(frequency.resId)
        }

    HabitInfoChip(
        text = text,
        icon = Icons.Default.CalendarToday,
    )
}

@Composable
fun HabitStartedInfo(firstCheck: HabitCheck) {
    val startedDateStr = firstCheck.checkTime.epochMillisToLocalDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    HabitInfoChip(
        text =
            stringResource(
                R.string.started_on_x,
                startedDateStr,
            ),
        icon = Icons.Default.Create,
    )
}

@Composable
@Preview
fun HabitTypeInfoPreview() {
    HabitTypeInfo(sampleHabit)
}

@Composable
@Preview
fun HabitTypeInfoPreview2() {
    HabitTypeInfo(sampleHabit2)
}
