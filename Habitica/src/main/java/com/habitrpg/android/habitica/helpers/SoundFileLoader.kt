package com.habitrpg.android.habitica.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Okio
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

    @SuppressLint("SetWorldReadable", "ObsoleteSdkInt", "ReturnCount")
    fun download(files: List<SoundFile>): Single<List<SoundFile>> {
        return Observable.fromIterable(files)
                .flatMap({ audioFile ->
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

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            try {
                                val sink = Okio.buffer(Okio.sink(file))
                                sink.writeAll(response.body()!!.source())
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
                    }
                    fileObservable.subscribeOn(Schedulers.io())
                }, 5)
                .toList()
    }

    private fun getFullAudioFilePath(soundFile: SoundFile): String =
            externalCacheDir + File.separator + soundFile.filePath
}