package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

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

        query.async().queryList(new TransactionListener<List<Challenge>>() {
            @Override
            public void onResultReceived(List<Challenge> result) {
                if (result.size() != 0) {
                    setAdapterEntries(result);
                }

                // load online challenges & save to database
                onRefresh();
            }

            @Override
            public boolean onReady(BaseTransaction<List<Challenge>> transaction) {
                return false;
            }

            @Override
            public boolean hasResult(BaseTransaction<List<Challenge>> transaction, List<Challenge> result) {
                return result.size() != 0;
            }
        });
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
                    });
        }
    }


    private void setAdapterEntries(List<Challenge> challenges) {

        challengeAdapter.setChallenges(challenges);

    }

    @Override
    public void onClick(View v) {

    }
}
