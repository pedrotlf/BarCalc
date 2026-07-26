package com.pedrotlf.barcalc.data.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One archived tab. The full session is kept as JSON (the same
 * [com.pedrotlf.barcalc.ui.TabSession] format the in-progress tab uses), while
 * the summary columns are denormalized so the history list can render — and
 * sort — without deserializing every row.
 */
@Entity(tableName = "tab_history")
data class TabHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** Epoch millis the tab was archived. */
    @ColumnInfo(name = "saved_at") val savedAt: Long,

    /** User-chosen name; null means "show the generated summary". */
    @ColumnInfo(name = "custom_name") val customName: String? = null,

    @ColumnInfo(name = "session_json") val sessionJson: String,

    @ColumnInfo(name = "item_count") val itemCount: Int,
    @ColumnInfo(name = "person_count") val personCount: Int,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
)
