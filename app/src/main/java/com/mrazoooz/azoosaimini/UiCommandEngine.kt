package com.mrazoooz.azoosaimini

enum class UiElementType { HEADING, BUTTON, CARD, FORM }
enum class UiColor { GREEN, RED, BLUE, PURPLE, ORANGE, BLACK, DEFAULT }

data class UiElementSpec(
    val type: UiElementType,
    val label: String,
    val color: UiColor = UiColor.DEFAULT
)

data class UiCommandResult(
    val elements: List<UiElementSpec>,
    val reply: String
)

/**
 * Local command interpreter for the first UI-generation milestone.
 * It creates native Compose preview elements; it does not generate arbitrary code.
 */
object UiCommandEngine {
    fun create(command: String): UiCommandResult {
        val text = normalize(command)
        val color = detectColor(text)
        val elements = mutableListOf<UiElementSpec>()

        if (containsAny(text, "زر", "button")) {
            elements += UiElementSpec(UiElementType.BUTTON, extractButtonLabel(text), color)
        }
        if (containsAny(text, "عنوان", "heading", "هيدر")) {
            elements += UiElementSpec(UiElementType.HEADING, "عنوان جديد", color)
        }
        if (containsAny(text, "بطاقه", "كرت", "card")) {
            elements += UiElementSpec(UiElementType.CARD, "بطاقة جديدة", color)
        }
        if (containsAny(text, "نموذج", "فورم", "form", "حقول")) {
            elements += UiElementSpec(UiElementType.FORM, "حقل إدخال", color)
        }

        val reply = if (elements.isEmpty()) {
            "لم أحدد عنصرًا لإنشائه. جرّب: أنشئ زرًا أخضر، أضف عنوانًا، أنشئ بطاقة، أو أضف نموذجًا."
        } else {
            "تم إنشاء ${elements.size} عنصر في معاينة الواجهة. يمكنك تجربة الزر أو الكتابة في حقل النموذج."
        }
        return UiCommandResult(elements, reply)
    }

    private fun extractButtonLabel(text: String): String = when {
        text.contains("تسجيل") -> "تسجيل الدخول"
        text.contains("شراء") -> "شراء الآن"
        text.contains("ارسال") -> "إرسال"
        else -> "زر جديد"
    }

    private fun detectColor(text: String): UiColor = when {
        containsAny(text, "اخضر", "green") -> UiColor.GREEN
        containsAny(text, "احمر", "red") -> UiColor.RED
        containsAny(text, "ازرق", "blue") -> UiColor.BLUE
        containsAny(text, "بنفسجي", "purple") -> UiColor.PURPLE
        containsAny(text, "برتقالي", "orange") -> UiColor.ORANGE
        containsAny(text, "اسود", "black") -> UiColor.BLACK
        else -> UiColor.DEFAULT
    }

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(it) }

    private fun normalize(value: String): String = value.lowercase()
        .replace(Regex("[أإآٱ]"), "ا")
        .replace("ة", "ه")
        .replace("ى", "ي")
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
}
