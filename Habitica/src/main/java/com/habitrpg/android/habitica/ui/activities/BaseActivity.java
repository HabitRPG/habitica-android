package com.habitrpg.android.habitica.ui.activities;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    private boolean destroyed;

    protected abstract int getLayoutResId();

    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHabiticaApplication();
        injectActivity(HabiticaBaseApplication.getComponent());
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
        return (HabiticaApplication) getApplication();
    }

    //Check for "Don't keep Activities" Developer setting
    //TODO: Make this check obsolete.
    boolean isAlwaysFinishActivitiesOptionEnabled() {
        int alwaysFinishActivitiesInt = 0;
        if (Build.VERSION.SDK_INT >= 17) {
            alwaysFinishActivitiesInt = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
        } else {
            alwaysFinishActivitiesInt = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
        }

        if (alwaysFinishActivitiesInt == 1) {
            return true;
        } else {
            return false;
        }
    }

    void showDeveloperOptionsScreen(){
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


}
