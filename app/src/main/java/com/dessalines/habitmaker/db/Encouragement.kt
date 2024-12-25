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
    indices = [Index(value = ["habit_id", "content"], unique = true)],
)
@Keep
data class Encouragement(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "content",
    )
    val content: String,
)

@Entity
data class EncouragementInsert(
    @ColumnInfo(
        name = "habit_id",
    )
    val encouragementId: Int,
    @ColumnInfo(
        name = "content",
    )
    val content: String,
)

@Entity
data class EncouragementUpdate(
    val id: Int,
    @ColumnInfo(
        name = "content",
    )
    val content: String,
)

private const val BY_ID_QUERY = "SELECT * FROM Encouragement where id = :encouragementId"

@Dao
interface EncouragementDao {
    @Query("SELECT * FROM Encouragement where habit_id = :habitId")
    fun getFromList(habitId: Int): Flow<List<Encouragement>>

    // TODO check which one of these is used
    @Query(BY_ID_QUERY)
    fun getById(encouragementId: Int): Flow<Encouragement>

    @Query(BY_ID_QUERY)
    fun getByIdSync(encouragementId: Int): Encouragement

    @Insert(entity = Encouragement::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(encouragement: EncouragementInsert): Long

    @Update(entity = Encouragement::class)
    suspend fun update(encouragement: EncouragementUpdate)

    @Delete
    suspend fun delete(encouragement: Encouragement)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class EncouragementRepository(
    private val encouragementDao: EncouragementDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getFromList(encouragementId: Int) = encouragementDao.getFromList(encouragementId)

    fun getById(encouragementId: Int) = encouragementDao.getById(encouragementId)

    fun getByIdSync(encouragementId: Int) = encouragementDao.getByIdSync(encouragementId)

    fun insert(encouragement: EncouragementInsert) = encouragementDao.insert(encouragement)

    @WorkerThread
    suspend fun update(encouragement: EncouragementUpdate) = encouragementDao.update(encouragement)

    @WorkerThread
    suspend fun delete(encouragement: Encouragement) = encouragementDao.delete(encouragement)
}

class EncouragementViewModel(
    private val repository: EncouragementRepository,
) : ViewModel() {
    fun getFromList(encouragementId: Int) = repository.getFromList(encouragementId)

    fun getById(encouragementId: Int) = repository.getById(encouragementId)

    fun getByIdSync(encouragementId: Int) = repository.getByIdSync(encouragementId)

    fun insert(encouragement: EncouragementInsert) = repository.insert(encouragement)

    fun update(encouragement: EncouragementUpdate) =
        viewModelScope.launch {
            repository.update(encouragement)
        }

    fun delete(encouragement: Encouragement) =
        viewModelScope.launch {
            repository.delete(encouragement)
        }
}

class EncouragementViewModelFactory(
    private val repository: EncouragementRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EncouragementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EncouragementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val sampleEncouragements =
    arrayOf(
        Encouragement(
            id = 1,
            habitId = 1,
            content = "Great job, keep going!",
        ),
        Encouragement(
            id = 2,
            habitId = 1,
            content = "Excellent! Remember why you're doing this.",
        ),
        Encouragement(
            id = 3,
            habitId = 1,
            content = "Nice! You're almost there!",
        ),
    )
