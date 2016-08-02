package com.habitrpg.android.habitica.ui.activities;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;

public class IntroActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    @Inject
    public APIHelper apiHelper;
    @BindView(R.id.view_pager)
    ViewPager pager;
    @BindView(R.id.view_pager_indicator)
    CirclePageIndicator indicator;
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

        getHabiticaApplication().getComponent().inject(this);

        setupIntro();
        indicator.setViewPager(pager);

        this.skipButton.setOnClickListener(this);
        this.finishButton.setOnClickListener(this);

        apiHelper.apiService.getContent(apiHelper.languageCode)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(contentResult -> {
                }, throwable -> {
                });
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    private void setupIntro() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        pager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                IntroFragment fragment = new IntroFragment();

                switch (position) {
                    case 0: {
                        fragment.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_1, null));
                        fragment.setTitle(getString(R.string.intro_1_title));
                        fragment.setDescription(getString(R.string.intro_1_description));
                        break;
                    }
                    case 1: {
                        fragment.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_2, null));
                        fragment.setTitle(getString(R.string.intro_2_title));
                        fragment.setDescription(getString(R.string.intro_2_description));
                        break;
                    }
                    case 2: {
                        fragment.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.intro_3, null));
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
