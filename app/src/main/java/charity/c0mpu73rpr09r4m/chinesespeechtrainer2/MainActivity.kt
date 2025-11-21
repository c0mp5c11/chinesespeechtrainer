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
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme.ChineseSpeechTrainerTheme

class MainActivity : ComponentActivity() {
    private var displayText by mutableStateOf("")
    private var displayColor by mutableStateOf(Color.Green)
    private lateinit var speechRecognizer: SpeechRecognizer
    private var displayWord: String = "你好"
    private var isMatch: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                updateText("Requesting microphone permission...", Color.Yellow)

                if (isGranted) {
                    updateText("Permission granted!", Color.Green)
                    initSpeechRecognizer()
                } else {
                    updateText(
                        "Failed to get permission.\n\n未能获得许可。\n\nWèi néng huòdé xǔkě.",
                        Color.Red
                    )
                }
            }

        setContent {
            ChineseSpeechTrainerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        initSpeechRecognizer()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
                            color = displayColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(bundle: Bundle?) {
                val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                matches?.forEach { word ->
                    if (word.contains(displayWord)) {
                        updateText("Match found: $displayWord", Color.Green)
                        isMatch = true
                    }
                }

                if (isMatch) {
                    displayWord = getWord()
                    isMatch = false
                }

                speechRecognizer.startListening(intent)
            }

            override fun onReadyForSpeech(params: Bundle?) {
                if (isMatch) {
                    updateText(displayWord, Color.Green)
                } else {
                    updateText(displayWord, Color.White)
                }
            }

            override fun onError(error: Int) {
                speechRecognizer.startListening(intent)
            }

            override fun onPartialResults(bundle: Bundle?) {}
            override fun onEndOfSpeech() {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    private fun getWord(): String {
        // TODO: generate from db file
        val result = displayWord + "1"
        return result
    }

    private fun updateText(text: String, color: Color) {
        displayText = text
        displayColor = color
    }
}