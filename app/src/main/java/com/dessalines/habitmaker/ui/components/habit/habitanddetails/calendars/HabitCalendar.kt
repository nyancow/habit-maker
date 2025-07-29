package com.dessalines.habitmaker.ui.components.habit.habitanddetails.calendars

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.dessalines.habitmaker.db.HabitCheck
import com.dessalines.habitmaker.ui.components.common.MEDIUM_PADDING
import com.dessalines.habitmaker.utils.epochMillisToLocalDate
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import java.time.DayOfWeek
import com.dessalines.habitmaker.db.sampleHabitChecks
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HabitCalendar(
    habitChecks: List<HabitCheck>,
    firstDayOfWeek: DayOfWeek,
    onClickDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkDates = habitChecks.map { it.checkTime.epochMillisToLocalDate() }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth }
    val firstDayOfWeek = remember { firstDayOfWeek }

    val state =
        rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = firstDayOfWeek,
        )

    var showConfetti by remember { mutableStateOf(false) }
    var confettiPosition by remember { mutableStateOf<Position>(Position.Relative(0.5, 0.5)) }

    var lastClickedDate by remember { mutableStateOf<LocalDate?>(null) }
    var lastClickedPosition by remember { mutableStateOf<Offset?>(null) }

    // Keep previous checked set across recompositions
    var previousCheckedSet by remember { mutableStateOf<Set<LocalDate>>(checkDates.toSet()) }

    // Detect transition unchecked -> checked after data update
    LaunchedEffect(habitChecks) {
        val currentCheckedSet = habitChecks.map { it.checkTime.epochMillisToLocalDate() }.toSet()
        lastClickedDate?.let { clicked ->
            if (!previousCheckedSet.contains(clicked) && currentCheckedSet.contains(clicked)) {
                // It transitioned from unchecked to checked
                lastClickedPosition?.let { pos ->
                    confettiPosition = Position.Absolute(pos.x, pos.y)
                    showConfetti = true
                }
            }
        }

        previousCheckedSet = currentCheckedSet
        lastClickedDate = null
        lastClickedPosition = null
    }

    // Wrap the calendar in a Box so we can draw a full-screen overlay when needed
    Box(modifier = modifier) {
        HorizontalCalendar(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            monthHeader = { month ->
                MonthHeader(month)
            },
            dayContent = { calendarDay ->
                Day(
                    day = calendarDay,
                    // TODO probably a more efficient way to do this
                    // Maybe a hashmap of dates?
                    checked = checkDates.contains(calendarDay.date),
                    onClick = { clickedDay, windowOffset, wasChecked ->
                        // Store clicked info for post-update processing
                        lastClickedDate = clickedDay.date
                        lastClickedPosition = windowOffset

                        // Trigger caller logic (which will update habitChecks list)
                        onClickDay(clickedDay.date)
                    },
                    modifier = Modifier,
                )
            },
        )

        // Full-screen confetti overlay that does not block interaction
        if (showConfetti) {
            Popup(
                alignment = Alignment.Center,
                properties =
                    PopupProperties(
                        focusable = false,
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                    ),
                onDismissRequest = {
                    // This popup is dismissed automatically when the particle system ends
                    showConfetti = false
                },
            ) {
                // Match the full screen size inside the popup
                Box(modifier = Modifier.fillMaxSize()) {
                    KonfettiView(
                        modifier = Modifier.fillMaxSize(),
                        parties =
                            listOf(
                                Party(
                                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(30),
                                    spread = 360,
                                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                                    position = confettiPosition,
                                    fadeOutEnabled = true,
                                    timeToLive = 800L,
                                ),
                            ),
                        updateListener =
                            object : OnParticleSystemUpdateListener {
                                override fun onParticleSystemEnded(
                                    system: nl.dionsegijn.konfetti.core.PartySystem,
                                    activeSystems: Int,
                                ) {
                                    if (activeSystems == 0) {
                                        showConfetti = false
                                    }
                                }
                            },
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    calendarMonth: CalendarMonth,
    modifier: Modifier = Modifier,
) {
    val daysOfWeek = calendarMonth.weekDays.first().map { it.date.dayOfWeek }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
    ) {
        val locale = Locale.getDefault()
        Text(
            text = calendarMonth.yearMonth.month.getDisplayName(TextStyle.SHORT, locale),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textDecoration = TextDecoration.Underline,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    checked: Boolean,
    onClick: (CalendarDay, Offset, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    //  Only allow clicking dates in the past
    val allowedDate = day.date.isBefore(LocalDate.now().plusDays(1))
    val isToday = day.date == LocalDate.now()

    var coords: LayoutCoordinates? by remember { mutableStateOf(null) }

    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .onGloballyPositioned { coords = it }
                .pointerInput(allowedDate) {
                    detectTapGestures { offset ->
                        if (allowedDate) {
                            // Transform local offset to window coordinates
                            val windowOffset = coords?.let { it.positionInWindow() + offset } ?: offset
                            onClick(day, windowOffset, checked)
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )
        } else {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                // Underline today's date
                textDecoration =
                    if (isToday) {
                        TextDecoration.Underline
                    } else {
                        TextDecoration.None
                    },
                color =
                    if (allowedDate) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
        }
    }
}

@Preview
@Composable
fun HabitCalendarPreview() {
    HabitCalendar(
        habitChecks = sampleHabitChecks,
        firstDayOfWeek = DayOfWeek.SUNDAY,
        onClickDay = {},
    )
}
