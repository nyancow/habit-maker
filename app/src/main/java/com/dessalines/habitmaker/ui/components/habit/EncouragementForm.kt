package com.dessalines.habitmaker.ui.components.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING

@Composable
fun EncouragementForm(
    encouragement: Encouragement? = null,
    onChange: (Encouragement) -> Unit,
    onDelete: () -> Unit,
) {
    var content by rememberSaveable {
        mutableStateOf(encouragement?.content.orEmpty())
    }

    fun encouragementChange() =
        onChange(
            Encouragement(
                id = encouragement?.id ?: 0,
                habitId = encouragement?.habitId ?: 0,
                content = content,
            ),
        )

    Column(
        modifier = Modifier.padding(horizontal = SMALL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.encouragement)) },
            trailingIcon = {
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                        )
                    },
                    onClick = onDelete,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            value = content,
            onValueChange = {
                content = it
                encouragementChange()
            },
        )
    }
}

@Composable
@Preview
fun EncouragementFormPreview() {
    EncouragementForm(
        onChange = {},
        onDelete = {},
    )
}
