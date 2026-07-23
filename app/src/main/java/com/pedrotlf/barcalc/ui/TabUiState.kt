package com.pedrotlf.barcalc.ui

import com.pedrotlf.barcalc.domain.Person
import com.pedrotlf.barcalc.domain.TabItem
import kotlinx.serialization.Serializable

/** Wizard screens, in order. */
enum class Screen { ITEMS, PEOPLE, RESULTS }

/**
 * The persistable core of a splitting session — everything that must survive
 * an app restart ([com.pedrotlf.barcalc.data.SessionRepository] serializes
 * exactly this).
 */
@Serializable
data class TabSession(
    val screen: Screen = Screen.ITEMS,
    val items: List<TabItem> = emptyList(),
    val people: List<Person> = emptyList(),
    val itemSeq: Int = 1,
    val personSeq: Int = 1,
    val tipEnabled: Boolean = TabDefaults.TIP_ENABLED,
    val tipPercent: Int = TabDefaults.TIP_PERCENT,
)

/** Full UI state: session + transient drafts and view flags. */
data class TabUiState(
    val session: TabSession = TabSession(),
    val newItemName: String = "",
    val newItemPriceCents: Long = 0L,
    val newItemQty: Int = 1,
    val newPersonName: String = "",
    val activePersonId: Int? = null,
    val expandedResultIds: Set<Int> = emptySet(),
    val showAbout: Boolean = false,
) {
    val items: List<TabItem> get() = session.items
    val people: List<Person> get() = session.people
    val screen: Screen get() = session.screen
    val tipEnabled: Boolean get() = session.tipEnabled
    val tipPercent: Int get() = session.tipPercent

    /** Index of the person whose claim sheet is open, or -1. */
    val activePersonIndex: Int get() = people.indexOfFirst { it.id == activePersonId }

    /** The person whose claim sheet is open, if any. */
    val activePerson: Person? get() = people.getOrNull(activePersonIndex)

    val addItemEnabled: Boolean
        get() = newItemName.isNotBlank() && newItemPriceCents > 0L

    val addPersonEnabled: Boolean get() = newPersonName.isNotBlank()
}

/** Design defaults, hardcoded per the design's props. */
object TabDefaults {
    const val TIP_ENABLED = true
    const val TIP_PERCENT = 10
    const val TIP_PERCENT_MAX = 40
    const val QTY_MAX = 99
}
