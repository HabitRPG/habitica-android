package com.habitrpg.android.habitica.interactors;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.ShareEvent;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.SuppressedModals;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.AvatarView;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.reactivex.Flowable;

public class LevelUpUseCase extends UseCase<LevelUpUseCase.RequestValues, Stats> {

    private SoundManager soundManager;
    private CheckClassSelectionUseCase checkClassSelectionUseCase;

    @Inject
    public LevelUpUseCase(SoundManager soundManager, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread,
                          CheckClassSelectionUseCase checkClassSelectionUseCase) {
        super(threadExecutor, postExecutionThread);
        this.soundManager = soundManager;
        this.checkClassSelectionUseCase = checkClassSelectionUseCase;
    }

    @Override
    protected Flowable<Stats> buildUseCaseObservable(RequestValues requestValues) {
        return Flowable.defer(() -> {
            soundManager.loadAndPlayAudio(SoundManager.SoundLevelUp);

            SuppressedModals suppressedModals = requestValues.user.getPreferences().getSuppressModals();
            if (suppressedModals != null) {
                if (suppressedModals.getLevelUp()) {
                    checkClassSelectionUseCase.observable(new CheckClassSelectionUseCase.RequestValues(requestValues.user, null, requestValues.activity))
                            .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());

                    return Flowable.just(requestValues.user.getStats());
                }
            }

            View customView = requestValues.activity.getLayoutInflater().inflate(R.layout.dialog_levelup, null);
            if (customView != null) {
                TextView detailView = customView.findViewById(R.id.levelupDetail);
                detailView.setText(requestValues.activity.getString(R.string.levelup_detail, requestValues.newLevel));
                AvatarView dialogAvatarView = customView.findViewById(R.id.avatarView);
                dialogAvatarView.setAvatar(requestValues.user);
            }

            final ShareEvent event = new ShareEvent();
            event.sharedMessage = requestValues.activity.getString(R.string.share_levelup, requestValues.newLevel) + " https://habitica.com/social/level-up";
            AvatarView avatarView = new AvatarView(requestValues.activity, true, true, true);
            avatarView.setAvatar(requestValues.user);
            avatarView.onAvatarImageReady(avatarImage -> event.shareImage = avatarImage);

            AlertDialog alert = new AlertDialog.Builder(requestValues.activity)
                    .setTitle(R.string.levelup_header)
                    .setView(customView)
                    .setPositiveButton(R.string.levelup_button, (dialog, which) -> checkClassSelectionUseCase.observable(new CheckClassSelectionUseCase.RequestValues(requestValues.user, null, requestValues.activity))
                            .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError()))
                    .setNeutralButton(R.string.share, (dialog, which) -> {
                        EventBus.getDefault().post(event);
                        dialog.dismiss();
                    })
                    .create();

            if (!requestValues.activity.isFinishing()) {
                alert.show();
            }

            return Flowable.just(requestValues.user.getStats());

        });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private User user;
        private int newLevel;
        private Activity activity;

        public RequestValues(User user, AppCompatActivity activity) {
            this.user = user;
            this.newLevel = user.getStats().getLvl();
            this.activity = activity;
        }
    }
}
