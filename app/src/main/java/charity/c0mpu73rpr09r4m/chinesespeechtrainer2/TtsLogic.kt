package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsLogic(context: Context, listener: TextToSpeech.OnInitListener) {
    var tts: TextToSpeech = TextToSpeech(context, listener)

    init{
        val setLanguage = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)

         if (setLanguage == TextToSpeech.LANG_MISSING_DATA || setLanguage == TextToSpeech.LANG_NOT_SUPPORTED) {
             tts.language = Locale.CHINESE
         }
     }

    fun announce(translation: Translation?) {
        translation?.englishWord?.let { english ->
            tts.language = Locale.ENGLISH
            tts.speak(
                english,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "UTTERANCE_ID_ENGLISH"
            )
            translation.chineseWord.let { chinese ->
                val languageCode = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
                if (languageCode == TextToSpeech.LANG_MISSING_DATA ||
                    languageCode == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts.language = Locale.CHINESE
                }
                tts.speak(
                    chinese,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "UTTERANCE_ID_CHINESE"
                )
            }
        }
    }
}