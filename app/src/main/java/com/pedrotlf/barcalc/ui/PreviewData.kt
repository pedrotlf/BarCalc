package com.pedrotlf.barcalc.ui

import com.pedrotlf.barcalc.domain.Person
import com.pedrotlf.barcalc.domain.TabItem

/** Sample state for @Preview composables: 2 people over a small shared tab. */
fun previewTabState(screen: Screen = Screen.ITEMS): TabUiState = TabUiState(
    session = TabSession(
        screen = screen,
        items = listOf(
            TabItem(1, "Beer", 1000L, 3, listOf(listOf(1), listOf(1), listOf(1, 2))),
            TabItem(2, "Nachos", 1200L, 1, listOf(listOf(2))),
        ),
        people = listOf(Person(1, "Alice"), Person(2, "Bob")),
        itemSeq = 3,
        personSeq = 3,
    ),
)
