package com.dessalines.habitmaker.utils

import androidx.annotation.StringRes
import com.dessalines.habitmaker.R

enum class ThemeMode(
    @StringRes val resId: Int,
) {
    System(R.string.system),
    Light(R.string.light),
    Dark(R.string.dark),
}

enum class ThemeColor(
    @StringRes val resId: Int,
) {
    Dynamic(R.string.dynamic),
    Green(R.string.green),
    Pink(R.string.pink),
}

enum class HabitFrequency(
    @StringRes val resId: Int,
) {
    Daily(R.string.daily),
    Weekly(R.string.weekly),
    Monthly(R.string.monthly),
    Yearly(R.string.yearly),
}

enum class HabitSort(
    @StringRes val resId: Int,
) {
    Name(R.string.name),
    Points(R.string.points),
    Score(R.string.score),
    Streak(R.string.streak),
    /**
     * Whether its completed or not.
     */
    Status(R.string.status),
    DateCreated(R.string.date_created),
}

enum class HabitSortOrder(
    @StringRes val resId: Int,
) {
    Ascending(R.string.ascending),
    Descending(R.string.descending),
}

