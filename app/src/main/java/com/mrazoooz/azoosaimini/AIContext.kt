package com.mrazoooz.azoosaimini

/**
 * Small in-memory conversation context.
 * It keeps recent turns only; it is not a persistent memory or a language model.
 */
class AIContext(private val maxTurns: Int = 8) {
    data class Turn(val user: String, val assistant: String)

    private val history = ArrayDeque<Turn>()

    fun add(user: String, assistant: String) {
        history.addLast(Turn(user.trim(), assistant.trim()))
        while (history.size > maxTurns) history.removeFirst()
    }

    fun recent(): List<Turn> = history.toList()

    fun clear() = history.clear()

    fun contextText(): String = history.joinToString("\n") {
        "المستخدم: ${it.user}\nAzooz AI: ${it.assistant}"
    }
}