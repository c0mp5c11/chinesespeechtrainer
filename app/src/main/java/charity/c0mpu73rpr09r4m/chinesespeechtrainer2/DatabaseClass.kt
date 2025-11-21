package charity.c0mpu73rpr09r4m.chinesespeechtrainer2

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import edu.cmu.pocketsphinx.Assets
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

class DatabaseTask(activity: MainActivity) : AsyncTask<Void, Void, Exception?>() {

    private val activityReference: WeakReference<MainActivity> = WeakReference(activity)

    companion object {
        var context: Context? = null
    }

    override fun doInBackground(vararg params: Void?): Exception? {
        return try {
            val assets = Assets(activityReference.get())
            val assetDir: File = assets.syncAssets()
            //activityReference.get()?.setupRecognizer(assetDir)
            null
        } catch (e: IOException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
            e
        }
    }

    override fun onPostExecute(result: Exception?) {
        if (result != null) {
            Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show()
        } else {
            //activityReference.get()?.initialize()
        }
    }
}