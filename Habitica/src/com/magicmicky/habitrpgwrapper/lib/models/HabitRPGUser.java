package com.magicmicky.habitrpgwrapper.lib.models;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;

import java.util.List;

/**
 * Created by MagicMicky on 10/06/2014.
 */
public class HabitRPGUser {
    private String id;
    private List<Daily> dailys;
    private List<ToDo> todos;
    private List<Reward> rewards;
    private List<Habit> habits;
    private List<Tag> tags;
    private Stats stats;
    private Preferences preferences;
    private Profile profile;
    private Party party;
    private Items items;

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Daily> getDailys() {
        return dailys;
    }

    public void setDailys(List<Daily> dailys) {
        this.dailys = dailys;
    }

    public List<ToDo> getTodos() {
        return todos;
    }

    public void setTodos(List<ToDo> todos) {
        this.todos = todos;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public List<Habit> getHabits() {
        return habits;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }


}
