package com.pedrotlf.barcalc.domain.receipt

import java.text.Normalizer

/**
 * Turns the text read off a tab into items.
 *
 * Deliberately pure: it takes text and returns items, so every rule here is
 * exercised by unit tests rather than by pointing a camera at something. It
 * works a line at a time, which is how a till receipt reads — a name on the
 * left, an amount on the right.
 *
 * It is built to be forgiving rather than clever. Anything it can't make sense
 * of is dropped instead of guessed at, because a missing line is easy to add by
 * hand while a wrong price is easy to miss.
 */
object ReceiptParser {

    fun parse(rawText: String): List<ParsedItem> =
        parseLines(rawText.lines())

    fun parseLines(lines: List<String>): List<ParsedItem> =
        lines.mapNotNull { parseLine(it) }

    private fun parseLine(rawLine: String): ParsedItem? {
        val line = rawLine.trim()
        if (line.isEmpty()) return null
        if (isNotAnItem(line)) return null

        // The amount is the last thing on the line; everything before it is the
        // name, possibly with a quantity attached.
        val amountMatch = AmountAtEnd.find(line) ?: return null
        val amountCents = parseAmountCents(amountMatch.groupValues[1]) ?: return null
        if (amountCents <= 0L) return null
        val remainder = line.substring(0, amountMatch.range.first)

        // "CHOPP 2 x 12,00 24,00" states the unit price outright, which beats
        // dividing the total and risking a rounding guess.
        val explicit = QtyTimesUnit.find(remainder)
        val explicitUnit = explicit?.let { parseAmountCents(it.groupValues[2]) }
        if (explicit != null && explicitUnit != null && explicitUnit > 0L) {
            val name = tidyName(remainder.substring(0, explicit.range.first))
            return if (name == null) null else {
                ParsedItem(name, explicitUnit, explicit.groupValues[1].toInt().coerceAtLeast(1))
            }
        }

        val (withoutQty, qty) = extractQty(remainder)
        val name = tidyName(withoutQty) ?: return null

        // The trailing amount is taken as the line total, which is how most
        // tabs print it. Splitting it back into a unit price only holds when it
        // divides evenly.
        //
        // When it doesn't, the quantity is dropped rather than kept: an item
        // model is one price times a count, so keeping the count while using
        // the total as the unit price would multiply the line by itself. One
        // unit at the printed total is the only reading that leaves the tab
        // adding up, and the count is easy to restore by hand.
        return if (qty > 1 && amountCents % qty == 0L) {
            ParsedItem(name, amountCents / qty, qty)
        } else {
            ParsedItem(name, amountCents, 1)
        }
    }

    // ── Amounts ────────────────────────────────────────────────────────────

    /**
     * Reads a money token into cents, in either convention: `12,34` and `12.34`
     * both mean the same, as do `1.234,56` and `1,234.56`.
     *
     * A separator counts as the decimal point only when exactly two digits
     * follow it, which is what tells `1.234` (a thousand-odd) apart from
     * `12.34`. A bare number is read as whole currency units.
     */
    internal fun parseAmountCents(token: String): Long? {
        val digits = token.filter { it.isDigit() || it == ',' || it == '.' }
        if (digits.none { it.isDigit() }) return null

        val decimal = DecimalTail.find(digits)
        return if (decimal != null) {
            val whole = digits.dropLast(3).filter { it.isDigit() }.ifEmpty { "0" }
            whole.toLongOrNull()?.let { it * 100 + decimal.groupValues[1].toLong() }
        } else {
            digits.filter { it.isDigit() }.toLongOrNull()?.times(100)
        }
    }

    // ── Quantities ─────────────────────────────────────────────────────────

    /** Pulls a leading or trailing quantity off the name, defaulting to one. */
    private fun extractQty(text: String): Pair<String, Int> {
        QtyPrefixTimes.find(text)?.let { match ->
            return text.removeRange(match.range) to match.groupValues[1].toInt()
        }
        QtyPrefixPlain.find(text)?.let { match ->
            return text.removeRange(match.range) to match.groupValues[1].toInt()
        }
        QtySuffix.find(text)?.let { match ->
            return text.removeRange(match.range) to match.groupValues[1].toInt()
        }
        return text to 1
    }

    // ── Names ──────────────────────────────────────────────────────────────

    /**
     * Cleans up what's left of the line, or returns null when there isn't a
     * plausible name in it. Tabs are usually printed in capitals, so a shouted
     * name is folded back to something that reads like the rest of the app.
     */
    private fun tidyName(text: String): String? {
        val stripped = text
            .replace(LeadingItemCode, "")
            .trim()
            .trim('-', '–', ':', '.', '*', '|', ' ')
            .replace(RepeatedSpaces, " ")
            .trim()
        if (stripped.length < 2) return null
        if (stripped.none { it.isLetter() }) return null

        return if (stripped.none { it.isLowerCase() }) {
            stripped.split(' ').joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        } else {
            stripped
        }
    }

    // ── Lines that aren't items ────────────────────────────────────────────

    private fun isNotAnItem(line: String): Boolean {
        val normalized = normalize(line)
        if (NonItemKeywords.any { it in normalized }) return true
        return DateOrTime.containsMatchIn(line)
    }

    /** Lower-cases and drops accents, so "serviço" and "servico" both match. */
    private fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Diacritics, "")

    private val Diacritics = Regex("\\p{Mn}")
    private val RepeatedSpaces = Regex("\\s{2,}")

    /** Currency marker optional, then the number closing the line. */
    private val AmountAtEnd = Regex("""(?:r\$|us\$|\$|€)?\s*(\d[\d.,]*)\s*$""", RegexOption.IGNORE_CASE)
    private val DecimalTail = Regex("""[.,](\d{2})$""")

    private val QtyPrefixTimes = Regex("""^\s*(\d{1,2})\s*[x×]\s*""", RegexOption.IGNORE_CASE)
    private val QtyPrefixPlain = Regex("""^\s*(\d{1,2})\s+(?=\p{L})""")
    private val QtySuffix = Regex("""\s*[x×]\s*(\d{1,2})\s*$""", RegexOption.IGNORE_CASE)
    private val QtyTimesUnit = Regex("""(\d{1,2})\s*[x×]\s*(\d[\d.,]*)""", RegexOption.IGNORE_CASE)

    /** Till codes printed before the name, e.g. "0231 CHOPP". */
    private val LeadingItemCode = Regex("""^\s*\d{3,}\s+""")

    private val DateOrTime = Regex("""\d{1,2}[/\-.]\d{1,2}[/\-.]\d{2,4}|\b\d{1,2}:\d{2}\b""")

    /**
     * Words that mark a line as something other than an item — totals, payment,
     * and the header and footer a till prints around the order. Matched against
     * accent-stripped lower case, in both languages the app speaks.
     */
    private val NonItemKeywords = listOf(
        // Totals and adjustments
        "total", "sub-total", "subtotal", "taxa", "servico", "service charge",
        "service", "tax", "iva", "desconto", "discount", "acrescimo",
        // Tips
        "gorjeta", "tip",
        // Payment
        "troco", "change", "dinheiro", "cash", "cartao", "card", "credito",
        "debito", "credit", "debit", "visa", "mastercard", "pix", "pagamento",
        "payment", "a pagar", "amount due",
        // Header and footer noise
        "cnpj", "cpf", "cupom", "fiscal", "nota", "mesa", "table", "garcom",
        "waiter", "obrigado", "thank you", "atendente", "operador", "pedido",
    )
}
