package com.habitrpg.android.habitica.ui;


import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionPlan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubscriptionDetailsView extends LinearLayout {

    @BindView(R.id.subscriptionDurationTextView)
    TextView subscriptionDurationTextView;

    @BindView(R.id.subscriptionStatusActive)
    TextView subscriptionStatusActive;
    @BindView(R.id.subscriptionStatusInactive)
    TextView getSubscriptionStatusInactive;

    @BindView(R.id.paymentProcessorTextView)
    TextView paymentProcessorTextView;

    @BindView(R.id.monthsSubscribedTextView)
    TextView monthsSubscribedTextView;

    @BindView(R.id.gemCapTextView)
    TextView gemCapTextView;

    @BindView(R.id.currentHourglassesTextView)
    TextView currentHourglassesTextView;

    @BindView(R.id.cancelSubscriptionDescription)
    TextView cancelSubscripnDescription;

    @BindView(R.id.visitWebsiteButton)
    Button visitWebsiteButton;

    private SubscriptionPlan plan;

    public SubscriptionDetailsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupView();
    }

    public SubscriptionDetailsView(Context context) {
        super(context);
        setupView();
    }

    private void setupView() {
        inflate(getContext(), R.layout.subscription_details, this);

        ButterKnife.bind(this);
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;

        if (plan.isActive()) {
            subscriptionStatusActive.setVisibility(View.VISIBLE);
            getSubscriptionStatusInactive.setVisibility(View.GONE);
        } else {
            subscriptionStatusActive.setVisibility(View.GONE);
            getSubscriptionStatusInactive.setVisibility(View.VISIBLE);
        }

        String duration = null;

        if (plan.planId != null) {
            if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC) || plan.planId.equals(SubscriptionPlan.PLANID_BASICEARNED)) {
                duration = getResources().getString(R.string.month);
            } else if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC3MONTH)) {
                duration = getResources().getString(R.string.three_months);
            } else if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC6MONTH) || plan.planId.equals(SubscriptionPlan.PLANID_GOOGLE6MONTH)) {
                duration = getResources().getString(R.string.six_months);
            } else if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC12MONTH)) {
                duration = getResources().getString(R.string.twelve_months);
            }
        }

        if (duration != null) {
            subscriptionDurationTextView.setText(getResources().getString(R.string.subscription_duration, duration));
        }

        paymentProcessorTextView.setText(plan.paymentMethod);

        if (plan.consecutive.getCount() == 1) {
            monthsSubscribedTextView.setText(getResources().getString(R.string.one_month));
        } else {
            monthsSubscribedTextView.setText(getResources().getString(R.string.months, plan.consecutive.getCount()));
        }
        gemCapTextView.setText(String.valueOf(plan.consecutive.getGemCapExtra() + 25));
        currentHourglassesTextView.setText(String.valueOf(plan.consecutive.getTrinkets()));

        if (plan.paymentMethod != null) {
            if (plan.paymentMethod.equals("Google")) {
                cancelSubscripnDescription.setText(R.string.cancel_subscription_google_description);
                visitWebsiteButton.setText(R.string.open_in_store);
            } else {
                cancelSubscripnDescription.setText(R.string.cancel_subscription_notgoogle_description);
                visitWebsiteButton.setText(R.string.visit_habitica_website);
            }
        }
    }

    @OnClick(R.id.visitWebsiteButton)
    public void openSubscriptionWebsite() {
        if (plan.paymentMethod != null) {
            Intent intent;
            if (plan.paymentMethod.equals("Google")) {
                intent = new Intent("android.intent.action.VIEW");
                intent.setComponent(new ComponentName("com.android.vending", "com.android.vending.MyDownloadsActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.BASE_URL + "/"));
            }
            getContext().startActivity(intent);
        }
    }
}
