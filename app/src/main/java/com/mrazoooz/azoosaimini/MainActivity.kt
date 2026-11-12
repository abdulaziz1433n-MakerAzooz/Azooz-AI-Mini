package com.mrazoooz.azoosaimini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = NeuralMiniModel()
        setContent { AzoozMiniScreen(model) }
    }
}

private data class ChatMessage(val isUser: Boolean, val text: String)

@Composable
private fun AzoozMiniScreen(model: NeuralMiniModel) {
    var prompt by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf(ChatMessage(false,
            "مرحبًا! أنا Azooz AI Mini. جرّب: أنشئ زر، أو غيّر لون الصفحة إلى أخضر.")))
    }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF16845B),
        secondary = Color(0xFF65B895)
    )) {
        Scaffold(
            topBar = {
                Surface(tonalElevation = 3.dp) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text("Azooz AI Mini", fontSize = 24.sp)
                        Text("نموذج عصبي تجريبي يعمل محليًا",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("حالة النموذج", style = MaterialTheme.typography.titleMedium)
                        Text("أمثلة التدريب: ${model.trainingExampleCount()}")
                        Text("مفردات النموذج: ${model.vocabularySize()}")
                        Text("التصنيفات: ${model.labelCount()}")
                        Text("خسارة التدريب: ${"%.3f".format(model.trainingLoss)}")
                    }
                }
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = if (message.isUser) Arrangement.End
                                else Arrangement.Start
                        ) {
                            Surface(
                                color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) { Text(message.text, Modifier.padding(12.dp)) }
                        }
                    }
                }
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("اكتب أمرًا بالعربية") },
                    maxLines = 3
                )
                Button(
                    onClick = {
                        val userText = prompt.trim()
                        if (userText.isNotEmpty()) {
                            val (intent, confidence) = model.predict(userText)
                            messages = messages + ChatMessage(true, userText) +
                                ChatMessage(false, buildReply(intent, confidence))
                            prompt = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = prompt.isNotBlank()
                ) { Text("إرسال") }
            }
        }
    }
}

private fun buildReply(intent: String, confidence: Double): String {
    val percent = (confidence * 100).roundToInt()
    val reply = when (intent) {
        "greeting" -> "أهلًا! أنا جاهز لتجربة أوامرك."
        "heading" -> "تعرفت على طلب إضافة عنوان. سنحوّله لاحقًا إلى عنصر HTML."
        "button" -> "تعرفت على طلب إضافة زر. سنضيف إنشاء الزر الفعلي في المرحلة التالية."
        "card" -> "تعرفت على طلب إنشاء بطاقة محتوى."
        "form" -> "تعرفت على طلب إنشاء نموذج إدخال."
        "color" -> "تعرفت على طلب تغيير لون التصميم."
        "interaction" -> "تعرفت على طلب إضافة تفاعل."
        "help" -> "أتعرف على التحية والعناوين والأزرار والبطاقات والنماذج والألوان والتفاعلات."
        else -> "لم أتعرف على الأمر بعد. جرّب: أضف زر أو أنشئ عنوانًا."
    }
    return "$reply\n\nالتصنيف: $intent • ثقة النموذج: $percent%"
}
