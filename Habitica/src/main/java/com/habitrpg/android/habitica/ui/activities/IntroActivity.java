package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;

import javax.inject.Inject;

import butterknife.BindView;

public class IntroActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    @Inject
    public ApiClient apiClient;
    @BindView(R.id.view_pager)
    ViewPager pager;
    @BindView(R.id.view_pager_indicator)
    IconPageIndicator indicator;
    @BindView(R.id.skipButton)
    Button skipButton;
    @BindView(R.id.finishButton)
    Button finishButton;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_intro;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HabiticaBaseApplication.getComponent().inject(this);

        setupIntro();
        indicator.setViewPager(pager);

        this.skipButton.setOnClickListener(this);
        this.finishButton.setOnClickListener(this);

        apiClient.getContent()
                .subscribe(contentResult -> {}, RxErrorHandler.handleEmptyError());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_20_alpha));
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    private void setupIntro() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        pager.setAdapter(new PagerAdapter(fragmentManager));

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
        overridePendingTransition(0, R.anim.activity_fade_out);
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

    private class PagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            IntroFragment fragment = new IntroFragment();

            switch (position) {
                case 0: {
                    fragment.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_1, null));
                    fragment.setSubtitle(getString(R.string.intro_1_subtitle));
                    fragment.setTitleImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_1_title, null));
                    fragment.setDescription(getString(R.string.intro_1_description, getString(R.string.habitica_user_count)));
                    fragment.setBackgroundColor(ContextCompat.getColor(IntroActivity.this, R.color.brand_300));
                    break;
                }
                case 1: {
                    fragment.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_2, null));
                    fragment.setSubtitle(getString(R.string.intro_2_subtitle));
                    fragment.setTitle(getString(R.string.intro_2_title));
                    fragment.setDescription(getString(R.string.intro_2_description));
                    fragment.setBackgroundColor(ContextCompat.getColor(IntroActivity.this, R.color.blue_10));
                    break;
                }
                case 2: {
                    fragment.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_3, null));
                    fragment.setSubtitle(getString(R.string.intro_3_subtitle));
                    fragment.setTitle(getString(R.string.intro_3_title));
                    fragment.setDescription(getString(R.string.intro_3_description));
                    fragment.setBackgroundColor(ContextCompat.getColor(IntroActivity.this, R.color.red_100));
                    break;
                }
            }

            return fragment;
        }

        @Override
        public int getIconResId(int index) {
            return R.drawable.indicator_diamond;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
