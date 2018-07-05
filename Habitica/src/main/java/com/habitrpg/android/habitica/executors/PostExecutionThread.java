package com.habitrpg.android.habitica.executors;


import io.reactivex.Scheduler;

public interface PostExecutionThread {
    Scheduler getScheduler();
}
