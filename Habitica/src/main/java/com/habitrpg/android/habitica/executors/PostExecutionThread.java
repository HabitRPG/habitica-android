package com.habitrpg.android.habitica.executors;

import rx.Scheduler;

public interface PostExecutionThread {
    Scheduler getScheduler();
}
