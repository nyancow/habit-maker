package com.dessalines.habitmaker.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.utils.HabitFrequency

@Composable
fun SectionTitle(title: String) =
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = LARGE_PADDING),
        style = MaterialTheme.typography.titleLarge,
    )

@Composable
fun textFieldBorder() =
    Modifier.border(
        width = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
        color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
        shape = OutlinedTextFieldDefaults.shape,
    )

@Composable
fun HabitInfoChip(
    text: String,
    icon: ImageVector,
) {
    val ctx = LocalContext.current

    AssistChip(
//        colors = AssistChipDefaults.assistChipColors().copy(containerColor = Color.Yellow),
//        onClick = { openLink(USER_GUIDE_URL, ctx) },
        onClick = {},
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                contentDescription = null,
            )
        },
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun HabitChipsFlowRow(
    habit: Habit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        modifier = modifier,
    ) {
        val freq = HabitFrequency.entries[habit.frequency]
        HabitInfoChip(
            text =
                stringResource(
                    when (freq) {
                        HabitFrequency.Daily -> R.string.x_day_streak
                        HabitFrequency.Weekly -> R.string.x_week_streak
                        HabitFrequency.Monthly -> R.string.x_month_streak
                        HabitFrequency.Yearly -> R.string.x_year_streak
                    },
                    habit.streak.toString(),
                ),
            icon = Icons.AutoMirrored.Default.ShowChart,
        )
        HabitInfoChip(
            text =
                stringResource(
                    R.string.x_points,
                    habit.points.toString(),
                ),
            icon = Icons.Outlined.FavoriteBorder,
        )
        HabitInfoChip(
            text =
                stringResource(
                    R.string.x_percent_complete,
                    habit.score.toString(),
                ),
            icon = Icons.Default.Check,
        )
    }
}
