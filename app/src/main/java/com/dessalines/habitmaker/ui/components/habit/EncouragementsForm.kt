package com.dessalines.habitmaker.ui.components.habit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Encouragement
import com.dessalines.habitmaker.db.sampleEncouragements
import okhttp3.internal.toImmutableList

@Composable
fun EncouragementsForm(
    initialEncouragements: List<Encouragement>,
    onChange: (List<Encouragement>) -> Unit,
) {
    var encouragements by rememberSaveable {
        mutableStateOf(initialEncouragements)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        encouragements.forEachIndexed { index, encouragement ->
            EncouragementForm(
                encouragement = encouragement,
                onChange = {
                    val tmp = encouragements.toMutableList()
                    tmp[index] = it
                    encouragements = tmp.toImmutableList()
                    onChange(encouragements)
                },
                onDelete = {
                    val tmp = encouragements.toMutableList()
                    tmp.removeAt(index)
                    encouragements = tmp.toImmutableList()
                    onChange(encouragements)
                },
            )
        }
        Button(
            onClick = {
                val tmp = encouragements.toMutableList()
                // TODO What to do about IDs here?
                tmp.add(
                    Encouragement(
                        id = 0,
                        habitId = 0,
                        content = "",
                    ),
                )
                encouragements = tmp.toImmutableList()
                onChange(encouragements)
            },
        ) {
            Text(stringResource(R.string.add_encouragement))
        }
    }
}

@Composable
@Preview
fun EncouragementsFormPreview() {
    EncouragementsForm(
        initialEncouragements = sampleEncouragements,
        onChange = {},
    )
}
