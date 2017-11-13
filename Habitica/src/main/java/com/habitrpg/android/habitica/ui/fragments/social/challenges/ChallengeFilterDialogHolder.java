package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengesFilterRecyclerViewAdapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

class ChallengeFilterDialogHolder {

    @BindView(R.id.challenge_filter_recycler_view)
    RecyclerView groupRecyclerView;

    @BindView(R.id.challenge_filter_button_all)
    Button allButton;

    @BindView(R.id.challenge_filter_button_none)
    Button noneButton;

    @BindView(R.id.challenge_filter_owned)
    CheckBox checkboxOwned;

    @BindView(R.id.challenge_filter_not_owned)
    CheckBox checkboxNotOwned;

    private AlertDialog dialog;
    private List<Challenge> challengesViewed;
    private ChallengeFilterOptions currentFilter;
    private Action1<ChallengeFilterOptions> selectedGroupsCallback;
    private Activity context;
    private ChallengesFilterRecyclerViewAdapter adapter;


    private ChallengeFilterDialogHolder(View view, Activity context) {
        this.context = context;
        ButterKnife.bind(this, view);
    }

    static void showDialog(Activity activity, List<Challenge> challengesViewed,
                           ChallengeFilterOptions currentFilter,
                           Action1<ChallengeFilterOptions> selectedGroupsCallback) {
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_challenge_filter, null);

        ChallengeFilterDialogHolder challengeFilterDialogHolder = new ChallengeFilterDialogHolder(dialogLayout, activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.filter)
                .setView(dialogLayout);

        challengeFilterDialogHolder.bind(builder, challengesViewed, currentFilter, selectedGroupsCallback);
    }

    public void bind(AlertDialog.Builder builder, List<Challenge> challengesViewed,
                     ChallengeFilterOptions currentFilter,
                     Action1<ChallengeFilterOptions> selectedGroupsCallback) {
        builder = builder
                .setPositiveButton(context.getString(R.string.done), (dialog1, which) -> doneClicked());
        this.dialog = builder.show();
        this.challengesViewed = challengesViewed;
        this.currentFilter = currentFilter;
        this.selectedGroupsCallback = selectedGroupsCallback;
        fillChallengeGroups();

        if(currentFilter != null ){
            checkboxOwned.setChecked(currentFilter.showOwned);
            checkboxNotOwned.setChecked(currentFilter.notOwned);
        }
    }

    private void fillChallengeGroups() {

       this.groupRecyclerView.setLayoutManager(new LinearLayoutManager(context));
       adapter = new ChallengesFilterRecyclerViewAdapter(getGroups(challengesViewed));
        if(currentFilter != null && currentFilter.showByGroups != null){
            adapter.selectAll(currentFilter.showByGroups);
        }

       this.groupRecyclerView.setAdapter(adapter);
    }

    private Collection<Group> getGroups(@Nullable List<Challenge> challenges){
        Map<String, Group> groupMap = new HashMap<>();

        if (challenges != null) {
            for (Challenge challenge : challenges) {
                if (groupMap.containsKey(challenge.groupName)) {
                    continue;
                }
                Group group = new Group();
                group.id = challenge.groupId;
                group.name = challenge.groupName;

                groupMap.put(challenge.groupName, group);
            }
        }

        return groupMap.values();
    }

    private void doneClicked() {
        ChallengeFilterOptions options = new ChallengeFilterOptions();
        options.showByGroups = this.adapter.getCheckedEntries();
        options.showOwned = checkboxOwned.isChecked();
        options.notOwned = checkboxNotOwned.isChecked();

        selectedGroupsCallback.call(options);
        this.dialog.hide();
    }


    @OnClick(R.id.challenge_filter_button_all)
    void allClicked() {
        this.adapter.selectAll();
    }

    @OnClick(R.id.challenge_filter_button_none)
    void noneClicked() {
        this.adapter.deSelectAll();
    }

}

