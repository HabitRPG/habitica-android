package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.mikepenz.materialdrawer.Drawer;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TavernActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tavern);

        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        // Enable Backbutton
        if (actionBar != null) {
            actionBar.setTitle(R.string.about_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setElevation(0);
        }

        Drawer drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar)
                .withTranslucentNavigationBar(false)
                .withTranslucentStatusBar(false)
                .withDisplayBelowStatusBar(false)
                .withDisplayBelowToolbar(false)
                .withActionBarDrawerToggle(false)
                .withSelectedItem(2)
                .build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
