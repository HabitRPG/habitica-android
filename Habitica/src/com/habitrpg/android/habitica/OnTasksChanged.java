package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.List;


public interface OnTasksChanged {
	 void onChange(List<Task> tasks);
	 void onTagFilter(List<String> tags);

}
