package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.domain.receipt.ParsedItem
import com.pedrotlf.barcalc.domain.receipt.ReceiptParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptParserTest {

    // ── Amounts ────────────────────────────────────────────────────────────

    @Test
    fun `reads both decimal conventions`() {
        assertEquals(1234L, ReceiptParser.parseAmountCents("12,34"))
        assertEquals(1234L, ReceiptParser.parseAmountCents("12.34"))
    }

    @Test
    fun `reads thousands in either convention`() {
        assertEquals(123456L, ReceiptParser.parseAmountCents("1.234,56"))
        assertEquals(123456L, ReceiptParser.parseAmountCents("1,234.56"))
    }

    @Test
    fun `a separator with three digits after it is thousands, not cents`() {
        // The trap: "1.234" is a thousand-odd, not twelve-thirty-four.
        assertEquals(123400L, ReceiptParser.parseAmountCents("1.234"))
    }

    @Test
    fun `a bare number is whole currency units`() {
        assertEquals(1200L, ReceiptParser.parseAmountCents("12"))
    }

    @Test
    fun `currency markers are ignored`() {
        assertEquals(1200L, ReceiptParser.parseAmountCents("R$ 12,00"))
        assertEquals(1200L, ReceiptParser.parseAmountCents("$12.00"))
    }

    @Test
    fun `text with no digits is not an amount`() {
        assertEquals(null, ReceiptParser.parseAmountCents("TOTAL"))
    }

    // ── Single lines ───────────────────────────────────────────────────────

    @Test
    fun `a plain line becomes one item`() {
        assertEquals(
            listOf(ParsedItem("Nachos", 1200L, 1)),
            ReceiptParser.parse("Nachos 12,00"),
        )
    }

    @Test
    fun `a quantity prefix splits the line total into a unit price`() {
        // 2 for 24,00 means 12,00 each — the app stores unit prices.
        assertEquals(
            listOf(ParsedItem("Chopp", 1200L, 2)),
            ReceiptParser.parse("2x Chopp 24,00"),
        )
    }

    @Test
    fun `quantity is read with or without the times sign, and from the end`() {
        assertEquals(listOf(ParsedItem("Chopp", 1200L, 2)), ReceiptParser.parse("2 Chopp 24,00"))
        assertEquals(listOf(ParsedItem("Chopp", 1200L, 2)), ReceiptParser.parse("Chopp x2 24,00"))
    }

    @Test
    fun `an explicit unit price is trusted over dividing the total`() {
        assertEquals(
            listOf(ParsedItem("Chopp", 1200L, 3)),
            ReceiptParser.parse("CHOPP 3 x 12,00 36,00"),
        )
    }

    @Test
    fun `a total that doesn't divide evenly becomes a single unit`() {
        // 25,00 across 3 leaves a remainder. Keeping qty at 3 with 25,00 as the
        // unit price would bill 75,00, so the count is dropped and the line
        // still adds up to what the tab printed.
        assertEquals(
            listOf(ParsedItem("Petisco", 2500L, 1)),
            ReceiptParser.parse("3 Petisco 25,00"),
        )
    }

    @Test
    fun `parsed lines never total more than the tab printed`() {
        // The property that matters: whatever the split, qty * unit price must
        // come back to the amount on the line.
        val lines = listOf(
            "2x Chopp 24,00" to 2400L,
            "3 Petisco 25,00" to 2500L,
            "4 Agua 10,00" to 1000L,
            "Nachos 12,50" to 1250L,
        )
        lines.forEach { (line, printed) ->
            val item = ReceiptParser.parse(line).single()
            assertEquals(line, printed, item.priceCents * item.qty)
        }
    }

    @Test
    fun `shouted names are folded back to normal case`() {
        assertEquals("Batata Frita", ReceiptParser.parse("BATATA FRITA 32,00").single().name)
    }

    @Test
    fun `names already in mixed case are left alone`() {
        assertEquals("Água com gás", ReceiptParser.parse("Água com gás 6,00").single().name)
    }

    @Test
    fun `a till code before the name is dropped`() {
        assertEquals("Chopp", ReceiptParser.parse("0231 CHOPP 12,00").single().name)
    }

    // ── Lines that aren't items ────────────────────────────────────────────

    @Test
    fun `totals, charges and payment lines are skipped`() {
        val lines = listOf(
            "SUBTOTAL 62,50",
            "TOTAL 68,75",
            "Taxa de serviço 6,25",
            "Service charge 6,25",
            "Gorjeta 10,00",
            "TROCO 5,00",
            "Cartão de crédito 68,75",
            "Desconto 2,00",
        )
        assertTrue(ReceiptParser.parseLines(lines).isEmpty())
    }

    @Test
    fun `accented and unaccented spellings are both recognised`() {
        assertTrue(ReceiptParser.parse("Servico 6,25").isEmpty())
        assertTrue(ReceiptParser.parse("Serviço 6,25").isEmpty())
    }

    @Test
    fun `header and footer noise is skipped`() {
        val lines = listOf(
            "BAR DO ZE",
            "CNPJ 12.345.678/0001-90",
            "Mesa 7",
            "27/07/2026 21:14",
            "Obrigado pela preferência!",
        )
        assertTrue(ReceiptParser.parseLines(lines).isEmpty())
    }

    @Test
    fun `lines without an amount are skipped`() {
        assertTrue(ReceiptParser.parse("BAR DO ZE").isEmpty())
    }

    @Test
    fun `an amount with no name is skipped`() {
        assertTrue(ReceiptParser.parse("12,00").isEmpty())
    }

    @Test
    fun `a free item is skipped rather than added at nothing`() {
        assertTrue(ReceiptParser.parse("Cortesia 0,00").isEmpty())
    }

    // ── Whole receipts ─────────────────────────────────────────────────────

    @Test
    fun `a Brazilian tab parses to its items only`() {
        val receipt = """
            BAR DO ZE
            CNPJ 12.345.678/0001-90
            Mesa 7          27/07/2026 21:14

            2x Chopp                 24,00
            BATATA FRITA             32,00
            1 Água com gás            6,00

            SUBTOTAL                 62,00
            Taxa de serviço           6,20
            TOTAL                    68,20
            Obrigado pela preferência!
        """.trimIndent()

        assertEquals(
            listOf(
                ParsedItem("Chopp", 1200L, 2),
                ParsedItem("Batata Frita", 3200L, 1),
                ParsedItem("Água com gás", 600L, 1),
            ),
            ReceiptParser.parse(receipt),
        )
    }

    @Test
    fun `an English tab parses to its items only`() {
        val receipt = """
            THE ANCHOR
            Table 4

            2x Lager               $11.00
            Nachos                  $8.50
            Soda                    $3.00

            Subtotal               $22.50
            Service charge          $2.25
            TOTAL                  $24.75
            Thank you!
        """.trimIndent()

        assertEquals(
            listOf(
                ParsedItem("Lager", 550L, 2),
                ParsedItem("Nachos", 850L, 1),
                ParsedItem("Soda", 300L, 1),
            ),
            ReceiptParser.parse(receipt),
        )
    }

    @Test
    fun `nothing recognisable yields nothing rather than junk`() {
        assertTrue(ReceiptParser.parse("").isEmpty())
        assertTrue(ReceiptParser.parse("~~~~ !!! ~~~~").isEmpty())
    }
}
