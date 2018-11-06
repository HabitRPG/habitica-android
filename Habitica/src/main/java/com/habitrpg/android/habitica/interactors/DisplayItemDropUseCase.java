package com.habitrpg.android.habitica.interactors;

import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;

import javax.inject.Inject;

import io.reactivex.Flowable;

public class DisplayItemDropUseCase extends UseCase<DisplayItemDropUseCase.RequestValues, Void> {

    private SoundManager soundManager;

    @Inject
    public DisplayItemDropUseCase(SoundManager soundManager, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.soundManager = soundManager;
    }

    @Override
    protected Flowable<Void> buildUseCaseObservable(RequestValues requestValues) {
        return Flowable.defer(() -> {
            TaskScoringResult data = requestValues.data;

            if (data != null) {
                if (data.getDrop() != null) {
                    new Handler().postDelayed(() -> {
                        HabiticaSnackbar.Companion.showSnackbar(requestValues.snackbarTargetView,
                                data.getDrop().getDialog(), HabiticaSnackbar.SnackbarDisplayType.DROP);
                        soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop);
                    }, 3000L);
                }
            }

            return Flowable.empty();
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private TaskScoringResult data;
        private AppCompatActivity context;
        private ViewGroup snackbarTargetView;

        public RequestValues(TaskScoringResult data, AppCompatActivity context, ViewGroup snackbarTargetView) {
            this.data = data;
            this.context = context;
            this.snackbarTargetView = snackbarTargetView;
        }
    }
}