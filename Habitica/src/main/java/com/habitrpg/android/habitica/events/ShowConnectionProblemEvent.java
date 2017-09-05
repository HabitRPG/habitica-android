package com.habitrpg.android.habitica.events;

/**
 * Created by phillip on 25.07.17.
 */

public class ShowConnectionProblemEvent {
    public String title;
    public String message;

    public ShowConnectionProblemEvent(String title, String message) {
        this.title = title;
        this.message = message;

    }
}
