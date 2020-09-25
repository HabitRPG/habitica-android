package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.HabiticaBaseApplication
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SoundManager {

    @Inject
    lateinit var soundFileLoader: SoundFileLoader
    var soundTheme: String = SoundThemeOff

    private val loadedSoundFiles: MutableMap<String, SoundFile> = HashMap()

    init {
        HabiticaBaseApplication.userComponent?.inject(this)
    }

    fun preloadAllFiles(): Maybe<List<SoundFile>> {
        if (soundTheme == SoundThemeOff) {
            return Maybe.empty()
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
        return soundFileLoader.download(soundFiles).toMaybe()
    }

    fun loadAndPlayAudio(type: String) {
        if (soundTheme == SoundThemeOff) {
            return
        }

        if (loadedSoundFiles.containsKey(type)) {
            loadedSoundFiles[type]?.play()
        } else {
            val soundFiles = ArrayList<SoundFile>()

            soundFiles.add(SoundFile(soundTheme, type))
            soundFileLoader.download(soundFiles).observeOn(Schedulers.newThread()).subscribe({
                val file = soundFiles[0]
                loadedSoundFiles[type] = file
                file.play()
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
