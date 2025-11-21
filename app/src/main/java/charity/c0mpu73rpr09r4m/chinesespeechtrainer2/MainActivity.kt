package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.lifecycle.lifecycleScope
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme.ChineseSpeechTrainerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // screen text
    private var displayText by mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.S)
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
                    updateText("Failed to get permission.\n\n未能获得许可。\n\nWèi néng huòdé xǔkě.")
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
                        updateText("Hello World\n\n你好 世界\n\nNǐ hǎo Shìjiè\n")
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getSpeechRecognizer(): SpeechRecognizer {
        // Use cloud recognizer for reliability
        val result = SpeechRecognizer.createSpeechRecognizer(this)

        result.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                updateText("onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                updateText("onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                updateText("onRmsChanged: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                updateText("onBufferReceived")
            }

            override fun onEndOfSpeech() {
                updateText("onEndOfSpeech")
            }

            override fun onError(error: Int) {
                updateText("Error code: $error")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.forEach { word ->
                    updateText("Recognized: $word")

                    if (word.contains("你好 世界")) {
                        updateText("Match found: 你好 世界")
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                updateText("onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                updateText("onEvent")
            }
        })

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
        lifecycleScope.launch(Dispatchers.Main) {
            displayText = text
        }
    }
}