package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.cmu.pocketsphinx.Hypothesis
import edu.cmu.pocketsphinx.RecognitionListener
import edu.cmu.pocketsphinx.SpeechRecognizer
import edu.cmu.pocketsphinx.SpeechRecognizerSetup
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : Activity(), RecognitionListener, TextToSpeech.OnInitListener {

    companion object {
        var dictionaryEntry: DictionaryEntry? = null
        const val DICTIONARY_ASSET_DIRECTORY = "zh_cn.cd_cont_5000"
        const val DICTIONARY_FILE = "zh_cn.dic"
        const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
        const val MAX_TRY_COUNT = 5
        var speechRecognizer: SpeechRecognizer? = null
        const val threshold = 4.885e-16f
        const val COMPANY_NAME = "C0MPU73R PR09R4M CHARITY"
        const val CHARACTER_COUNT_TEXT = "single"
        const val WAKE_LOCK_TAG = "CHINESE_SPEECH_TRAINER:TAG"
    }

    var intensity: Int = 1
    var level: Int = 1
    var tryCount: Int = 1
    private var tts: TextToSpeech? = null
    var isSphinxInitialized: Boolean = false
    var isTtsInitialized: Boolean = false
    private var wakeLock: PowerManager.WakeLock? = null
    private var localService: LocalService? = null

    /** Defines callbacks for service binding, passed to bindService() */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocalService.LocalBinder
            localService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // intentionally left blank
        }
    }

    override fun onStart() {
        super.onStart()
        DatabaseTask.context = this
        startService()
        val intent = Intent(this, LocalService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
        wakeLock?.acquire()
        setContentView(R.layout.start)

        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.RECORD_AUDIO
        )

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            DatabaseTask(this).execute()
        }
    }

    fun startService() {
        val serviceIntent = Intent(this, LocalService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(this, LocalService::class.java)
        stopService(serviceIntent)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.ERROR) {
            isTtsInitialized = true
            tts?.language = Locale.SIMPLIFIED_CHINESE
            tts?.setOnUtteranceProgressListener(
                SpeechRecognizerUtteranceProgressListener(speechRecognizer)
            )
            displayNext()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DatabaseTask(this).execute()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.cancel()
        speechRecognizer?.shutdown()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        stopService()
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        val chineseText = findViewById<TextView>(R.id.ChineseText)
        if (hypothesis != null && chineseText.currentTextColor != Color.GREEN) {
            chineseText.setTextColor(Color.GREEN)
            onNext()
        }
    }

    override fun onResult(hypothesis: Hypothesis?) {
        // intentionally left blank
    }

    override fun onBeginningOfSpeech() {
        // intentionally left blank
    }

    override fun onEndOfSpeech() {
        val chineseText = findViewById<TextView>(R.id.ChineseText)
        if (chineseText.currentTextColor != Color.GREEN) {
            tryCount++
            val tryText = findViewById<TextView>(R.id.TryText)
            tryText.text = String.format("%s", tryCount)
            if (tryCount >= MAX_TRY_COUNT) {
                chineseText.setTextColor(Color.RED)
                onNext()
            }
        }
    }

    fun onNext() {
        speechRecognizer?.cancel()
        speechRecognizer?.stop()
        tryCount = 1
        level++
        val handler = Handler()
        handler.postDelayed({ displayNext() }, 3000)
    }

    override fun onError(error: Exception?) {
        // intentionally left blank
    }

    override fun onTimeout() {
        // intentionally left blank
    }

    @Throws(IOException::class)
    fun setupRecognizer(assetsDir: File) {
        val setup = SpeechRecognizerSetup.defaultSetup()
        setup.setKeywordThreshold(threshold)
        setup.setAcousticModel(File(assetsDir, DICTIONARY_ASSET_DIRECTORY))
        setup.setDictionary(File(assetsDir, DICTIONARY_FILE))
        speechRecognizer = setup.recognizer
        speechRecognizer?.addListener(this)
    }

    fun initialize() {
        isSphinxInitialized = true
        DataLogic.initialize(this)
        tts = TextToSpeech(applicationContext, this)
        level = DataLogic.getLevel(intensity)
        displayNext()
    }

    fun displayNext() {
        if (isSphinxInitialized && isTtsInitialized) {
            DataLogic.setLevel(intensity, level)
            dictionaryEntry = DataLogic.getDictionaryEntry(intensity, level)
            setContentView(R.layout.main)

            val levelText = findViewById<TextView>(R.id.LevelText)
            val tryText = findViewById<TextView>(R.id.TryText)
            val chineseText = findViewById<TextView>(R.id.ChineseText)
            val englishText = findViewById<TextView>(R.id.EnglishText)
            val pinyinText = findViewById<TextView>(R.id.PinyinText)
            val phonicsText = findViewById<TextView>(R.id.PhonicsText)

            if (dictionaryEntry != null) {
                if (dictionaryEntry?.ChineseWord != null &&
                    dictionaryEntry?.EnglishWord != null &&
                    dictionaryEntry?.Pinyin != null
                ) {
                    val input = "${dictionaryEntry?.Pinyin} ${dictionaryEntry?.EnglishWord}"
                    localService?.showNotification(localService!!, input)
                }

                levelText.text = String.format("%s", level)
                tryText.text = String.format("%s", tryCount)
                announce()
                listen()
                chineseText.setTextColor(Color.WHITE)
                chineseText.text = dictionaryEntry?.ChineseWord
                englishText.text = dictionaryEntry?.EnglishWord
                pinyinText.text = dictionaryEntry?.Pinyin
                phonicsText.text = dictionaryEntry?.Phonics
            } else {
                level--
                setContentView(R.layout.win)
                val winText = findViewById<TextView>(R.id.WinText)
                val text =
                    "Dear trainee, You have completed the $CHARACTER_COUNT_TEXT character Chinese language speech training! " +
                            "You have trained with $level words! Sincerely, $COMPANY_NAME"
                winText.text = text
            }
        }
    }

    fun listen() {
        speechRecognizer?.addKeyphraseSearch(
            dictionaryEntry?.ChineseWord,
            dictionaryEntry?.ChineseWord
        )
    }

    fun announce() {
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = dictionaryEntry?.ChineseWord ?: ""
        tts?.speak(dictionaryEntry?.ChineseWord ?: "", TextToSpeech.QUEUE_FLUSH, params)
    }

    fun ResetButtonOnClick(view: View) {
        level = 1
        tryCount = 0
        displayNext()
    }
}