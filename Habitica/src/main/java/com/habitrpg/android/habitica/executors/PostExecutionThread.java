package com.habitrpg.android.habitica.executors;


import io.reactivex.rxjava3.core.Scheduler;

public interface PostExecutionThread {
    Scheduler getScheduler();
}
