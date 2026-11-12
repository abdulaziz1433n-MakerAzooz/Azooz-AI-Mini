package com.mrazoooz.azoosaimini

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tanh
import kotlin.random.Random

/**
 * Tiny trainable MLP implemented with Kotlin arrays.
 * This is an intent classifier, not a general-purpose language model.
 */
class NeuralMiniModel(seed: Int = 42) {
    data class Example(val text: String, val label: String)

    private val examples = listOf(
        Example("مرحبا اهلا السلام عليكم", "greeting"),
        Example("هلا كيف حالك صباح الخير", "greeting"),
        Example("انشئ عنوان اكتب عنوان رئيسي", "heading"),
        Example("اضف عنوان كبير للصفحة", "heading"),
        Example("ضع عنوان ترحيبي في الموقع", "heading"),
        Example("انشئ زر اضف زر", "button"),
        Example("اضف زر تسجيل الدخول", "button"),
        Example("اريد زر للشراء", "button"),
        Example("انشئ بطاقة اضف كرت", "card"),
        Example("اعمل بطاقة منتج", "card"),
        Example("اضف بطاقة فيها صورة ونص", "card"),
        Example("انشئ نموذج اضف فورم", "form"),
        Example("اضف حقول الاسم والبريد", "form"),
        Example("اريد نموذج تواصل", "form"),
        Example("غير اللون الى احمر", "color"),
        Example("اجعل الخلفية سوداء", "color"),
        Example("غير لون الصفحة الى اخضر", "color"),
        Example("اجعل الزر عند الضغط يعرض رسالة", "interaction"),
        Example("اضف تفاعل عند النقر", "interaction"),
        Example("عندما اضغط على الزر افتح نافذة", "interaction"),
        Example("مساعدة ماذا تستطيع ان تفعل", "help"),
        Example("كيف استخدم البرنامج", "help"),
        Example("ما هي اوامرك", "help")
    )

    private val labels = examples.map { it.label }.distinct()
    private val vocabulary = examples.flatMap { tokenize(it.text) }.distinct().sorted()
    private val wordIndex = vocabulary.withIndex().associate { it.value to it.index }
    private val hiddenSize = 24
    private val random = Random(seed)
    private val w1 = Array(hiddenSize) {
        DoubleArray(vocabulary.size) { random.nextDouble(-0.2, 0.2) }
    }
    private val b1 = DoubleArray(hiddenSize)
    private val w2 = Array(labels.size) {
        DoubleArray(hiddenSize) { random.nextDouble(-0.2, 0.2) }
    }
    private val b2 = DoubleArray(labels.size)

    var trainingLoss: Double = 0.0
        private set

    init { train(epochs = 400, learningRate = 0.08) }

    private fun normalize(text: String): String = text.lowercase()
        .replace(Regex("[أإآٱ]"), "ا")
        .replace("ة", "ه")
        .replace("ى", "ي")
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")

    private fun tokenize(text: String): List<String> =
        normalize(text).split(Regex("\\s+")).filter { it.length > 1 }

    private fun vectorize(text: String): DoubleArray {
        val vector = DoubleArray(vocabulary.size)
        tokenize(text).forEach { token -> wordIndex[token]?.let { vector[it] += 1.0 } }
        val total = vector.sum().coerceAtLeast(1.0)
        for (i in vector.indices) vector[i] /= total
        return vector
    }

    private fun forward(input: DoubleArray): Pair<DoubleArray, DoubleArray> {
        val hidden = DoubleArray(hiddenSize)
        for (j in 0 until hiddenSize) {
            var value = b1[j]
            for (i in input.indices) value += w1[j][i] * input[i]
            hidden[j] = tanh(value)
        }
        val logits = DoubleArray(labels.size)
        for (k in labels.indices) {
            var value = b2[k]
            for (j in 0 until hiddenSize) value += w2[k][j] * hidden[j]
            logits[k] = value
        }
        val maxValue = logits.maxOrNull() ?: 0.0
        val exps = DoubleArray(logits.size) { exp(logits[it] - maxValue) }
        val sum = exps.sum().coerceAtLeast(1e-12)
        return hidden to DoubleArray(exps.size) { exps[it] / sum }
    }

    private fun train(epochs: Int, learningRate: Double) {
        val trainingSet = examples.map { vectorize(it.text) to labels.indexOf(it.label) }
        repeat(epochs) {
            var epochLoss = 0.0
            trainingSet.shuffled(random).forEach { (input, target) ->
                val (hidden, probabilities) = forward(input)
                epochLoss -= ln(probabilities[target].coerceAtLeast(1e-12))
                val outputGradient = probabilities.copyOf()
                outputGradient[target] -= 1.0
                val hiddenGradient = DoubleArray(hiddenSize)

                for (k in labels.indices) {
                    for (j in 0 until hiddenSize) {
                        hiddenGradient[j] += w2[k][j] * outputGradient[k]
                        w2[k][j] -= learningRate * outputGradient[k] * hidden[j]
                    }
                    b2[k] -= learningRate * outputGradient[k]
                }
                for (j in 0 until hiddenSize) {
                    val gradient = hiddenGradient[j] * (1.0 - hidden[j] * hidden[j])
                    for (i in input.indices) w1[j][i] -= learningRate * gradient * input[i]
                    b1[j] -= learningRate * gradient
                }
            }
            trainingLoss = epochLoss / trainingSet.size
        }
    }

    fun predict(text: String): Pair<String, Double> {
        val input = vectorize(text)
        if (input.sum() == 0.0) return "unknown" to 0.0
        val probabilities = forward(input).second
        val best = probabilities.indices.maxByOrNull { probabilities[it] }
            ?: return "unknown" to 0.0
        val confidence = probabilities[best]
        return if (confidence < 0.38) "unknown" to confidence
        else labels[best] to confidence
    }

    fun vocabularySize() = vocabulary.size
    fun labelCount() = labels.size
    fun trainingExampleCount() = examples.size
}
