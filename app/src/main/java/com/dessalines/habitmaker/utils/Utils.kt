package com.dessalines.habitmaker.utils
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.HabitViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

const val TAG = "com.habitmaker"

const val GITHUB_URL = "https://github.com/dessalines/habit-maker"
const val MATRIX_CHAT_URL = "https://matrix.to/#/#habit-maker:matrix.org"
const val DONATE_URL = "https://liberapay.com/dessalines"
const val LEMMY_URL = "https://lemmy.ml/c/habitmaker"
const val MASTODON_URL = "https://mastodon.social/@dessalines"

fun openLink(
    url: String,
    ctx: Context,
) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    ctx.startActivity(intent)
}

fun Context.getPackageInfo(): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }

fun Context.getVersionCode(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getPackageInfo().longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo().versionCode
    }

fun writeData(
    ctx: Context,
    uri: Uri,
    data: String,
) {
    ctx.contentResolver.openOutputStream(uri)?.use {
        val bytes = data.toByteArray()
        it.write(bytes)
    }
}

fun nameIsValid(name: String): Boolean = name.isNotEmpty()

fun timesPerFrequencyIsValid(
    timesPerFrequency: Int,
    frequency: HabitFrequency,
): Boolean =
    when (frequency) {
        HabitFrequency.Daily -> IntRange(1, 1)
        HabitFrequency.Weekly -> IntRange(1, 7)
        HabitFrequency.Monthly -> IntRange(1, 28)
        HabitFrequency.Yearly -> IntRange(1, 365)
    }.contains(timesPerFrequency)

sealed interface SelectionVisibilityState<out Item> {
    object NoSelection : SelectionVisibilityState<Nothing>

    data class ShowSelection<Item>(
        val selectedItem: Item,
    ) : SelectionVisibilityState<Item>
}

fun Int.toBool() = this == 1

fun Boolean.toInt() = this.compareTo(false)

fun epochMillisToLocalDate(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

fun localDateToEpochMillis(date: LocalDate) = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Checks / toggles a habit for a given check time.
 *
 * If it already exists, it deletes the row in order to toggle it.
 *
 * returns true if successful / check, false for deleted check.
 */
fun checkHabitForDay(
    habitId: Int,
    checkTime: Long,
    habitCheckViewModel: HabitCheckViewModel,
): Boolean {
    val insert =
        HabitCheckInsert(
            habitId = habitId,
            checkTime = checkTime,
        )
    val success = habitCheckViewModel.insert(insert)

    // If its -1, that means that its already been checked for today,
    // and you actually need to delete it to toggle
    if (success == -1L) {
        habitCheckViewModel.deleteForDay(habitId, checkTime)
        return false
    } else {
        return true
    }
}

fun updateStatsForHabit(
    habitId: Int,
    habitViewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
) {
    // Read the history for that item
    val checks = habitCheckViewModel.listForHabitSync(habitId)
    val dateChecks = checks.map { epochMillisToLocalDate(it.checkTime) }
    val todayDate = LocalDate.now()

    val completed = dateChecks.lastOrNull() == todayDate

    val statsUpdate =
        HabitUpdateStats(
            id = habitId,
            points = 0,
            score = 0,
            streak = 0,
            completed = completed.toInt(),
        )
    habitViewModel.updateStats(statsUpdate)
}
