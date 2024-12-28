package com.dessalines.habitmaker.ui.components.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettingsViewModel
import com.dessalines.habitmaker.db.DEFAULT_COMPLETED_COUNT
import com.dessalines.habitmaker.db.MAX_COMPLETED_COUNT
import com.dessalines.habitmaker.db.MIN_COMPLETED_COUNT
import com.dessalines.habitmaker.db.SettingsUpdate
import com.dessalines.habitmaker.ui.components.common.SimpleTopAppBar
import com.dessalines.habitmaker.utils.HabitSort
import com.dessalines.habitmaker.utils.HabitSortOrder
import com.dessalines.habitmaker.utils.ThemeColor
import com.dessalines.habitmaker.utils.ThemeMode
import com.dessalines.habitmaker.utils.toBool
import com.dessalines.habitmaker.utils.toInt
import com.roomdbexportimport.RoomDBExportImport
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.SliderPreference
import me.zhanghai.compose.preference.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel,
) {
    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()
    val ctx = LocalContext.current

    // TODO split this into thumb-key style:
    // Look and feel,
    // Backup and restore
    // Behavior
    // Guide

    var themeState = ThemeMode.entries[settings?.theme ?: 0]
    var themeColorState = ThemeColor.entries[settings?.themeColor ?: 0]
    var sortState = HabitSort.entries[settings?.sort ?: 0]
    var sortOrderState = HabitSortOrder.entries[settings?.sortOrder ?: 0]

    var hideCompletedState = (settings?.hideCompleted ?: 0).toBool()
    var hideArchivedState = (settings?.hideArchived ?: 0).toBool()
    var hidePointsOnHomeState = (settings?.hidePointsOnHome ?: 0).toBool()
    var hideScoreOnHomeState = (settings?.hideScoreOnHome ?: 0).toBool()
    var hideStreakOnHomeState = (settings?.hideStreakOnHome ?: 0).toBool()

    var completedCountState = (settings?.completedCount ?: DEFAULT_COMPLETED_COUNT).toFloat()
    var completedCountSliderState by remember { mutableFloatStateOf(completedCountState) }

    val dbSavedText = stringResource(R.string.database_backed_up)
    val dbRestoredText = stringResource(R.string.database_restored)

    val dbHelper = RoomDBExportImport(AppDB.getDatabase(ctx).openHelper)

    val exportDbLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip"),
        ) {
            it?.also {
                dbHelper.export(ctx, it)
                Toast.makeText(ctx, dbSavedText, Toast.LENGTH_SHORT).show()
            }
        }

    val importDbLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) {
            it?.also {
                dbHelper.import(ctx, it, true)
                Toast.makeText(ctx, dbRestoredText, Toast.LENGTH_SHORT).show()
            }
        }

    fun updateSettings() =
        appSettingsViewModel.updateSettings(
            SettingsUpdate(
                id = 1,
                completedCount = completedCountState.toInt(),
                theme = themeState.ordinal,
                themeColor = themeColorState.ordinal,
                sort = sortState.ordinal,
                sortOrder = sortOrderState.ordinal,
                hideCompleted = hideCompletedState.toInt(),
                hideArchived = hideArchivedState.toInt(),
                hidePointsOnHome = hidePointsOnHomeState.toInt(),
                hideScoreOnHome = hideScoreOnHomeState.toInt(),
                hideStreakOnHome = hideStreakOnHomeState.toInt(),
            ),
        )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.settings),
                onBackClick = { navController.navigateUp() },
            )
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .imePadding(),
            ) {
                ProvidePreferenceTheme {
                    Preference(
                        title = { Text(stringResource(R.string.about)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                            )
                        },
                        onClick = { navController.navigate("about") },
                    )
                    SliderPreference(
                        value = completedCountState,
                        sliderValue = completedCountSliderState,
                        onValueChange = {
                            completedCountState = it
                            updateSettings()
                        },
                        onSliderValueChange = { completedCountSliderState = it },
                        valueRange = MIN_COMPLETED_COUNT.toFloat()..MAX_COMPLETED_COUNT.toFloat(),
                        title = {
                            val completedCountStr =
                                stringResource(
                                    R.string.completed_count,
                                    completedCountSliderState.toInt().toString(),
                                )
                            Text(completedCountStr)
                        },
                        summary = {
                            Text(stringResource(R.string.completed_count_summary))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.DataThresholding,
                                contentDescription = null,
                            )
                        },
                    )
                    ListPreference(
                        type = ListPreferenceType.DROPDOWN_MENU,
                        value = themeState,
                        onValueChange = {
                            themeState = it
                            updateSettings()
                        },
                        values = ThemeMode.entries,
                        valueToText = {
                            AnnotatedString(ctx.getString(it.resId))
                        },
                        title = {
                            Text(stringResource(R.string.theme))
                        },
                        summary = {
                            Text(stringResource(themeState.resId))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                            )
                        },
                    )

                    ListPreference(
                        type = ListPreferenceType.DROPDOWN_MENU,
                        value = themeColorState,
                        onValueChange = {
                            themeColorState = it
                            updateSettings()
                        },
                        values = ThemeColor.entries,
                        valueToText = {
                            AnnotatedString(ctx.getString(it.resId))
                        },
                        title = {
                            Text(stringResource(R.string.theme_color))
                        },
                        summary = {
                            Text(stringResource(themeColorState.resId))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Colorize,
                                contentDescription = null,
                            )
                        },
                    )
                    ListPreference(
                        type = ListPreferenceType.DROPDOWN_MENU,
                        value = sortState,
                        onValueChange = {
                            sortState = it
                            updateSettings()
                        },
                        values = HabitSort.entries,
                        valueToText = {
                            AnnotatedString(ctx.getString(it.resId))
                        },
                        title = {
                            Text(stringResource(R.string.sort))
                        },
                        summary = {
                            Text(stringResource(sortState.resId))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Sort,
                                contentDescription = null,
                            )
                        },
                    )

                    ListPreference(
                        type = ListPreferenceType.DROPDOWN_MENU,
                        value = sortOrderState,
                        onValueChange = {
                            sortOrderState = it
                            updateSettings()
                        },
                        values = HabitSortOrder.entries,
                        valueToText = {
                            AnnotatedString(ctx.getString(it.resId))
                        },
                        title = {
                            Text(stringResource(R.string.sort_order))
                        },
                        summary = {
                            Text(stringResource(sortOrderState.resId))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.SortByAlpha,
                                contentDescription = null,
                            )
                        },
                    )

                    SwitchPreference(
                        value = hideCompletedState,
                        onValueChange = {
                            hideCompletedState = it
                            updateSettings()
                        },
                        title = {
                            Text(stringResource(R.string.hide_completed))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.RemoveRedEye,
                                contentDescription = null,
                            )
                        },
                    )
                    SwitchPreference(
                        value = hideArchivedState,
                        onValueChange = {
                            hideArchivedState = it
                            updateSettings()
                        },
                        title = {
                            Text(stringResource(R.string.hide_archived))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.RemoveRedEye,
                                contentDescription = null,
                            )
                        },
                    )

                    SwitchPreference(
                        value = hideStreakOnHomeState,
                        onValueChange = {
                            hideStreakOnHomeState = it
                            updateSettings()
                        },
                        title = {
                            Text(stringResource(R.string.hide_streak_on_home))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.RemoveRedEye,
                                contentDescription = null,
                            )
                        },
                    )

                    SwitchPreference(
                        value = hidePointsOnHomeState,
                        onValueChange = {
                            hidePointsOnHomeState = it
                            updateSettings()
                        },
                        title = {
                            Text(stringResource(R.string.hide_points_on_home))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.RemoveRedEye,
                                contentDescription = null,
                            )
                        },
                    )

                    SwitchPreference(
                        value = hideScoreOnHomeState,
                        onValueChange = {
                            hideScoreOnHomeState = it
                            updateSettings()
                        },
                        title = {
                            Text(stringResource(R.string.hide_score_on_home))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.RemoveRedEye,
                                contentDescription = null,
                            )
                        },
                    )

                    Preference(
                        title = { Text(stringResource(R.string.backup_database)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            exportDbLauncher.launch("habit-maker")
                        },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.restore_database)) },
                        summary = {
                            Text(stringResource(R.string.restore_database_warning))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Restore,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            importDbLauncher.launch(arrayOf("application/zip"))
                        },
                    )
                }
            }
        },
    )
}
