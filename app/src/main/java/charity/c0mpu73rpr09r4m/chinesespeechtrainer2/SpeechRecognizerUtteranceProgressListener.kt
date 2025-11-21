package charity.c0mpu73rpr09r4m.chinesespeechtrainer

import android.speech.tts.UtteranceProgressListener
import charity.c0mpu73rpr09r4m.chinesespeechtrainer2.MainActivity
import edu.cmu.pocketsphinx.SpeechRecognizer

class SpeechRecognizerUtteranceProgressListener(
    private val speechRecognizer: SpeechRecognizer
) : UtteranceProgressListener() {

    override fun onDone(utteranceId: String?) {
        // Safely access dictionaryEntry and its ChineseWord
        MainActivity.dictionaryEntry?.ChineseWord?.let {
            speechRecognizer.startListening(it)
        }
    }

    override fun onError(utteranceId: String?) {
        // intentionally left blank
    }

    override fun onStart(utteranceId: String?) {
        // intentionally left blank
    }
}