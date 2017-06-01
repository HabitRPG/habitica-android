package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.activities.CreateChallengeActivity;
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity;
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action0;

public class ChallengeListFragment extends BaseMainFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.challenge_filter_layout)
    LinearLayout challengeFilterLayout;

    @BindView(R.id.action_filter_icon)
    View actionFilterIcon;

    @BindView(R.id.challenges_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.challenges_refresh_empty)
    SwipeRefreshLayout swipeRefreshEmptyLayout;

    @BindView(R.id.challenges_list)
    RecyclerView recyclerView;

    private ChallengesListViewAdapter challengeAdapter;
    private boolean viewUserChallengesOnly;
    private Action0 refreshCallback;
    private boolean withFilter;

    public void setWithFilter(boolean withFilter) {
        this.withFilter = withFilter;
    }

    public void setViewUserChallengesOnly(boolean only) {
        this.viewUserChallengesOnly = only;
    }

    public void setRefreshingCallback(Action0 refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    private List<Challenge> currentChallengesInView;

    private ChallengeFilterOptions lastFilterOptions;

    public void setObservable(Observable<ArrayList<Challenge>> listObservable) {
        listObservable
                .subscribe(challenges -> {

                    List<Challenge> userChallenges = this.user != null ? this.user.getChallengeList() : new ArrayList<>();

                    HashSet<String> userChallengesHash = new HashSet<>();

                    for (Challenge userChallenge : userChallenges) {
                        userChallengesHash.add(userChallenge.id);
                    }

                    userChallenges.clear();

                    for (Challenge challenge : challenges) {
                        if (userChallengesHash.contains(challenge.id) && challenge.name != null && !challenge.name.isEmpty()) {
                            challenge.user_id = this.user.getId();
                            userChallenges.add(challenge);
                        } else {
                            challenge.user_id = null;
                        }

                        challenge.async().save();
                    }

                    setRefreshingIfVisible(swipeRefreshLayout, false);
                    setRefreshingIfVisible(swipeRefreshEmptyLayout, false);

                    if (viewUserChallengesOnly) {
                        setChallengeEntries(userChallenges);
                    } else {
                        setChallengeEntries(challenges);
                    }


                }, throwable -> {
                    Log.e("ChallengeListFragment", "", throwable);

                    setRefreshingIfVisible(swipeRefreshLayout, false);
                    setRefreshingIfVisible(swipeRefreshEmptyLayout, false);
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_challengeslist, container, false);
        unbinder = ButterKnife.bind(this, v);

        challengeAdapter = new ChallengesListViewAdapter(viewUserChallengesOnly, user);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshEmptyLayout.setOnRefreshListener(this);

        challengeFilterLayout.setVisibility(withFilter ? View.VISIBLE : View.GONE);
        challengeFilterLayout.setClickable(true);
        challengeFilterLayout.setOnClickListener(view -> ChallengeFilterDialogHolder.showDialog(getActivity(), currentChallengesInView, lastFilterOptions, filterOptions -> {
            challengeAdapter.setFilterByGroups(filterOptions);
            this.lastFilterOptions = filterOptions;
        }));

        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setAdapter(challengeAdapter);
        if (!viewUserChallengesOnly) {
            this.recyclerView.setBackgroundResource(R.color.white);
        }

        fetchLocalChallenges();
        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onRefresh() {
        setRefreshingIfVisible(swipeRefreshEmptyLayout, true);
        setRefreshingIfVisible(swipeRefreshLayout, true);

        fetchOnlineChallenges();
    }

    private void setRefreshingIfVisible(SwipeRefreshLayout refreshLayout, boolean state) {
        if (refreshLayout != null && refreshLayout.getVisibility() == View.VISIBLE) {
            refreshLayout.setRefreshing(state);
        }
    }

    private void fetchLocalChallenges() {
        setRefreshingIfVisible(swipeRefreshLayout, true);

        Where<Challenge> query = new Select().from(Challenge.class).where(Condition.column("name").isNotNull());

        if (viewUserChallengesOnly && user != null) {
            query = query.and(Condition.column("user_id").is(user.getId()));
        }

        List<Challenge> challenges = query.queryList();

        if (challenges.size() != 0) {
            setChallengeEntries(challenges);
        }

        setRefreshingIfVisible(swipeRefreshLayout, false);

        // load online challenges & save to database
        onRefresh();
    }

    private void setChallengeEntries(List<Challenge> challenges) {
        if (swipeRefreshEmptyLayout == null || swipeRefreshLayout == null) {
            return;
        }
        currentChallengesInView = challenges;

        if (viewUserChallengesOnly && challenges.size() == 0) {
            swipeRefreshEmptyLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            swipeRefreshEmptyLayout.setRefreshing(false);
            swipeRefreshEmptyLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }

        challengeAdapter.setChallenges(challenges);
    }

    private void fetchOnlineChallenges() {
        if (refreshCallback != null) {
            refreshCallback.call();
        }
    }

    public void addItem(Challenge challenge) {
        challengeAdapter.addChallenge(challenge);
    }

    public void updateItem(Challenge challenge) {
        challengeAdapter.replaceChallenge(challenge);
    }

    @Override
    public String customTitle() {
        return getString(R.string.sidebar_challenges);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list_challenges, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_challenge:
                Intent intent = new Intent(getActivity(), CreateChallengeActivity.class);
                startActivity(intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
