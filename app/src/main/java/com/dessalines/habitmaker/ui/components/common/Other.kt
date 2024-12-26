package com.dessalines.habitmaker.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun textFieldBorder() =
    Modifier.border(
        width = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
        color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
        shape = OutlinedTextFieldDefaults.shape,
    )
