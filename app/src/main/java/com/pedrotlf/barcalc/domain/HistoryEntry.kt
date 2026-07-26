package com.pedrotlf.barcalc.domain

/**
 * A row in the history list. Deliberately excludes the serialized session —
 * the JSON is only read when a tab is actually duplicated, so scrolling a long
 * history never parses anything.
 */
data class HistoryEntry(
    val id: Long,
    val savedAt: Long,
    val customName: String?,
    val itemCount: Int,
    val personCount: Int,
    val totalCents: Long,
) {
    /** True when the user has given this tab their own name. */
    val hasCustomName: Boolean get() = !customName.isNullOrBlank()
}
