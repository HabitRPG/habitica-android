package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeListFragment extends BaseMainFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.challenges_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.challenges_list)
    RecyclerView recyclerView;

    private ChallengesListViewAdapter challengeAdapter;
    private boolean viewUserChallengesOnly;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        challengeAdapter = new ChallengesListViewAdapter();

    }

    public void setViewUserChallengesOnly(boolean only) {
        this.viewUserChallengesOnly = only;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_challengeslist, container, false);
        unbinder = ButterKnife.bind(this, v);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));

        this.onRefresh();
        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        fetchChallenges();
    }

    private void fetchChallenges() {
        if (this.apiHelper != null && this.apiHelper.apiService != null) {

            apiHelper.apiService.getUserChallenges().
                    compose(apiHelper.configureApiCallObserver())
                    .subscribe(s -> {

                        if (viewUserChallengesOnly) {
                            List<Challenge> userChallenges = this.user.getChallengeList();

                            HashSet<String> userChallengesHash = new HashSet<String>();

                            for (Challenge userChallenge : userChallenges) {
                                userChallengesHash.add(userChallenge.id);
                            }

                            userChallenges.clear();

                            for (Challenge challenge : s) {
                                if (userChallengesHash.contains(challenge.id)) {
                                    challenge.user_id = this.user.getId();
                                    userChallenges.add(challenge);
                                }

                                challenge.async().save();
                            }

                            challengeAdapter.setChallenges(userChallenges);
                        } else {
                            challengeAdapter.setChallenges(s);

                            for (Challenge challenge : s) {
                                challenge.async().save();
                            }
                        }
                        recyclerView.setAdapter(challengeAdapter);

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, throwable -> {
                    });
        }
    }

    @Override
    public void onClick(View v) {

    }
}
