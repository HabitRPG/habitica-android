package com.habitrpg.android.habitica.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import com.habitrpg.android.habitica.HabiticaApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Okio
import rx.Observable
import rx.exceptions.OnErrorThrowable
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.*

// based on http://stackoverflow.com/questions/29838565/downloading-files-using-okhttp-okio-and-rxjava
class SoundFileLoader(private val context: Context) {
    private val client: OkHttpClient = OkHttpClient()

    private val externalCacheDir: String?
        get() {
            val cacheDir = HabiticaApplication.getInstance(context).getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS)
            return cacheDir?.path
        }

    @SuppressLint("SetWorldReadable", "ObsoleteSdkInt")
    fun download(files: List<SoundFile>): Observable<List<SoundFile>> {
        return Observable.from(files)
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
                            throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, audioFile))
                        }

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            try {
                                val sink = Okio.buffer(Okio.sink(file))
                                sink.writeAll(response.body()!!.source())
                                sink.flush()
                                sink.close()
                            } catch (io: IOException) {
                                throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, audioFile))
                            }

                            file.setReadable(true, false)
                            audioFile.file = file
                            sub.onNext(audioFile)
                            sub.onCompleted()
                        }
                    }
                    fileObservable.subscribeOn(Schedulers.io())
                }, 5)
                .toList()
                .map({ ArrayList(it) })
    }

    private fun getFullAudioFilePath(soundFile: SoundFile): String =
            externalCacheDir + File.separator + soundFile.filePath
}