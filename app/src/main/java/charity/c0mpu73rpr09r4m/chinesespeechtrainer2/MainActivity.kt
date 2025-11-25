package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import androidx.compose.runtime.Composable
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
    private val maximumTries = 3
    private val threshold = 1e-20f
    private val wakeLockTag = "CHINESE_SPEECH_TRAINER:TAG"
    private var recognizer: SpeechRecognizer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wordIndex: Int = 0
    private var tryCount: Int = 1
    private var isSphinxInitialized: Boolean = false
    private var isTtsInitialized: Boolean = false
    private val dataLogic: DataLogic = DataLogic()
    private val speechLogic: SpeechLogic = SpeechLogic()
    private var ttsLogic: TtsLogic? = null
    var mutableBorderColor = MutableLiveData(Color.White)
    var borderColor = Color.White

    private val timeout: Long = 10 * 60 * 1000L

    override fun onResult(hypothesis: Hypothesis?) {}
    override fun onBeginningOfSpeech() {}
    override fun onError(error: Exception?) {}
    override fun onTimeout() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
        wakeLock?.acquire(timeout)
        ttsLogic = TtsLogic(applicationContext, this)
        enableEdgeToEdge()

        setContent {
            setContent()
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
                setupRecognizer(filesDir)
                refresh()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()


    }

    @Composable
    fun setContent() {
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

                        val color by mutableBorderColor.observeAsState(Color.White)

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
                                fontSize = 24.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true

            ttsLogic?.tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "UTTERANCE_ID_CHINESE") runOnUiThread { listen() }
                }
                override fun onError(utteranceId: String?) {
                    if (utteranceId == "UTTERANCE_ID_CHINESE") runOnUiThread { listen() }
                }
            })

            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer?.cancel()
        recognizer?.shutdown()
        ttsLogic?.tts?.shutdown()
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        val text = hypothesis?.hypstr

        if (text != null) {
            if (text.contains(translation?.chineseWord ?: "")) {
                borderColor = Color.Green
                onNext()
            } else {
                borderColor = Color.Red
            }
        }
    }

    override fun onEndOfSpeech() {
        tryCount++
        mutableBorderColor.postValue(borderColor)

        if (tryCount >= maximumTries) {
            onNext()
        }
    }

    private fun onNext() {
        recognizer?.cancel()
        recognizer?.stop()
        tryCount = 1
        wordIndex++
        dataLogic.setWordIndex(this, wordIndex)
        Handler(mainLooper).postDelayed({ displayNext() }, 3000)
    }
    @Throws(IOException::class)
    private fun setupRecognizer(speechFolder: File?) {
        if (speechFolder != null) {
            recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(File(speechFolder, "zh-cn"))
                .setDictionary(File(speechFolder, "zh_cn.dic"))
                .recognizer

            //recognizer?.addListener(this)
            isSphinxInitialized = true

        }
    }

    private fun refresh() {
        if (isSphinxInitialized && isTtsInitialized && translation != null) {
            updateDisplayText()
            ttsLogic?.announce(translation)
        }
    }

    private fun displayNext() {
        if (isSphinxInitialized && isTtsInitialized) {
            translation = dataLogic.getTranslation(this, wordIndex)
            if (translation != null) {
                updateDisplayText()
                ttsLogic?.announce(translation)
            } else {
                startActivity(Intent(this, WinActivity::class.java))
            }
        }
    }

    private fun updateDisplayText() {
        translation?.let {
            displayText = "${it.englishWord}\n${it.chineseWord}\n${it.pinyin}"
            borderColor = Color.White
        }
    }

    private fun listen() {
        translation?.chineseWord?.let { word ->
            recognizer?.addKeyphraseSearch(word, word)
            recognizer?.startListening(word)
        }
    }


}