package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.mikepenz.materialdrawer.Drawer;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GemPurchaseActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gem_purchase);

        // Inject Controls
        ButterKnife.inject(this);

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

            toolbar.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tool_bar_top_padding), 0, 0);
            toolbar.setTitle("Purchase Gems");
            actionBar.setTitle("Purchase Gems 2");
        }


        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar)

                .build();
    }
}
