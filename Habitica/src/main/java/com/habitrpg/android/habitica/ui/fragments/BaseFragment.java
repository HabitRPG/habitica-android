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
import com.habitrpg.android.habitica.data.TutorialRepository;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.helpers.AmplitudeManager;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseFragment extends DialogFragment {

    @Inject
    protected
    TutorialRepository tutorialRepository;

    public String tutorialStepIdentifier;
    public String tutorialText;
    public Unbinder unbinder;
    protected boolean tutorialCanBeDeferred = true;
    public List<String> tutorialTexts;

    CompositeSubscription compositeSubscription;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (this.tutorialStepIdentifier != null && tutorialRepository != null) {
                tutorialRepository.getTutorialStep(this.tutorialStepIdentifier).first().subscribe(step -> {
                    if (step != null && step.isValid() && step.isManaged() && step.shouldDisplay()) {
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
                }, ReactiveErrorHandler.handleEmptyError());
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
        injectFragment(HabiticaBaseApplication.getComponent());
        compositeSubscription = new CompositeSubscription();
        super.onCreate(savedInstanceState);
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

    @Override
    public void onDestroy() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    public String getDisplayedClassName() {
        return this.getClass().getSimpleName();
    }
}
