package com.habitrpg.android.habitica.ui;


import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionPlan;

import org.w3c.dom.Text;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
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

    public SubscriptionDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.subscription_details, this);

        ButterKnife.bind(this);

    }

    public void setPlan(SubscriptionPlan plan) {

        if (plan.isActive()) {
            subscriptionStatusActive.setVisibility(View.VISIBLE);
            getSubscriptionStatusInactive.setVisibility(View.GONE);
        } else {
            subscriptionStatusActive.setVisibility(View.GONE);
            getSubscriptionStatusInactive.setVisibility(View.VISIBLE);
        }

        String duration = null;

        if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC) || plan.planId.equals(SubscriptionPlan.PLANID_BASICEARNED)) {
            duration = getResources().getString(R.string.month);
        } else if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC3MONTH)) {
            duration = getResources().getString(R.string.three_months);
        } else if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC6MONTH) || plan.planId.equals(SubscriptionPlan.PLANID_GOOGLE6MONTH)) {
            duration = getResources().getString(R.string.six_months);
        } else if (plan.planId.equals(SubscriptionPlan.PLANID_BASIC12MONTH)) {
            duration = getResources().getString(R.string.twelve_months);
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
        gemCapTextView.setText(String.valueOf(plan.consecutive.getGemCapExtra()+25));
        currentHourglassesTextView.setText(String.valueOf(plan.consecutive.getTrinkets()));
    }

    @OnClick(R.id.visitWebsiteButton)
    public void openSubscriptionWebsite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.BASE_URL+"/"));
        getContext().startActivity(browserIntent);
    }
}
