package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.HabiticaBaseApplication

import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import rx.Observable
import rx.functions.Action1
import rx.schedulers.Schedulers

class SoundManager {

    @Inject
    lateinit var soundFileLoader: SoundFileLoader
    @Inject
    lateinit var context: Context
    var soundTheme: String = SoundThemeOff

    private val loadedSoundFiles: MutableMap<String, SoundFile> = HashMap()

    init {
        HabiticaBaseApplication.getComponent().inject(this)
    }

    fun preloadAllFiles(): Observable<List<SoundFile>> {
        if (soundTheme == SoundThemeOff) {
            return Observable.empty()
        }

        val soundFiles = ArrayList<SoundFile>()
        soundFiles.add(SoundFile(soundTheme, SoundAchievementUnlocked))
        soundFiles.add(SoundFile(soundTheme, SoundChat))
        soundFiles.add(SoundFile(soundTheme, SoundDaily))
        soundFiles.add(SoundFile(soundTheme, SoundDeath))
        soundFiles.add(SoundFile(soundTheme, SoundItemDrop))
        soundFiles.add(SoundFile(soundTheme, SoundLevelUp))
        soundFiles.add(SoundFile(soundTheme, SoundMinusHabit))
        soundFiles.add(SoundFile(soundTheme, SoundPlusHabit))
        soundFiles.add(SoundFile(soundTheme, SoundReward))
        soundFiles.add(SoundFile(soundTheme, SoundTodo))
        return soundFileLoader.download(soundFiles)
    }

    fun loadAndPlayAudio(type: String) {
        if (soundTheme == SoundThemeOff) {
            return
        }

        if (loadedSoundFiles.containsKey(type)) {
            loadedSoundFiles[type]?.play(context)
        } else {
            val soundFiles = ArrayList<SoundFile>()

            soundFiles.add(SoundFile(soundTheme, type))
            soundFileLoader.download(soundFiles).observeOn(Schedulers.newThread()).subscribe(Action1 {
                val file = soundFiles[0]
                loadedSoundFiles.put(type, file)
                file.play(context)
            }, RxErrorHandler.handleEmptyError())
        }
    }

    companion object {
        const val SoundAchievementUnlocked = "Achievement_Unlocked"
        const val SoundChat = "Chat"
        const val SoundDaily = "Daily"
        const val SoundDeath = "Death"
        const val SoundItemDrop = "Item_Drop"
        const val SoundLevelUp = "Level_Up"
        const val SoundMinusHabit = "Minus_Habit"
        const val SoundPlusHabit = "Plus_Habit"
        const val SoundReward = "Reward"
        const val SoundTodo = "ToDo"
        const val SoundThemeOff = "off"
    }

}
