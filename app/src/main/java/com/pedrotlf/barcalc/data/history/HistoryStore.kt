package com.pedrotlf.barcalc.data.history

import com.pedrotlf.barcalc.domain.HistoryEntry
import com.pedrotlf.barcalc.ui.TabSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Archive of finished tabs. An interface so [com.pedrotlf.barcalc.ui.TabViewModel]
 * can be unit-tested with a fake, without Room or an Android runtime.
 */
interface HistoryStore {
    fun observeEntries(): Flow<List<HistoryEntry>>

    /** Archives [session], returning the new entry id. */
    suspend fun archive(session: TabSession): Long

    /** The full session behind [id], or null if it's gone. */
    suspend fun loadSession(id: Long): TabSession?

    /** Sets a custom name; blank or null restores the generated summary. */
    suspend fun rename(id: Long, name: String?)

    suspend fun delete(id: Long)

    suspend fun clearAll()
}

/** Room-backed [HistoryStore]. */
class RoomHistoryStore(
    private val dao: TabHistoryDao,
    private val now: () -> Long = System::currentTimeMillis,
) : HistoryStore {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeEntries(): Flow<List<HistoryEntry>> =
        dao.observeSummaries().map { rows ->
            rows.map {
                HistoryEntry(
                    id = it.id,
                    savedAt = it.savedAt,
                    customName = it.customName,
                    itemCount = it.itemCount,
                    personCount = it.personCount,
                    totalCents = it.totalCents,
                )
            }
        }

    override suspend fun archive(session: TabSession): Long = dao.insert(
        TabHistoryEntity(
            savedAt = now(),
            sessionJson = json.encodeToString(TabSession.serializer(), session),
            itemCount = session.items.sumOf { it.qty },
            personCount = session.people.size,
            totalCents = session.items.sumOf { it.priceCents * it.qty },
        )
    )

    override suspend fun loadSession(id: Long): TabSession? {
        val raw = dao.sessionJson(id) ?: return null
        return runCatching { json.decodeFromString<TabSession>(raw) }.getOrNull()
    }

    override suspend fun rename(id: Long, name: String?) =
        dao.rename(id, name?.trim()?.takeIf { it.isNotEmpty() })

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun clearAll() = dao.clearAll()
}
