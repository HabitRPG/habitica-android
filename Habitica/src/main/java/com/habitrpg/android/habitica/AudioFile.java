package com.habitrpg.android.habitica;

import java.io.File;

public class AudioFile{
    private String theme;
    private String fileName;
    private File file;

    public AudioFile(String theme, String fileName){

        this.theme = theme;
        this.fileName = fileName;
    }

    public String getTheme() {
        return theme;
    }

    public String getFileName() {
        return fileName;
    }

    public String getWebUrl(){
        return "https://github.com/HabitRPG/habitica/blob/develop/website/assets/audio/"+
                getTheme()+"/"+getFileName()+".mp3";
    }

    public String getFilePath() {
        return getTheme()+"_"+getFileName()+".mp3";
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
