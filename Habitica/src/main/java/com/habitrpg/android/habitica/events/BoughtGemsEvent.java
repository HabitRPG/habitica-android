package com.habitrpg.android.habitica.events;

/**
 * Created by Negue on 29.11.2015.
 */
public class BoughtGemsEvent {
    public int NewGemsToAdd;

    public BoughtGemsEvent(int newGemsToAdd) {
        NewGemsToAdd = newGemsToAdd;
    }
}
