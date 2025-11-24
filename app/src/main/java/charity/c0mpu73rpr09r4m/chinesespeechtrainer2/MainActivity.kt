package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme.ChineseSpeechTrainerTheme
import edu.cmu.pocketsphinx.Hypothesis
import edu.cmu.pocketsphinx.RecognitionListener
import edu.cmu.pocketsphinx.SpeechRecognizer
import edu.cmu.pocketsphinx.SpeechRecognizerSetup
import java.io.File
import java.io.IOException
import java.util.Locale

class MainActivity : ComponentActivity(), RecognitionListener, TextToSpeech.OnInitListener {
    private var displayText by mutableStateOf("")
    private var translation: Translation? = null
    private val dictionaryFileName = "zh_cn.dic"
    private val maximumTries = 5
    private val threshold = 4.885e-16f
    private val wakeLockTag = "CHINESE_SPEECH_TRAINER:TAG"
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wordIndex: Int = 0
    private var tryCount: Int = 1
    private var isSphinxInitialized: Boolean = false
    private var isTtsInitialized: Boolean = false
    private val dataLogic: DataLogic = DataLogic()
    private val speechLogic: SpeechLogic = SpeechLogic()

    override fun onResult(hypothesis: Hypothesis?) {}
    override fun onBeginningOfSpeech() {}
    override fun onError(error: Exception?) {}
    override fun onTimeout() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
        wakeLock?.acquire()
        enableEdgeToEdge()

        setContent {
            ChineseSpeechTrainerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White, shape = RectangleShape)
                                .border(1.dp, Color.Black, RectangleShape)
                                .padding(70.dp, 20.dp)
                        ) {
                            Text(
                                text = displayText,
                                fontSize = 40.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 50.sp
                            )
                        }
                    }
                }
            }
        }

        wordIndex = dataLogic.getWordIndex(this)
        translation = dataLogic.getTranslation(this, wordIndex)

        if (translation != null) {
            updateText("${translation?.englishWord}\n${translation?.chineseWord}\n${translation?.pinyin}")
        } else {
            startActivity(Intent(this, WinActivity::class.java))
        }

        Thread {
            try {
                val speechFolder = speechLogic.copyFolder(this)
                setupRecognizer(speechFolder)
                tryStartFirstSession()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()

        tts = TextToSpeech(applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.CHINESE)
            }

            isTtsInitialized = true

            speechRecognizer?.let {
                tts?.setOnUtteranceProgressListener(
                    SpeechRecognizerUtteranceProgressListener(it)
                )
            }

            tryStartFirstSession()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.cancel()
        speechRecognizer?.shutdown()
        tts?.shutdown()

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis != null) {
            onNext()
        }
    }

    override fun onEndOfSpeech() {
        tryCount++

        if (tryCount >= maximumTries) {
            onNext()
        }
    }

    private fun onNext() {
        speechRecognizer?.cancel()
        speechRecognizer?.stop()
        tryCount = 1
        wordIndex++
        dataLogic.setWordIndex(this, wordIndex)
        Handler(mainLooper).postDelayed({ displayNext() }, 3000)
    }

    @Throws(IOException::class)
    private fun setupRecognizer(speechFolder: File?) {
        if (speechFolder != null) {
            val setup = SpeechRecognizerSetup.defaultSetup()
            setup.setKeywordThreshold(threshold)
            setup.setAcousticModel(speechFolder)
            setup.setDictionary(File(speechFolder, dictionaryFileName))
            speechRecognizer = setup.recognizer
            speechRecognizer?.addListener(this)
            isSphinxInitialized = true
        }
    }

    private fun tryStartFirstSession() {
        if (isSphinxInitialized && isTtsInitialized && translation != null) {

            updateText("${translation?.englishWord}\n${translation?.chineseWord}\n${translation?.pinyin}")
            announce()
            listen()
        }
    }

    private fun displayNext() {
        if (isSphinxInitialized && isTtsInitialized) {
            translation = dataLogic.getTranslation(this, wordIndex)

            if (translation != null) {
                updateText("${translation?.englishWord}\n${translation?.chineseWord}\n${translation?.pinyin}")
                announce()
                listen()
            } else {
                startActivity(Intent(this, WinActivity::class.java))
            }
        }
    }

    private fun listen() {
        translation?.chineseWord?.let { word ->
            speechRecognizer?.addKeyphraseSearch(word, word)
            speechRecognizer?.startListening(word)
        }
    }

    private fun announce() {
        translation?.chineseWord?.let { word ->
            if (isTtsInitialized) {
                val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.CHINESE)
                }

                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID_$word")
            }
        }
    }

    private fun updateText(text: String) {
        displayText = text
    }
}