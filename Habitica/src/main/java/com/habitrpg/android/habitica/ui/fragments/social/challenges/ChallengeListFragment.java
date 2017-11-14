package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.activities.CreateChallengeActivity;
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;
import rx.Observable;

public class ChallengeListFragment extends BaseMainFragment implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    ChallengeRepository challengeRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerViewEmptySupport recyclerView;
    @BindView(R.id.emptyView)
    public View emptyView;

    private ChallengesListViewAdapter challengeAdapter;
    private boolean viewUserChallengesOnly;

    public void setViewUserChallengesOnly(boolean only) {
        this.viewUserChallengesOnly = only;
    }


    private RealmResults<Challenge> challenges;

    private ChallengeFilterOptions filterOptions;

    @Override
    public void onDestroy() {
        challengeRepository.close();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_challengeslist, container, false);
        unbinder = ButterKnife.bind(this, v);

        challengeAdapter = new ChallengesListViewAdapter(null, true, viewUserChallengesOnly, userId);

        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setAdapter(challengeAdapter);
        if (!viewUserChallengesOnly) {
            this.recyclerView.setBackgroundResource(R.color.white);
        }

        recyclerView.setEmptyView(emptyView);
        recyclerView.setItemAnimator(new SafeDefaultItemAnimator());

        loadLocalChallenges();
        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onRefresh() {
        fetchOnlineChallenges();
    }

    private void setRefreshing(boolean state) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.getVisibility() == View.VISIBLE) {
            swipeRefreshLayout.setRefreshing(state);
        }
    }

    private void loadLocalChallenges() {
        Observable<RealmResults<Challenge>> observable;

        if (viewUserChallengesOnly && user != null) {
            observable = challengeRepository.getUserChallenges(user.getId());
        } else {
            observable = challengeRepository.getChallenges();
        }

        observable.first().subscribe(challenges -> {
            if (challenges.size() == 0) {
                fetchOnlineChallenges();
            }
            this.challenges = challenges;
            challengeAdapter.updateUnfilteredData(challenges);
        }, RxErrorHandler.handleEmptyError());
    }

    private void fetchOnlineChallenges() {
        setRefreshing(true);
        challengeRepository.retrieveChallenges(user).subscribe(challenges -> {}, RxErrorHandler.handleEmptyError(), () -> setRefreshing(false));
    }

    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.sidebar_challenges);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list_challenges, menu);


        RelativeLayout badgeLayout = (RelativeLayout) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        TextView filterCountTextView = (TextView) badgeLayout.findViewById(R.id.badge_textview);
        filterCountTextView.setText(null);
        filterCountTextView.setVisibility(View.GONE);
        badgeLayout.setOnClickListener(view -> showFilterDialog());
    }

    private void showFilterDialog() {
        ChallengeFilterDialogHolder.showDialog(getActivity(),
                challenges,
                filterOptions, this::changeFilter);
    }

    private void changeFilter(ChallengeFilterOptions challengeFilterOptions) {
        filterOptions = challengeFilterOptions;
        if (challengeAdapter != null) {
            challengeAdapter.filter(filterOptions);
        }
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
            case R.id.action_reload:
                fetchOnlineChallenges();
                return true;
            case R.id.action_search:
                showFilterDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
