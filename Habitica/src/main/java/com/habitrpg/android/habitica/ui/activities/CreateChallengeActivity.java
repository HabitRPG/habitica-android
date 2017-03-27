package com.habitrpg.android.habitica.ui.activities;

import android.os.Bundle;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;

public class CreateChallengeActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.id.activity_create_challenge;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }
}
