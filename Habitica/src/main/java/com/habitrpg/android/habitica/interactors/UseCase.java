package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;

import io.reactivex.Flowable;

public abstract class UseCase<Q extends UseCase.RequestValues, T> {

    private final PostExecutionThread postExecutionThread;

    protected UseCase(ThreadExecutor threadExecutor,
                      PostExecutionThread postExecutionThread) {
        this.postExecutionThread = postExecutionThread;
    }

    protected abstract Flowable<T> buildUseCaseObservable(Q requestValues);

    public Flowable<T> observable(Q requestValues) {
        return this.buildUseCaseObservable(requestValues)
                .subscribeOn(postExecutionThread.getScheduler())
                .observeOn(postExecutionThread.getScheduler());
    }

    public interface RequestValues {

    }
}
