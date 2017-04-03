package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengesFilterRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

class ChallegeFilterDialogHolder {

    @BindView(R.id.challenge_filter_recycler_view)
    RecyclerView groupRecyclerView;

    @BindView(R.id.challenge_filter_button_done)
    Button doneButton;

    @BindView(R.id.challenge_filter_button_all)
    Button allButton;

    @BindView(R.id.challenge_filter_button_none)
    Button noneButton;

    @BindView(R.id.challenge_filter_owned)
    CheckBox checkboxOwned;

    @BindView(R.id.challenge_filter_not_owned)
    CheckBox checkboxNotOwned;

    private AlertDialog dialog;
    private ApiClient apiClient;
    private HabitRPGUser user;
    private List<Challenge> challengesViewed;
    private ChallengeFilterOptions currentFilter;
    private Action1<ChallengeFilterOptions> selectedGroupsCallback;
    private Activity context;
    private ChallengesFilterRecyclerViewAdapter adapter;


    protected ChallegeFilterDialogHolder(View view, Activity context) {
        this.context = context;
        ButterKnife.bind(this, view);
    }

    public static void showDialog(Activity activity, ApiClient apiClient, HabitRPGUser user, List<Challenge> challengesViewed,
                                  ChallengeFilterOptions currentFilter,
                                  Action1<ChallengeFilterOptions> selectedGroupsCallback) {
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_challenge_filter, null);

        ChallegeFilterDialogHolder challegeFilterDialogHolder = new ChallegeFilterDialogHolder(dialogLayout, activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(dialogLayout);

        challegeFilterDialogHolder.bind(builder.show(), apiClient, user, challengesViewed, currentFilter, selectedGroupsCallback);
    }

    public void bind(AlertDialog dialog, ApiClient apiClient, HabitRPGUser user, List<Challenge> challengesViewed,
                     ChallengeFilterOptions currentFilter,
                     Action1<ChallengeFilterOptions> selectedGroupsCallback) {
        this.dialog = dialog;
        this.apiClient = apiClient;
        this.user = user;
        this.challengesViewed = challengesViewed;
        this.currentFilter = currentFilter;
        this.selectedGroupsCallback = selectedGroupsCallback;

        fillChallengeGroups();

        if(currentFilter != null ){
            checkboxOwned.setChecked(currentFilter.ShowOwned);
            checkboxNotOwned.setChecked(currentFilter.NotOwned);
        }
    }

    private void fillChallengeGroups() {

       this.groupRecyclerView.setLayoutManager(new LinearLayoutManager(context));
       adapter = new ChallengesFilterRecyclerViewAdapter(getGroups(challengesViewed));
        if(currentFilter != null && currentFilter.ShowByGroups != null){
            adapter.selectAll(currentFilter.ShowByGroups);
        }

       this.groupRecyclerView.setAdapter(adapter);
    }

    private Collection<Group> getGroups(List<Challenge> challenges){
        HashMap<String, Group> groupMap = new HashMap<>();

        for (Challenge c : challenges) {
            if(!groupMap.containsKey(c.groupName)){
                Group g = new Group();
                g.id = c.groupId;
                g.name = c.groupName;

                groupMap.put(c.groupName, g);
            }
        }

        return groupMap.values();
    }

    @OnClick(R.id.challenge_filter_button_done)
    public void doneClicked() {
        ChallengeFilterOptions options = new ChallengeFilterOptions();
        options.ShowByGroups = this.adapter.getCheckedEntries();
        options.ShowOwned = checkboxOwned.isChecked();
        options.NotOwned = checkboxNotOwned.isChecked();

        selectedGroupsCallback.call(options);
        this.dialog.hide();
    }


    @OnClick(R.id.challenge_filter_button_all)
    public void allClicked() {
        this.adapter.selectAll();
    }

    @OnClick(R.id.challenge_filter_button_none)
    public void noneClicked() {
        this.adapter.deSelectAll();
    }

}

