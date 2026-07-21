package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.domain.Person
import com.pedrotlf.barcalc.domain.TabItem
import com.pedrotlf.barcalc.ui.Screen
import com.pedrotlf.barcalc.ui.TabSession
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TabSessionSerializationTest {

    @Test
    fun `session round-trips through JSON`() {
        val session = TabSession(
            screen = Screen.PEOPLE,
            items = listOf(
                TabItem(1, "Beer", 1000L, 3, listOf(listOf(1), listOf(1), listOf(1, 2))),
                TabItem(2, "Nachos", 1250L, 1, listOf(emptyList())),
            ),
            people = listOf(Person(1, "Alice"), Person(2, "Bob")),
            itemSeq = 3,
            personSeq = 3,
            tipEnabled = false,
            tipPercent = 15,
        )
        val json = Json { ignoreUnknownKeys = true }
        val decoded = json.decodeFromString<TabSession>(json.encodeToString(TabSession.serializer(), session))
        assertEquals(session, decoded)
    }

    @Test
    fun `unknown keys in stored JSON are ignored`() {
        val json = Json { ignoreUnknownKeys = true }
        val decoded = json.decodeFromString<TabSession>(
            """{"screen":"ITEMS","items":[],"people":[],"futureField":true}"""
        )
        assertEquals(TabSession(), decoded)
    }
}
