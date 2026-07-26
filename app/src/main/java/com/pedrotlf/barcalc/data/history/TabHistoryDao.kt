package com.pedrotlf.barcalc.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TabHistoryDao {

    /** Newest first. Projects away [TabHistoryEntity.sessionJson] on purpose. */
    @Query(
        """
        SELECT id, saved_at, custom_name, item_count, person_count, total_cents
        FROM tab_history
        ORDER BY saved_at DESC
        """
    )
    fun observeSummaries(): Flow<List<TabHistorySummary>>

    @Query("SELECT session_json FROM tab_history WHERE id = :id")
    suspend fun sessionJson(id: Long): String?

    @Insert
    suspend fun insert(entry: TabHistoryEntity): Long

    @Query("UPDATE tab_history SET custom_name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String?)

    @Query("DELETE FROM tab_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM tab_history")
    suspend fun clearAll()
}

/** Projection backing [TabHistoryDao.observeSummaries]. */
data class TabHistorySummary(
    val id: Long,
    @androidx.room.ColumnInfo(name = "saved_at") val savedAt: Long,
    @androidx.room.ColumnInfo(name = "custom_name") val customName: String?,
    @androidx.room.ColumnInfo(name = "item_count") val itemCount: Int,
    @androidx.room.ColumnInfo(name = "person_count") val personCount: Int,
    @androidx.room.ColumnInfo(name = "total_cents") val totalCents: Long,
)
