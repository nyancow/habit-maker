package com.dessalines.habitmaker.utils

import com.dessalines.habitmaker.db.HabitCheck
import okhttp3.internal.toImmutableList
import java.time.Duration
import java.time.LocalDate

data class Streak(
    val begin: LocalDate,
    val end: LocalDate,
)

/**
 * Gives the number of days for a streak
 */
fun Streak.duration(): Long =
    Duration
        .between(
            this.begin.atStartOfDay(),
            this.end.atStartOfDay(),
        ).toDays()
        .plus(1)

fun currentStreak(
    streaks: List<Streak>,
    todayDate: LocalDate,
): Long {
    val currentStreak =
        streaks.lastOrNull()?.let {
            if (it.end == todayDate) {
                it.duration()
            } else {
                0
            }
        } ?: 0
    return currentStreak
}

fun calculateStreaks(habitChecks: List<HabitCheck>): List<Streak> {
    val dates = habitChecks.map { it.checkTime.epochMillisToLocalDate() }.sortedDescending()

    if (dates.isEmpty()) {
        return emptyList()
    }

    var begin = dates[0]
    var end = dates[0]

    val streaks = mutableListOf<Streak>()

    for (i in 1 until dates.size) {
        val current = dates[i]
        // TODO this needs to factor in interval and freq iterations
        if (current == begin.minusDays(1)) {
            begin = current
        } else {
            streaks.add(Streak(begin, end))
            begin = current
            end = current
        }
    }
    streaks.add(Streak(begin, end))

    return streaks.reversed().toImmutableList()
}

/**
 * Get a bonus points for each day that the streak is long.
 *
 * Called nth triangle number:
 * https://math.stackexchange.com/a/593320
 */
fun calculatePoints(streaks: List<Streak>): Long {
    var points = 0L

    streaks.forEach {
        val duration = it.duration()
        points += (duration * duration + duration) / 2
    }
    return points
}

/**
 * The percent complete score.
 *
 * Calculated using the # of times you've done it.
 */
fun calculateScore(
    habitChecks: List<HabitCheck>,
    completedCount: Int,
): Int = (100 * habitChecks.size).div(completedCount)
