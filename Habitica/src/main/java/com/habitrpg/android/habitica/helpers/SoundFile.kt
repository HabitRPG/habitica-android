package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import java.io.File


class SoundFile(val theme: String, private val fileName: String) : MediaPlayer.OnCompletionListener {
    var file: File? = null
    private var playerPrepared: Boolean = false
    private var isPlaying: Boolean = false

    val webUrl: String
        get() = "https://habitica.com/static/audio/$theme/$fileName.mp3"

    val filePath: String
        get() = theme + "_" + fileName + ".mp3"

    init {
    }

    fun play(context: Context) {
        if (isPlaying) {
            return
        }

        val m = MediaPlayer()

        m.setOnCompletionListener { mp ->
            isPlaying = false
            mp.release()
        }

        try {
            m.setDataSource(file?.path)
            m.prepare()

            playerPrepared = true
            m.setVolume(100f, 100f)
            m.isLooping = false
            isPlaying = true
            m.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        isPlaying = false
    }
}
