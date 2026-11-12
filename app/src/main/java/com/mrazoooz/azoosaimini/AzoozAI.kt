package com.mrazoooz.azoosaimini

/**
 * Main entry point for the in-app Kotlin AI prototype.
 * Combines the trainable intent classifier, UI command engine and website generator.
 */
class AzoozAI(
    private val neuralModel: NeuralMiniModel = NeuralMiniModel(),
    private val context: AIContext = AIContext()
) {
    data class Result(
        val reply: String,
        val intent: String,
        val confidence: Double,
        val ui: UiCommandResult,
        val website: WebsiteGenerator.GeneratedSite?
    )

    fun ask(prompt: String): Result {
        val cleaned = prompt.trim()
        require(cleaned.isNotEmpty()) { "اكتب طلبك أولاً." }

        val (intent, confidence) = neuralModel.predict(cleaned)
        val ui = UiCommandEngine.create(cleaned)
        val isWebsiteRequest = Regex(
            "موقع|صفحه|صفحة|متجر|مطعم|مدرسه|شركة|معرض|website|web site|landing",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(cleaned)
        val website = if (isWebsiteRequest) WebsiteGenerator.generate(cleaned) else null

        val reply = when {
            website != null -> "حللت وصفك وأنشأت مسودة موقع بعنوان «${website.title}» تضم: ${website.sections.joinToString("، ")}. يمكنك تطوير الوصف وإعادة التوليد."
            ui.elements.isNotEmpty() -> ui.reply
            intent == "greeting" -> "مرحباً بك! أنا Azooz AI، أستطيع تصنيف بعض الأوامر وإنشاء عناصر واجهة ومسودات مواقع."
            intent == "help" -> "اكتب وصفاً لموقع أو اطلب إنشاء عنوان أو زر أو بطاقة أو نموذج."
            else -> "استلمت طلبك، لكن قدرات النموذج الحالي تجريبية ومحدودة. جرّب وصف موقع أو عنصر واجهة."
        }
        context.add(cleaned, reply)
        return Result(reply, intent, confidence, ui, website)
    }

    fun recentConversation(): List<AIContext.Turn> = context.recent()
    fun resetConversation() = context.clear()
}