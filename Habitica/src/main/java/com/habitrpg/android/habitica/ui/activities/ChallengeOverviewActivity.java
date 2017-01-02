package com.habitrpg.android.habitica.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragment;

import javax.inject.Inject;

import butterknife.BindView;

public class ChallengeOverviewActivity extends BaseActivity {

    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.avatar_with_bars)
    View avatar_with_bars;

    @Inject
    public APIHelper apiHelper;

    private ChallengesOverviewFragment overviewFragment;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_challenge_overview;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar(toolbar);

        getSupportActionBar().setTitle(R.string.challenges);

        overviewFragment = new ChallengesOverviewFragment();
        overviewFragment.setTabLayout(detail_tabs);
        overviewFragment.setUser(HabiticaApplication.User);

        AvatarWithBarsViewModel avatarInHeader = new AvatarWithBarsViewModel(this, avatar_with_bars);
        avatarInHeader.updateData(HabiticaApplication.User);

        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, overviewFragment).commitAllowingStateLoss();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, overviewFragment).addToBackStack(null).commitAllowingStateLoss();
        }
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(!overviewFragment.onHandleBackPressed()){
            finish();
        }
    }
}
