package com.habitrpg.android.habitica.events.commands;

/**
 * Created by maia on 2017/06/04.
 */

public class ShowConnectionProblemCommand {
    public String title;
    public String message;

    public ShowConnectionProblemCommand(String title, String message) {
        this.title = title;
        this.message = message;

    }
}
