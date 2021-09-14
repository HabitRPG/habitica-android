package com.habitrpg.android.habitica.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
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
    fun download(files: List<SoundFile>): Single<List<SoundFile>> {
        return Observable.fromIterable(files)
            .flatMap(
                { audioFile ->
                    val file = File(getFullAudioFilePath(audioFile))
                    if (file.exists() && file.length() > 5000) {
                        // Important, or else the MediaPlayer can't access this file
                        file.setReadable(true, false)
                        audioFile.file = file
                        return@flatMap Observable.just(audioFile)
                    }

                    val fileObservable = Observable.create<SoundFile> { sub ->
                        val request = Request.Builder().url(audioFile.webUrl).build()

                        val response: Response
                        try {
                            response = client.newCall(request).execute()
                            if (!response.isSuccessful) {
                                throw IOException()
                            }
                        } catch (io: IOException) {
                            sub.onComplete()
                            return@create
                        }

                        try {
                            val sink = file.sink().buffer()
                            sink.writeAll(response.body!!.source())
                            sink.flush()
                            sink.close()
                        } catch (io: IOException) {
                            sub.onComplete()
                            return@create
                        }

                        file.setReadable(true, false)
                        audioFile.file = file
                        sub.onNext(audioFile)
                        sub.onComplete()
                    }
                    fileObservable.subscribeOn(Schedulers.io())
                },
                5
            )
            .toList()
    }

    private fun getFullAudioFilePath(soundFile: SoundFile): String =
        externalCacheDir + File.separator + soundFile.filePath
}
