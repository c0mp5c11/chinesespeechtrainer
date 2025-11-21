package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme.ChineseSpeechTrainerTheme

class MainActivity : ComponentActivity() {
    private var displayText by mutableStateOf("")
    private lateinit var dictionaryEntry : DictionaryEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Modern permission launcher
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    updateText("Permission granted!")
                    val speechRecognizer = getSpeechRecognizer()
                    startListening(speechRecognizer)
                } else {
                    updateText("Failed to get permission.")
                }
            }

        setContent {
            ChineseSpeechTrainerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Check permission at startup
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        dictionaryEntry = DictionaryEntry("你好", "Hello", "Nǐ hǎo")
                        updateText("${dictionaryEntry.englishWord}\n\n${dictionaryEntry.chineseWord}\n\n${dictionaryEntry.pinyin}\n")
                        val speechRecognizer = getSpeechRecognizer()
                        startListening(speechRecognizer)
                    } else {
                        // Prompt the user
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        updateText("Requesting microphone permission...")
                    }

                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayText,
                            fontSize = 32.sp,
                            color = Color.Green,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    private fun getSpeechRecognizer(): SpeechRecognizer {
        val result = SpeechRecognizer.createSpeechRecognizer(this)

        return result
    }

    private fun startListening(speechRecognizer: SpeechRecognizer) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN") // Chinese (Mandarin)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
        speechRecognizer.startListening(intent)
    }

    private fun updateText(text: String) {
        displayText = text
    }
}