package com.habitrpg.android.habitica.interactors;

import android.content.Intent;
import android.os.Bundle;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.events.SelectClassEvent;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity;
import com.habitrpg.android.habitica.models.HabitRPGUser;

import javax.inject.Inject;

import rx.Observable;

import static com.habitrpg.android.habitica.ui.activities.MainActivity.SELECT_CLASS_RESULT;

public class CheckClassSelectionUseCase extends UseCase<CheckClassSelectionUseCase.RequestValues, Void> {
    @Inject
    public CheckClassSelectionUseCase(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @Override
    protected Observable<Void> buildUseCaseObservable(RequestValues requestValues) {
        return Observable.from(() -> {

            HabitRPGUser user = requestValues.user;

            if(requestValues.selectClassEvent == null) {
                if (user.getStats().getLvl() > 10 &&
                        !user.getPreferences().getDisableClasses() &&
                        !user.getFlags().getClassSelected()) {
                    SelectClassEvent event = new SelectClassEvent();
                    event.isInitialSelection = true;
                    event.currentClass = user.getStats().get_class().toString();
                    displayClassSelectionActivity(user, event);
                }
            } else {
                displayClassSelectionActivity(user, requestValues.selectClassEvent);
            }

            return null;
        });
    }

    private void displayClassSelectionActivity(HabitRPGUser user, SelectClassEvent event) {
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

        Intent intent = new Intent(HabiticaApplication.currentActivity, ClassSelectionActivity.class);
        intent.putExtras(bundle);
        HabiticaApplication.currentActivity.startActivityForResult(intent, SELECT_CLASS_RESULT);
    }

    public static final class RequestValues implements UseCase.RequestValues {


        private HabitRPGUser user;
        private SelectClassEvent selectClassEvent;

        public RequestValues(HabitRPGUser user, SelectClassEvent selectClassEvent) {

            this.user = user;
            this.selectClassEvent = selectClassEvent;
        }
    }
}
