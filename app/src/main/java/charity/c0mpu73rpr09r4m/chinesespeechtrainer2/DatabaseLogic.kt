package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object DataLogic {
    private const val DATABASE_FILENAME = "database.db"
    private var DATABASE_PATH: String = ""

    fun initialize(context: Context) {
        val file: File = context.getDatabasePath(DATABASE_FILENAME)
        DATABASE_PATH = file.absolutePath

        if (!file.exists()) {
            try {
                FileOutputStream(file).use { outputStream ->
                    context.assets.open(DATABASE_FILENAME).use { inputStream ->
                        val buffer = ByteArray(1024)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("ERROR", e.toString())
            }
        }
    }

    fun getDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(DATABASE_PATH, null, 0)
    }

    fun getDictionaryEntry(intensity: Int, level: Int): DictionaryEntry? {
        var result: DictionaryEntry? = null
        val sql = "SELECT CHINESEWORD, ENGLISHWORD, PHONICS, PINYIN " +
                "FROM TRANSLATION ORDER BY ENGLISHWORD ASC LIMIT 1 OFFSET ${level - 1}"
        val db = getDatabase()
        val cursor: Cursor = db.rawQuery(sql, null)

        if (cursor.count > 0) {
            cursor.moveToNext()
            result = DictionaryEntry().apply {
                ChineseWord = cursor.getString(0)
                EnglishWord = cursor.getString(1)
                Phonics = cursor.getString(2)
                Pinyin = cursor.getString(3)
            }
        }
        cursor.close()
        return result
    }

    fun setLevel(intensity: Int, level: Int) {
        val sql = "UPDATE IntensityLevel SET Level = ? WHERE intensity = ?"
        val args = arrayOf(level.toString(), intensity.toString())
        val db = getDatabase()
        db.execSQL(sql, args)
    }

    fun getLevel(intensity: Int): Int {
        var result = 0
        val sql = "SELECT level FROM intensitylevel WHERE intensity = ?"
        val args = arrayOf(intensity.toString())
        val db = getDatabase()
        val cursor: Cursor = db.rawQuery(sql, args)

        if (cursor.count > 0) {
            cursor.moveToNext()
            result = cursor.getInt(0)
        }
        cursor.close()
        return result
    }
}