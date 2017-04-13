package com.habitrpg.android.habitica.interactors;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.models.TaskDirectionData;

import javax.inject.Inject;

import rx.Observable;

import static com.habitrpg.android.habitica.ui.helpers.UiUtils.showSnackbar;

public class DisplayItemDropUseCase extends UseCase<DisplayItemDropUseCase.RequestValues, Void> {

    private SoundManager soundManager;

    @Inject
    public DisplayItemDropUseCase(SoundManager soundManager, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.soundManager = soundManager;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable(RequestValues requestValues) {
        return Observable.from(() -> {
            TaskDirectionData data = requestValues.data;

            if (data.get_tmp() != null) {
                if (data.get_tmp().getDrop() != null) {
                    new Handler().postDelayed(() -> {
                        showSnackbar(requestValues.context, requestValues.snackbarTargetView,
                                data.get_tmp().getDrop().getDialog(), UiUtils.SnackbarDisplayType.DROP);
                        soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop);
                    }, 3000L);

                }
            }

            return null;
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private TaskDirectionData data;
        private AppCompatActivity context;
        private View snackbarTargetView;

        public RequestValues(TaskDirectionData data, AppCompatActivity context, View snackbarTargetView) {
            this.data = data;
            this.context = context;
            this.snackbarTargetView = snackbarTargetView;
        }
    }
}