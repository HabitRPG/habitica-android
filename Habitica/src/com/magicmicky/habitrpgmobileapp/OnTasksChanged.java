package com.magicmicky.habitrpgmobileapp;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;

import java.util.List;


public interface OnTasksChanged {
	 void onChange(List<HabitItem> tasks);
	 void onTagFilter(List<String> tags);

}
