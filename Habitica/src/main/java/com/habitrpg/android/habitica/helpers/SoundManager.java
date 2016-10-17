package com.habitrpg.android.habitica.helpers;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.habitrpg.android.habitica.HabiticaApplication;

import java.io.File;
import java.util.ArrayList;
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

    private MediaPlayer mp = new MediaPlayer();
    private String currentSound = "";

    public SoundManager(){
        HabiticaApplication.getInstance(HabiticaApplication.currentActivity).getComponent().inject(this);
    }

    public void setSoundTheme(String soundTheme){
        this.soundTheme = soundTheme;
    }

    public Observable<List<SoundFile>> preloadAllFiles() {
        if(soundTheme == "off")
        {
            return Observable.never();
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

    public void loadAndPlayAudio(String type){
        if(soundTheme == "off")
        {
            return;
        }

        ArrayList<SoundFile> soundFiles = new ArrayList<>();

        String soundFileKey = soundTheme+"_"+type;

        if(currentSound == soundFileKey){
            mp.start();
        } else {
            soundFiles.add(new SoundFile(soundTheme, type));
            soundFileLoader.download(soundFiles).observeOn(Schedulers.newThread()).subscribe(audioFiles1 -> {
                File file = soundFiles.get(0).getFile();
                String path = file.getPath();

                try {
                    mp.setDataSource(path);
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.prepare();
                    currentSound = soundFileKey;
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                mp.start();
            });
        }
    }

}
