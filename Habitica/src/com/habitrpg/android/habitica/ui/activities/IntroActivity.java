package com.habitrpg.android.habitica.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.IntroFragment;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    @Bind(R.id.view_pager)
    ViewPager pager;

    @Bind(R.id.view_pager_indicator)
    CirclePageIndicator indicator;

    @Bind(R.id.skipButton)
    Button skipButton;

    @Bind(R.id.finishButton)
    Button finishButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        setupIntro();
        indicator.setViewPager(pager);

        this.skipButton.setOnClickListener(this);
        this.finishButton.setOnClickListener(this);
    }

    private void setupIntro() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        pager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                IntroFragment fragment = new IntroFragment();

                switch (position) {
                    case 0: {
                        fragment.setImage(getResources().getDrawable(R.drawable.intro_1));
                        fragment.setTitle(getString(R.string.intro_1_title));
                        fragment.setDescription(getString(R.string.intro_1_description));
                        break;
                    }
                    case 1: {
                        fragment.setImage(getResources().getDrawable(R.drawable.intro_2));
                        fragment.setTitle(getString(R.string.intro_2_title));
                        fragment.setDescription(getString(R.string.intro_2_description));
                        break;
                    }
                    case 2: {
                        fragment.setImage(getResources().getDrawable(R.drawable.intro_3));
                        fragment.setTitle(getString(R.string.intro_3_title));
                        fragment.setDescription(getString(R.string.intro_3_description));
                        break;
                    }
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        pager.addOnPageChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        finishIntro();
    }

    private void finishIntro() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        this.startActivity(intent);
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 2) {
            this.finishButton.setVisibility(View.VISIBLE);
        } else {
            this.finishButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
