package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;

import io.reactivex.Flowable;

public abstract class UseCase<Q extends UseCase.RequestValues, T> {

    private final ThreadExecutor threadExecutor;
    private final PostExecutionThread postExecutionThread;

    protected UseCase(ThreadExecutor threadExecutor,
                      PostExecutionThread postExecutionThread) {
        this.threadExecutor = threadExecutor;
        this.postExecutionThread = postExecutionThread;
    }

    /**
     * Builds an {@link rx.Flowable} which will be used when executing the current {@link UseCase}.
     */
    protected abstract Flowable<T> buildUseCaseObservable(Q requestValues);

    @SuppressWarnings("unchecked")
    public Flowable<T> observable(Q requestValues) {
        return this.buildUseCaseObservable(requestValues)
                .subscribeOn(postExecutionThread.getScheduler())
                .observeOn(postExecutionThread.getScheduler());
    }

    public interface RequestValues {

    }
}
