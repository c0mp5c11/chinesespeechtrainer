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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.MutableLiveData
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.ui.theme.ChineseSpeechTrainerTheme
import edu.cmu.pocketsphinx.Hypothesis
import edu.cmu.pocketsphinx.RecognitionListener
import edu.cmu.pocketsphinx.SpeechRecognizer
import edu.cmu.pocketsphinx.SpeechRecognizerSetup
import java.io.File
import java.io.IOException
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState


class MainActivity : ComponentActivity(), RecognitionListener, TextToSpeech.OnInitListener {
    private var displayText by mutableStateOf("")
    private var translation: Translation? = null
    private val dictionaryFileName = "zh_cn.dic"
    private val maximumTries = 3
    private val threshold = 1e-20f
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
    val borderColor = MutableLiveData(Color.White)

    private val timeout: Long = 10*60*1000L

    override fun onResult(hypothesis: Hypothesis?) {}
    override fun onBeginningOfSpeech() {}
    override fun onError(error: Exception?) {}
    override fun onTimeout() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
        wakeLock?.acquire( timeout)
        enableEdgeToEdge()

        setContent {
            ChineseSpeechTrainerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = wordIndex.toString(),
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = TextAlign.Start,
                                lineHeight = 12.sp,
                                modifier = Modifier.align(Alignment.TopStart)
                            )

                            val color by borderColor.observeAsState(Color.White)

                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.White, shape = RectangleShape)
                                    .border(5.dp, color, RectangleShape)
                                    .padding(70.dp, 20.dp),
                                contentAlignment = Alignment.Center
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
        }

        wordIndex = dataLogic.getWordIndex(this)
        translation = dataLogic.getTranslation(this, wordIndex)

        if (translation != null) {
            updateDisplayText()
        } else {
            startActivity(Intent(this, WinActivity::class.java))
        }

        Thread {
            try {
                val speechFolder = speechLogic.copyFolder(this)
                setupRecognizer(speechFolder)
                refresh()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()

        tts = TextToSpeech(applicationContext, this)
    }

    override fun onPause() {
        super.onPause()

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.CHINESE
            }
            isTtsInitialized = true
            speechRecognizer?.let {
                tts?.setOnUtteranceProgressListener(SpeechRecognizerUtteranceProgressListener(it))
            }
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.cancel()
        speechRecognizer?.shutdown()
        tts?.shutdown()
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        val text = hypothesis?.hypstr
        if (text != null) {
            if (text.contains(translation?.chineseWord ?: "")) {
                setBorderColor(Color.Green)
                onNext()
            } else {
                setBorderColor(Color.Red)
            }
        }
    }

    fun setBorderColor(color: Color) {
        borderColor.postValue(color)

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

    private fun refresh() {
        if (isSphinxInitialized && isTtsInitialized && translation != null) {
            updateDisplayText()
            announce()
            listen()
        }
    }

    private fun displayNext() {
        if (isSphinxInitialized && isTtsInitialized) {
            translation = dataLogic.getTranslation(this, wordIndex)
            if (translation != null) {
                updateDisplayText()
                announce()
                listen()
            } else {
                startActivity(Intent(this, WinActivity::class.java))
            }
        }
    }

    private fun updateDisplayText() {
        translation?.let {
            displayText = "${it.englishWord}\n${it.chineseWord}\n${it.pinyin}"
        }

        setBorderColor(Color.White)
    }

    private fun listen() {
        translation?.chineseWord?.let { word ->
            speechRecognizer?.addKeyphraseSearch(word, word)
            speechRecognizer?.startListening(word)
        }
    }

    private fun announce() {
        translation?.englishWord?.let { english ->
            tts?.language = Locale.ENGLISH
            tts?.speak(
                english,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "UTTERANCE_ID_ENGLISH"
            )
            translation?.chineseWord?.let { chinese ->
                val languageCode = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                if (languageCode == TextToSpeech.LANG_MISSING_DATA ||
                    languageCode == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.language = Locale.CHINESE
                }
                tts?.speak(
                    chinese,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "UTTERANCE_ID_CHINESE"
                )
            }
        }
    }
}