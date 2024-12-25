package com.dessalines.habitmaker.db

import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("habit_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "check_time"], unique = true)],
)
@Keep
data class HabitCheck(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "check_time",
    )
    val checkTime: Long,
)

@Entity
data class HabitCheckInsert(
    @ColumnInfo(
        name = "habit_id",
    )
    val habitCheckId: Int,
    @ColumnInfo(
        name = "check_time",
    )
    val checkTime: Long,
)

@Entity
data class HabitCheckUpdate(
    val id: Int,
    @ColumnInfo(
        name = "check_time",
    )
    val checkTime: Long,
)

private const val BY_ID_QUERY = "SELECT * FROM HabitCheck where id = :habitCheckId"

@Dao
interface HabitCheckDao {
    @Query("SELECT * FROM HabitCheck where habit_id = :habitId")
    fun getFromList(habitId: Int): Flow<List<HabitCheck>>

    // TODO check which one of these is used
    @Query(BY_ID_QUERY)
    fun getById(habitCheckId: Int): Flow<HabitCheck>

    @Query(BY_ID_QUERY)
    fun getByIdSync(habitCheckId: Int): HabitCheck

    @Insert(entity = HabitCheck::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(habitCheck: HabitCheckInsert): Long

    @Update(entity = HabitCheck::class)
    suspend fun update(habitCheck: HabitCheckUpdate)

    @Delete
    suspend fun delete(habitCheck: HabitCheck)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HabitCheckRepository(
    private val habitCheckDao: HabitCheckDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getFromList(habitCheckId: Int) = habitCheckDao.getFromList(habitCheckId)

    fun getById(habitCheckId: Int) = habitCheckDao.getById(habitCheckId)

    fun getByIdSync(habitCheckId: Int) = habitCheckDao.getByIdSync(habitCheckId)

    fun insert(habitCheck: HabitCheckInsert) = habitCheckDao.insert(habitCheck)

    @WorkerThread
    suspend fun update(habitCheck: HabitCheckUpdate) = habitCheckDao.update(habitCheck)

    @WorkerThread
    suspend fun delete(habitCheck: HabitCheck) = habitCheckDao.delete(habitCheck)
}

class HabitCheckViewModel(
    private val repository: HabitCheckRepository,
) : ViewModel() {
    fun getFromList(habitCheckId: Int) = repository.getFromList(habitCheckId)

    fun getById(habitCheckId: Int) = repository.getById(habitCheckId)

    fun getByIdSync(habitCheckId: Int) = repository.getByIdSync(habitCheckId)

    fun insert(habitCheck: HabitCheckInsert) = repository.insert(habitCheck)

    fun update(habitCheck: HabitCheckUpdate) =
        viewModelScope.launch {
            repository.update(habitCheck)
        }

    fun delete(habitCheck: HabitCheck) =
        viewModelScope.launch {
            repository.delete(habitCheck)
        }
}

class HabitCheckViewModelFactory(
    private val repository: HabitCheckRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitCheckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitCheckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val sampleHabitChecks =
    arrayOf(
        HabitCheck(
            id = 1,
            habitId = 1,
            checkTime = 0,
        ),
        HabitCheck(
            id = 2,
            habitId = 2,
            checkTime = 0,
        ),
    )
