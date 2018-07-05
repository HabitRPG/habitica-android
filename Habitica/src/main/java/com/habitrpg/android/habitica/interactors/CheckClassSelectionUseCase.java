package com.habitrpg.android.habitica.interactors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.habitrpg.android.habitica.events.SelectClassEvent;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity;

import javax.inject.Inject;

import io.reactivex.Flowable;

import static com.habitrpg.android.habitica.ui.activities.MainActivity.SELECT_CLASS_RESULT;

public class CheckClassSelectionUseCase extends UseCase<CheckClassSelectionUseCase.RequestValues, Void> {
    @Inject
    public CheckClassSelectionUseCase(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @Override
    protected Flowable<Void> buildUseCaseObservable(RequestValues requestValues) {
        return Flowable.defer(() -> {
            User user = requestValues.user;

            if(requestValues.selectClassEvent == null) {
                if (user.getStats().getLvl() >= 10 &&
                        !user.getPreferences().getDisableClasses() &&
                        !user.getFlags().getClassSelected()) {
                    SelectClassEvent event = new SelectClassEvent();
                    event.isInitialSelection = true;
                    event.currentClass = user.getStats().getHabitClass();
                    displayClassSelectionActivity(user, event, requestValues.activity);
                }
            } else {
                displayClassSelectionActivity(user, requestValues.selectClassEvent, requestValues.activity);
            }

            return Flowable.empty();
        });
    }

    private void displayClassSelectionActivity(User user, SelectClassEvent event, Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putString("size", user.getPreferences().getSize());
        bundle.putString("skin", user.getPreferences().getSkin());
        bundle.putString("shirt", user.getPreferences().getShirt());
        bundle.putInt("hairBangs", user.getPreferences().getHair().getBangs());
        bundle.putInt("hairBase", user.getPreferences().getHair().getBase());
        bundle.putString("hairColor", user.getPreferences().getHair().getColor());
        bundle.putInt("hairMustache", user.getPreferences().getHair().getMustache());
        bundle.putInt("hairBeard", user.getPreferences().getHair().getBeard());
        bundle.putBoolean("isInitialSelection", event.isInitialSelection);
        bundle.putString("currentClass", event.currentClass);

        Intent intent = new Intent(activity, ClassSelectionActivity.class);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, SELECT_CLASS_RESULT);
    }

    public static final class RequestValues implements UseCase.RequestValues {


        private final Activity activity;
        private User user;
        private SelectClassEvent selectClassEvent;

        public RequestValues(User user, SelectClassEvent selectClassEvent, Activity activity) {

            this.user = user;
            this.selectClassEvent = selectClassEvent;
            this.activity = activity;
        }
    }
}
