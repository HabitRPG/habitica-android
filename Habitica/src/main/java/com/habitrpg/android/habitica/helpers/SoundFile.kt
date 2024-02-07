package com.habitrpg.android.habitica.helpers

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import java.io.File

class SoundFile(val theme: String, private val fileName: String) {
    private var player: MediaPlayer? = null
    var file: File? = null
    private var playerPrepared: Boolean = false

    val webUrl: String
        get() = "https://s3.amazonaws.com/habitica-assets/mobileApp/sounds/$theme/$fileName.mp3"

    val filePath: String
        get() = theme + "_" + fileName + ".mp3"

    fun play() {
        if (player?.isPlaying == true || file?.path == null) {
            return
        }

        if (player?.isPlaying == false) {
            player?.release()
            player = null
        }

        player = MediaPlayer()

        player?.setOnCompletionListener { mp ->
            mp.release()
            player = null
        }

        try {
            player?.setDataSource(file?.path)
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            player?.setAudioAttributes(attributes)
            player?.prepare()

            playerPrepared = true
            player?.setVolume(100f, 100f)
            player?.isLooping = false
            player?.start()
        } catch (_: IllegalStateException) {
        } catch (e: Exception) {
            ExceptionHandler.reportError(e)
        }
    }
}
