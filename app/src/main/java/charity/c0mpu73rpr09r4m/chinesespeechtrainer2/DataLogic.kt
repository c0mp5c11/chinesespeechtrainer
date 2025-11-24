package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

public class DataLogic {
    private val fileName = "database.db"

    fun getDatabase(context: Context): SQLiteDatabase {
        var dbPath = copyDatabase(context)
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    fun getTranslation(context: Context, index: Int): Translation? {
        var result: Translation? = null
        val sql = "SELECT CHINESEWORD, ENGLISHWORD, PINYIN FROM TRANSLATION WHERE ChineseWordCount = ? ORDER BY ENGLISHWORD ASC LIMIT 1 OFFSET $index"
        val selectionArgs = arrayOf("2")
        val db = getDatabase(context)
        val cursor: Cursor = db.rawQuery(sql, selectionArgs)

        if (cursor.count > 0) {
            cursor.moveToNext()
            result = Translation(cursor.getString(0), cursor.getString(1), cursor.getString(2))
        }

        cursor.close()

        return result
    }

    fun copyDatabase(context: Context): String {
        val file = context.getDatabasePath(fileName)

        if (!file.exists()) {
            file.parentFile?.mkdirs()

            context.assets.open(fileName).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return file.path
    }

    fun getWordIndex(context: Context): Int {
        var result = 0
        val sql = "SELECT WordIndex FROM Cache"
        val db = getDatabase(context)
        val cursor: Cursor = db.rawQuery(sql, null)

        if (cursor.moveToFirst()) {
            result = cursor.getInt(0)
        }

        cursor.close()

        return result
    }


    fun setWordIndex(context: Context, index: Int) {
        val db = getDatabase(context)
        db.execSQL("DELETE FROM Cache")
        val sql = "INSERT INTO Cache (WordIndex) VALUES (?)"
        db.execSQL(sql, arrayOf(index))
    }
}