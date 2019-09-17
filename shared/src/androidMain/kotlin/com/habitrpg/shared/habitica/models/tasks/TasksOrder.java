package com.habitrpg.shared.habitica.models.tasks;

import java.util.List;

public class TasksOrder {

    List<String> habits;
    List<String> dailys;
    List<String> todos;
    List<String> rewards;

    public List<String> getHabits() {
        return habits;
    }

    public void setHabits(List<String> habits) {
        this.habits = habits;
    }

    public List<String> getDailys() {
        return dailys;
    }

    public void setDailys(List<String> dailys) {
        this.dailys = dailys;
    }

    public List<String> getTodos() {
        return todos;
    }

    public void setTodos(List<String> todos) {
        this.todos = todos;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public void setRewards(List<String> rewards) {
        this.rewards = rewards;
    }
}
