package com.daybook.app.data

data class DraftMoney(
    val amountCents: Long,
    val isExpense: Boolean,
    val category: String,
    val label: String
)

data class OrganizedEntry(
    val note: String,
    val mood: String,
    val money: List<DraftMoney>
)

/** Turns a spoken transcript into a tidy note, a mood and money entries. Works on the phone, no AI service. */
object EntryOrganizer {

    private val I = RegexOption.IGNORE_CASE

    fun organize(transcript: String): OrganizedEntry {
        val cleaned = removeFillers(transcript)
        val sentences = splitSentences(cleaned)
        val note = sentences.joinToString("\n") { "• " + tidy(it) }
        val money = sentences.flatMap { extractMoney(it) }
        return OrganizedEntry(note, detectMood(cleaned), money)
    }

    // ------------------------------------------------------------ Note

    private val fillers = Regex("""\b(?:um+|uh+|erm|hmm+|you know|i mean)\b,?""", I)

    // Splits at punctuation (but not the dot in 12.50), new lines, and "then / after that".
    private val sentenceSplitter = Regex(
        """(?<!\d)[.!?]+|[.!?]+(?!\d)|\n+|\b(?:and then|then|after that|afterwards)\b""",
        I
    )

    private fun removeFillers(text: String) =
        text.replace(fillers, " ").replace(Regex("""[ \t]+"""), " ")

    private fun splitSentences(text: String): List<String> =
        text.split(sentenceSplitter)
            .map { it.trim(' ', ',', ';') }
            .filter { it.length > 2 }

    private fun tidy(sentence: String): String {
        var s = sentence.trim().replace(Regex("""\bi\b"""), "I")
        s = s.replaceFirstChar { it.uppercase() }
        if (!s.endsWith(".") && !s.endsWith("!") && !s.endsWith("?")) s += "."
        return s
    }

    // ------------------------------------------------------------ Mood

    private val moodWords = linkedMapOf(
        "😄" to listOf("happy", "great", "amazing", "wonderful", "excited", "awesome",
            "fantastic", "thrilled", "loved", "delighted", "proud"),
        "🙂" to listOf("good", "nice", "fine", "pleasant", "calm", "relaxed", "peaceful",
            "productive", "content", "grateful", "glad"),
        "😐" to listOf("okay", "ok", "meh", "boring", "bored", "average", "normal"),
        "😔" to listOf("sad", "down", "lonely", "upset", "cried", "disappointed", "depressed",
            "anxious", "worried", "stressed", "hurt", "miserable"),
        "😡" to listOf("angry", "furious", "annoyed", "frustrated", "mad", "irritated"),
        "😴" to listOf("tired", "exhausted", "sleepy", "drained", "fatigued", "sleepless")
    )

    private fun detectMood(text: String): String {
        val lower = text.lowercase()
        var best = ""
        var bestScore = 0
        for ((emoji, words) in moodWords) {
            val score = words.sumOf { w -> Regex("""\b${Regex.escape(w)}\b""").findAll(lower).count() }
            if (score > bestScore) {
                best = emoji
                bestScore = score
            }
        }
        return best
    }

    // ------------------------------------------------------------ Money

    private val clauseSplitter = Regex("""\s*(?:,|;|\band\b)\s*""", I)

    private val amountRegex = Regex(
        """(?:(\$|€|£|\b(?:ksh|kes|usd|eur|gbp)\b\.?)\s?)?(\d{1,3}(?:,\d{3})+|\d+)(?:\.(\d{1,2}))?(?!\d)(?:\s?\b(dollars?|bucks|euros?|pounds?|shillings?|bob|rupees|naira|rand|ksh|kes|usd|eur|gbp)\b)?""",
        I
    )

    private val unitAfter = Regex(
        """^\s*(?:(?:am|pm|a\.m|p\.m|o'clock|people|persons?|minutes?|mins?|hours?|hrs?|days?|weeks?|months?|years?|km|kms|kilometers?|miles?|kg|percent|times|steps|st|nd|rd|th)\b|%|:\d)""",
        I
    )

    private val expenseVerbs = Regex(
        """\b(?:spent|spend|paid|pay|bought|buy|purchased|cost|costs|charged|fare|bill|rent|tipped|donated)\b""", I
    )
    private val incomeVerbs = Regex(
        """\b(?:earned|earn|received|got paid|was paid|salary|paycheck|wages?|income|sold|refund(?:ed)?|bonus|deposited)\b""", I
    )

    private val expenseCategories = listOf(
        "Food" to Regex("""\b(?:lunch|dinner|breakfast|supper|food|coffee|tea|snacks?|groceries|grocery|restaurant|meal|drinks?|pizza|burger|fruit|takeaway)\b""", I),
        "Transport" to Regex("""\b(?:bus|taxi|uber|bolt|fuel|petrol|gas|fare|matatu|train|parking|ride|boda|flight|metro)\b""", I),
        "Bills" to Regex("""\b(?:rent|electricity|internet|airtime|wifi|subscription|bills?|insurance)\b""", I),
        "Shopping" to Regex("""\b(?:shirt|shoes|clothes|shopping|store|mall|bag|dress|gadget)\b""", I),
        "Health" to Regex("""\b(?:pharmacy|medicine|doctor|hospital|clinic|dentist|medical)\b""", I),
        "Fun" to Regex("""\b(?:movie|cinema|game|concert|party|netflix|bar|club|outing)\b""", I)
    )
    private val incomeCategories = listOf(
        "Salary" to Regex("""\b(?:salary|paycheck|wages?|payday|got paid|was paid)\b""", I),
        "Gift" to Regex("""\b(?:gift|present|birthday)\b""", I)
    )

    private fun category(text: String, income: Boolean): String? =
        (if (income) incomeCategories else expenseCategories)
            .firstOrNull { it.second.containsMatchIn(text) }?.first

    private fun extractMoney(sentence: String): List<DraftMoney> {
        val isIncome = incomeVerbs.containsMatchIn(sentence)
        val hasVerb = isIncome || expenseVerbs.containsMatchIn(sentence)
        val results = mutableListOf<DraftMoney>()

        for (clause in sentence.split(clauseSplitter)) {
            for (m in amountRegex.findAll(clause)) {
                val marker = m.groups[1] != null || m.groups[4] != null
                val whole = m.groupValues[2].replace(",", "")
                val decimals = m.groupValues[3]

                if (!marker) {
                    if (!hasVerb) continue
                    val prev = clause.getOrNull(m.range.first - 1)
                    if (prev == ':' || prev == '/') continue
                    if (unitAfter.containsMatchIn(clause.substring(m.range.last + 1))) continue
                    if (decimals.isEmpty() && whole.toIntOrNull() in 1900..2100) continue
                }

                val number = if (decimals.isEmpty()) whole else "$whole.$decimals"
                val cents = Money.parseToCents(number) ?: continue

                val label = clause.replace(m.value, " ")
                    .replace(Regex("""\s+"""), " ")
                    .trim()
                    .replace(Regex("""^(?:and|so|but)\s+""", I), "")
                    .replace(Regex("""\s+(?:for|on|at|of|about|around|only|just)$""", I), "")
                    .replaceFirstChar { it.uppercase() }
                    .take(40)

                results += DraftMoney(
                    amountCents = cents,
                    isExpense = !isIncome,
                    category = category(clause, isIncome)
                        ?: category(sentence, isIncome)
                        ?: "Other",
                    label = label
                )
            }
        }
        return results
    }
}
