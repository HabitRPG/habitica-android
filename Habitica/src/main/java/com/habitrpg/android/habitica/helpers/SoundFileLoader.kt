package com.habitrpg.android.habitica.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

// based on http://stackoverflow.com/questions/29838565/downloading-files-using-okhttp-okio-and-rxjava
class SoundFileLoader(private val context: Context) {
    private val client: OkHttpClient = OkHttpClient()

    private val externalCacheDir: String?
        get() {
            val cacheDir = HabiticaBaseApplication.getInstance(context)?.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS)
            return cacheDir?.path
        }

    @SuppressLint("SetWorldReadable", "ReturnCount")
    suspend fun download(files: List<SoundFile>): List<SoundFile> {
        return files.map { audioFile ->
            withContext(Dispatchers.IO) {
                val file = File(getFullAudioFilePath(audioFile))
                if (file.exists() && file.length() > 5000) {
                    // Important, or else the MediaPlayer can't access this file
                    file.setReadable(true, false)
                    audioFile.file = file
                    return@withContext audioFile
                }
                val request = Request.Builder().url(audioFile.webUrl).build()

                val response: Response
                try {
                    response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw IOException()
                    }
                } catch (io: IOException) {
                    return@withContext audioFile
                }

                try {
                    val sink = file.sink().buffer()
                    sink.writeAll(response.body!!.source())
                    sink.flush()
                    sink.close()
                } catch (io: IOException) {
                    return@withContext audioFile
                }

                file.setReadable(true, false)
                audioFile.file = file
                return@withContext audioFile
            }
        }
    }

    private fun getFullAudioFilePath(soundFile: SoundFile): String =
        externalCacheDir + File.separator + soundFile.filePath
}
