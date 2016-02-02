package com.habitrpg.android.habitica.ui.fragments;

import android.support.v4.app.Fragment;

import com.amplitude.api.Amplitude;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import de.greenrobot.event.EventBus;

public class BaseFragment extends Fragment {

    public String tutorialStepIdentifier;
    public String tutorialText;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (this.tutorialStepIdentifier != null) {
                new Select().from(TutorialStep.class).where(Condition.column("identifier").eq(tutorialStepIdentifier)).async().querySingle(tutorialStepTransactionListener);
            }

            String displayedClassName = this.getDisplayedClassName();

            if (displayedClassName != null) {
                JSONObject eventProperties = new JSONObject();
                try {
                    eventProperties.put("eventAction", "navigate");
                    eventProperties.put("eventCategory", "navigation");
                    eventProperties.put("hitType", "pageview");
                    eventProperties.put("page", displayedClassName);
                } catch (JSONException exception) {
                }
                Amplitude.getInstance().logEvent("navigate", eventProperties);
            }
        }
    }

    public String getDisplayedClassName() {
        return this.getClass().getSimpleName();
    }

    private TransactionListener<TutorialStep> tutorialStepTransactionListener = new TransactionListener<TutorialStep>() {
        @Override
        public void onResultReceived(TutorialStep step) {
            if (step != null && !step.getWasCompleted() && (step.getDisplayedOn() == null || (new Date().getTime() - step.getDisplayedOn().getTime()) > 86400000)) {
                DisplayTutorialEvent event = new DisplayTutorialEvent();
                event.step = step;
                event.tutorialText = tutorialText;
                EventBus.getDefault().post(event);
            }
        }

        @Override
        public boolean onReady(BaseTransaction<TutorialStep> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<TutorialStep> baseTransaction, TutorialStep step) {
            return true;
        }
    };



}
