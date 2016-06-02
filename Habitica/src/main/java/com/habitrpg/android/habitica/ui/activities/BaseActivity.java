package com.habitrpg.android.habitica.ui.activities;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.components.AppComponent;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayoutResId();

    private boolean destroyed;

    public boolean isDestroyed(){
        return destroyed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectActivity(getHabiticaApplication().getComponent());
        setContentView(getLayoutResId());
        ButterKnife.bind(this);
    }

    protected abstract void injectActivity(AppComponent component);

    protected void setupToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    public HabiticaApplication getHabiticaApplication() {
        return (HabiticaApplication)getApplication();
    }
}
