package com.habitrpg.android.habitica.helpers

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import java.io.File

class SoundFile(val theme: String, private val fileName: String) : MediaPlayer.OnCompletionListener {
    var file: File? = null
    private var playerPrepared: Boolean = false
    private var isPlaying: Boolean = false

    val webUrl: String
        get() = "https://s3.amazonaws.com/habitica-assets/mobileApp/sounds/$theme/$fileName.mp3"

    val filePath: String
        get() = theme + "_" + fileName + ".mp3"

    fun play() {
        if (isPlaying || file?.path == null) {
            return
        }

        val m = MediaPlayer()

        m.setOnCompletionListener { mp ->
            isPlaying = false
            mp.release()
        }

        try {
            m.setDataSource(file?.path)
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .build()
            m.setAudioAttributes(attributes)
            m.prepare()

            playerPrepared = true
            m.setVolume(100f, 100f)
            m.isLooping = false
            isPlaying = true
            m.start()
        } catch (e: Exception) {
            RxErrorHandler.reportError(e)
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        isPlaying = false
    }
}
