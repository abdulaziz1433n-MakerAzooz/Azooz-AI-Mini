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
    val ai = remember(model) { AzoozAI(model) }
    var prompt by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf(ChatMessage(false,
            "مرحبًا! اكتب أمرًا مثل: أنشئ زرًا أخضر، أو أضف بطاقة، أو أنشئ نموذجًا.")))
    }
    var elements by remember { mutableStateOf(emptyList<UiElementSpec>()) }
    var previewNotice by remember { mutableStateOf("") }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF16845B),
        secondary = Color(0xFF65B895)
    )) {
        Scaffold(
            topBar = {
                Surface(tonalElevation = 3.dp) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text("Azooz AI Mini", fontSize = 24.sp)
                        Text("مساعد محلي تجريبي لإنشاء عناصر الواجهات",
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

                Text("معاينة الواجهة", style = MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (elements.isEmpty()) {
                            Text("ستظهر العناصر التي تنشئها هنا.")
                        } else {
                            elements.forEach { element ->
                                GeneratedElementView(element) {
                                    previewNotice = "تم الضغط على: ${element.label}"
                                }
                            }
                        }
                        if (previewNotice.isNotBlank()) {
                            Text(previewNotice, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Text("المحادثة", style = MaterialTheme.typography.titleMedium)
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
                            val aiResult = ai.ask(userText)
                            val result = aiResult.ui
                            val intent = aiResult.intent
                            val confidence = aiResult.confidence
                            if (result.elements.isNotEmpty()) {
                                elements = elements + result.elements
                                previewNotice = ""
                            }
                            messages = messages + ChatMessage(true, userText) +
                                ChatMessage(false, aiResult.reply + "\n\nالتصنيف العصبي: $intent • الثقة: ${(confidence * 100).roundToInt()}%")
                            prompt = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = prompt.isNotBlank()
                ) { Text("تنفيذ الأمر") }
            }
        }
    }
}

@Composable
private fun GeneratedElementView(element: UiElementSpec, onAction: () -> Unit) {
    var fieldValue by remember(element.label) { mutableStateOf("") }
    val tint = element.color.toComposeColor()
    when (element.type) {
        UiElementType.HEADING -> Text(
            element.label,
            style = MaterialTheme.typography.headlineSmall,
            color = tint
        )
        UiElementType.BUTTON -> Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = tint)
        ) { Text(element.label) }
        UiElementType.CARD -> Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.14f))
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(element.label, style = MaterialTheme.typography.titleMedium, color = tint)
                Text("هذا عنصر بطاقة أُنشئ من الأمر.")
            }
        }
        UiElementType.FORM -> OutlinedTextField(
            value = fieldValue,
            onValueChange = { fieldValue = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(element.label) },
            placeholder = { Text("اكتب هنا") },
            readOnly = false
        )
    }
}

private fun UiColor.toComposeColor(): Color = when (this) {
    UiColor.GREEN -> Color(0xFF16845B)
    UiColor.RED -> Color(0xFFD84343)
    UiColor.BLUE -> Color(0xFF2878D0)
    UiColor.PURPLE -> Color(0xFF8053B8)
    UiColor.ORANGE -> Color(0xFFE58A26)
    UiColor.BLACK -> Color(0xFF202124)
    UiColor.DEFAULT -> Color(0xFF16845B)
}
