package com.dessalines.habitmaker.ui.components.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.nameIsValid

@Composable
fun HabitForm(
    habit: Habit? = null,
    onChange: (Habit) -> Unit,
) {
    var name by rememberSaveable {
        mutableStateOf(habit?.name.orEmpty())
    }

    var frequency by rememberSaveable {
        mutableStateOf(HabitFrequency.entries[habit?.frequency ?: 0])
    }

    var timesPerFrequency by rememberSaveable {
        mutableIntStateOf(habit?.timesPerFrequency ?: 1)
    }

    var notes by rememberSaveable {
        mutableStateOf(habit?.notes.orEmpty())
    }

    fun habitChange() =
        onChange(
            Habit(
                id = habit?.id ?: 0,
                name = name,
                frequency = frequency.ordinal,
                timesPerFrequency = timesPerFrequency,
                notes = notes,
                archived = habit?.archived ?: 0,
                points = habit?.points ?: 0,
                score = habit?.score ?: 0,
                streak = habit?.streak ?: 0,
                completed = habit?.completed ?: 0,
            ),
        )

    Column(
        modifier = Modifier.padding(horizontal = SMALL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            value = name,
            isError = !nameIsValid(name),
            onValueChange = {
                name = it
                habitChange()
            },
        )

        OutlinedTextField(
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(),
            value = notes,
            onValueChange = {
                notes = it
                habitChange()
            },
        )
    }
}

@Composable
@Preview
fun HabitFormPreview() {
    HabitForm(onChange = {})
}
