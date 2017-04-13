package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.helpers.AmplitudeManager;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends DialogFragment {

    public String tutorialStepIdentifier;
    public String tutorialText;
    public Unbinder unbinder;
    protected boolean tutorialCanBeDeferred = true;
    private TransactionListener<TutorialStep> tutorialStepTransactionListener = new TransactionListener<TutorialStep>() {
        @Override
        public void onResultReceived(TutorialStep step) {
            if (step != null && step.shouldDisplay()) {
                DisplayTutorialEvent event = new DisplayTutorialEvent();
                event.step = step;
                if (tutorialText != null) {
                    event.tutorialText = tutorialText;
                } else {
                    event.tutorialTexts = tutorialTexts;
                }
                event.canBeDeferred = tutorialCanBeDeferred;
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
    public List<String> tutorialTexts;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (this.tutorialStepIdentifier != null) {
                new Select().from(TutorialStep.class).where(Condition.column("identifier").eq(tutorialStepIdentifier)).async().querySingle(tutorialStepTransactionListener);
            }

            String displayedClassName = this.getDisplayedClassName();

            if (displayedClassName != null) {
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("page", displayedClassName);
                AmplitudeManager.sendEvent("navigate", AmplitudeManager.EVENT_CATEGORY_NAVIGATION, AmplitudeManager.EVENT_HITTYPE_PAGEVIEW, additionalData);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectFragment(HabiticaBaseApplication.getComponent());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Receive Events
        try {
            EventBus.getDefault().register(this);
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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }

        super.onDestroyView();
        RefWatcher refWatcher = HabiticaApplication.getInstance(getContext()).refWatcher;
        refWatcher.watch(this);
    }

    public String getDisplayedClassName() {
        return this.getClass().getSimpleName();
    }


}
