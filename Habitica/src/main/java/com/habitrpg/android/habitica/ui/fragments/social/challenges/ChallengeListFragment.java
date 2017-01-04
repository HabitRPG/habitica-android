package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeListFragment extends BaseMainFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.challenges_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.challenges_list)
    RecyclerView recyclerView;

    private ChallengesListViewAdapter challengeAdapter;
    private boolean viewUserChallengesOnly;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        challengeAdapter = new ChallengesListViewAdapter(viewUserChallengesOnly);
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setAdapter(challengeAdapter);

        fetchLocalChallenges();
        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        fetchOnlineChallenges();
    }

    private void fetchLocalChallenges() {
        swipeRefreshLayout.setRefreshing(true);
        Where<Challenge> query = new Select().from(Challenge.class).where(Condition.column("name").isNotNull());

        if (viewUserChallengesOnly) {
            query = query.and(Condition.column("user_id").is(user.getId()));
        }

        List<Challenge> challenges = query.queryList();

        if (challenges.size() != 0) {
            setAdapterEntries(challenges);
        }

        // load online challenges & save to database
        onRefresh();
    }

    private void fetchOnlineChallenges() {
        if (this.apiHelper != null && this.apiHelper.apiService != null) {

            apiHelper.apiService.getUserChallenges().
                    compose(apiHelper.configureApiCallObserver())
                    .subscribe(challenges -> {

                        List<Challenge> userChallenges = this.user.getChallengeList();

                        HashSet<String> userChallengesHash = new HashSet<String>();

                        for (Challenge userChallenge : userChallenges) {
                            userChallengesHash.add(userChallenge.id);
                        }

                        userChallenges.clear();

                        for (Challenge challenge : challenges) {
                            if (userChallengesHash.contains(challenge.id) && challenge.name != null && !challenge.name.isEmpty()) {
                                challenge.user_id = this.user.getId();
                                userChallenges.add(challenge);
                            }

                            challenge.async().save();

                        }

                        if (viewUserChallengesOnly) {
                            setAdapterEntries(userChallenges);
                        } else {

                            setAdapterEntries(challenges);
                        }


                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, throwable -> {
                        Log.e("ChallengeListFragment", "", throwable);

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        }
    }


    private void setAdapterEntries(List<Challenge> challenges) {
        challengeAdapter.setChallenges(challenges);
    }

    public void addItem(Challenge challenge) {
        challengeAdapter.addChallenge(challenge);
    }

    public void updateItem(Challenge challenge) {
        challengeAdapter.replaceChallenge(challenge);
    }
}
