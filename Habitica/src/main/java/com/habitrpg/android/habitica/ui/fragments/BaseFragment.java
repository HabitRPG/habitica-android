package com.habitrpg.android.habitica.ui.fragments;

import com.amplitude.api.Amplitude;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.ui.activities.BaseActivity;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends DialogFragment {

    public String tutorialStepIdentifier;
    public String tutorialText;
    public Unbinder unbinder;
    private boolean registerEventBus = false;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectFragment(((BaseActivity) getActivity()).getHabiticaApplication().getComponent());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Receive Events
        try {
            EventBus.getDefault().register(this);
            registerEventBus = true;
        } catch (EventBusException ignored) {

        }

        return null;
    }

    public abstract void injectFragment(AppComponent component);

    @CallSuper
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (unbinder == null) {
            unbinder = ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onDestroyView() {
        if (registerEventBus) {
            EventBus.getDefault().unregister(this);
        }
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }

        super.onDestroyView();
    }

    public String getDisplayedClassName() {
        return this.getClass().getSimpleName();
    }


}
