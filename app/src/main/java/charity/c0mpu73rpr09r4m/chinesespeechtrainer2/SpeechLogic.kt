package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Context
import java.io.File
import java.io.IOException

public class SpeechLogic {
    private val assetFolderName = "zh_cn.cd_cont_5000"
    private val targetFolderName = "pocketsphinx"

    fun copyFolder(context: Context) : File? {
        var result : File? = null

        try {
            val assetManager = context.assets
            val files = assetManager.list(assetFolderName)

            if (files != null) {
                result = File(context.filesDir, targetFolderName)

                if (!result.exists()) {
                    result.mkdirs()
                }

                for (fileName in files) {
                    val assetPath = "$assetFolderName/$fileName"
                    val outFile = File(result, fileName)

                    assetManager.open(assetPath).use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result;
    }
}