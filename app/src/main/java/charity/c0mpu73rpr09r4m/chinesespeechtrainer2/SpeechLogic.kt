package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Context
import java.io.File
import java.io.FileOutputStream

public class SpeechLogic {
    fun copyFolder(context: Context) {
        copyAssetFolder(context, "", context.filesDir)
    }

    private fun copyAssetFolder(context: Context, assetPath: String, destDir: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: return

        for (fileName in files) {
            val fullAssetPath = if (assetPath.isEmpty()) fileName else "$assetPath/$fileName"
            val outFile = File(destDir, fileName)

            if (assetManager.list(fullAssetPath)?.isNotEmpty() == true) {
                outFile.mkdirs()
                copyAssetFolder(context, fullAssetPath, outFile)
            } else {
                assetManager.open(fullAssetPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                    }
                }
            }
        }
    }

}