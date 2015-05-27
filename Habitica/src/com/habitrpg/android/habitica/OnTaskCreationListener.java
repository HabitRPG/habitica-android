package com.habitrpg.android.habitica;


import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;

public interface OnTaskCreationListener {
	public void onTaskCreation(HabitItem task, boolean editMode);
	public void onTaskCreationFail(String message);
}
