package com.habitrpg.android.habitica.helpers

import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.MainScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager
@Inject
constructor(var soundFileLoader: SoundFileLoader) {
    var soundTheme: String = SOUND_THEME_OFF

    private val loadedSoundFiles: MutableMap<String, SoundFile> = HashMap()

    fun preloadAllFiles() {
        loadedSoundFiles.clear()
        if (soundTheme == SOUND_THEME_OFF) {
            return
        }

        val soundFiles = ArrayList<SoundFile>()
        soundFiles.add(SoundFile(soundTheme, SOUND_ACHIEVEMENT_UNLOCKED))
        soundFiles.add(SoundFile(soundTheme, SOUND_CHAT))
        soundFiles.add(SoundFile(soundTheme, SOUND_DAILY))
        soundFiles.add(SoundFile(soundTheme, SOUND_DEATH))
        soundFiles.add(SoundFile(soundTheme, SOUND_ITEM_DROP))
        soundFiles.add(SoundFile(soundTheme, SOUND_LEVEL_UP))
        soundFiles.add(SoundFile(soundTheme, SOUND_MINUS_HABIT))
        soundFiles.add(SoundFile(soundTheme, SOUND_PLUS_HABIT))
        soundFiles.add(SoundFile(soundTheme, SOUND_REWARD))
        soundFiles.add(SoundFile(soundTheme, SOUND_TODO))
        MainScope().launchCatching {
            soundFileLoader.download(soundFiles)
        }
    }

    fun loadAndPlayAudio(type: String) {
        if (soundTheme == SOUND_THEME_OFF) {
            return
        }

        if (loadedSoundFiles.containsKey(type)) {
            loadedSoundFiles[type]?.play()
        } else {
            val soundFiles = ArrayList<SoundFile>()

            soundFiles.add(SoundFile(soundTheme, type))
            MainScope().launchCatching {
                val newFiles = soundFileLoader.download(soundFiles)
                val file = newFiles[0]
                loadedSoundFiles[type] = file
                file.play()
            }
        }
    }

    companion object {
        const val SOUND_ACHIEVEMENT_UNLOCKED = "Achievement_Unlocked"
        const val SOUND_CHAT = "Chat"
        const val SOUND_DAILY = "Daily"
        const val SOUND_DEATH = "Death"
        const val SOUND_ITEM_DROP = "Item_Drop"
        const val SOUND_LEVEL_UP = "Level_Up"
        const val SOUND_MINUS_HABIT = "Minus_Habit"
        const val SOUND_PLUS_HABIT = "Plus_Habit"
        const val SOUND_REWARD = "Reward"
        const val SOUND_TODO = "Todo"
        const val SOUND_THEME_OFF = "off"
    }
}
