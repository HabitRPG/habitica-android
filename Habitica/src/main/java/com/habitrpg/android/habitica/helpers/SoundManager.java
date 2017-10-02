package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.HabiticaBaseApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;

public class SoundManager {
    public static String SoundAchievementUnlocked = "Achievement_Unlocked";
    public static String SoundChat = "Chat";
    public static String SoundDaily = "Daily";
    public static String SoundDeath = "Death";
    public static String SoundItemDrop = "Item_Drop";
    public static String SoundLevelUp = "Level_Up";
    public static String SoundMinusHabit = "Minus_Habit";
    public static String SoundPlusHabit = "Plus_Habit";
    public static String SoundReward = "Reward";
    public static String SoundTodo = "ToDo";

    @Inject
    SoundFileLoader soundFileLoader;
    private String soundTheme;

    private HashMap<String, SoundFile> loadedSoundFiles;

    public SoundManager() {
        HabiticaBaseApplication.getComponent().inject(this);

        loadedSoundFiles = new HashMap<>();
    }

    public void setSoundTheme(String soundTheme) {
        this.soundTheme = soundTheme;
    }

    public Observable<List<SoundFile>> preloadAllFiles() {
        if (soundTheme.equals("off")) {
            return Observable.empty();
        }

        ArrayList<SoundFile> soundFiles = new ArrayList<>();

        soundFiles.add(new SoundFile(soundTheme, SoundAchievementUnlocked));
        soundFiles.add(new SoundFile(soundTheme, SoundChat));
        soundFiles.add(new SoundFile(soundTheme, SoundDaily));
        soundFiles.add(new SoundFile(soundTheme, SoundDeath));
        soundFiles.add(new SoundFile(soundTheme, SoundItemDrop));
        soundFiles.add(new SoundFile(soundTheme, SoundLevelUp));
        soundFiles.add(new SoundFile(soundTheme, SoundMinusHabit));
        soundFiles.add(new SoundFile(soundTheme, SoundPlusHabit));
        soundFiles.add(new SoundFile(soundTheme, SoundReward));
        soundFiles.add(new SoundFile(soundTheme, SoundTodo));
        return soundFileLoader.download(soundFiles);
    }

    public void clearLoadedFiles() {
        loadedSoundFiles.clear();
    }

    public void loadAndPlayAudio(String type) {
        if ("off".equals(soundTheme) || soundTheme == null) {
            return;
        }

        if (loadedSoundFiles.containsKey(type)) {
            loadedSoundFiles.get(type).play();
        } else {
            ArrayList<SoundFile> soundFiles = new ArrayList<>();

            soundFiles.add(new SoundFile(soundTheme, type));
            soundFileLoader.download(soundFiles).observeOn(Schedulers.newThread()).subscribe(audioFiles1 -> {
                SoundFile file = soundFiles.get(0);

                loadedSoundFiles.put(type, file);
                file.play();

            }, RxErrorHandler.handleEmptyError());
        }
    }

}
