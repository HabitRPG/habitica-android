package com.habitrpg.android.habitica;


import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

public interface OnTaskCreationListener {
	public void onTaskCreation(Task task, boolean editMode);
	public void onTaskCreationFail(String message);
}
