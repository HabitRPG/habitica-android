package com.habitrpg.android.habitica.ui.activities;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.interactors.BuyRewardUseCase;
import com.habitrpg.android.habitica.interactors.ChecklistCheckUseCase;
import com.habitrpg.android.habitica.interactors.DailyCheckUseCase;
import com.habitrpg.android.habitica.interactors.DisplayItemDropUseCase;
import com.habitrpg.android.habitica.interactors.HabitScoreUseCase;
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase;
import com.habitrpg.android.habitica.interactors.TodoCheckUseCase;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeDetailDialogHolder;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeTasksRecyclerViewFragment;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.LeaveChallengeBody;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.AlertDialog;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

import static com.habitrpg.android.habitica.ui.helpers.UiUtils.showSnackbar;

public class ChallengeDetailActivity extends BaseActivity {

    public static String CHALLENGE_ID = "CHALLENGE_ID";

    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    public ApiClient apiClient;

    @BindView(R.id.floating_menu_wrapper)
    FrameLayout floatingMenuWrapper;

    // region UseCases

    @Inject
    HabitScoreUseCase habitScoreUseCase;

    @Inject
    DailyCheckUseCase dailyCheckUseCase;

    @Inject
    TodoCheckUseCase todoCheckUseCase;

    @Inject
    BuyRewardUseCase buyRewardUseCase;

    @Inject
    ChecklistCheckUseCase checklistCheckUseCase;

    @Inject
    DisplayItemDropUseCase displayItemDropUseCase;

    @Inject
    NotifyUserUseCase notifyUserUseCase;

    // endregion

    private Challenge challenge;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_challenge_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_challenge_details, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.challenge_details);
        }
        detail_tabs.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();

        String challengeId = extras.getString(CHALLENGE_ID);

        ObservableList<Task> fullList = new ObservableArrayList<>();

        if (challengeId != null) {

            apiClient.getChallengeTasks(challengeId)
                    .subscribe(taskList -> {
                        ArrayList<Task> resultList = new ArrayList<>();

                        ArrayList<Task> todos = new ArrayList<>();
                        ArrayList<Task> habits = new ArrayList<>();
                        ArrayList<Task> dailies = new ArrayList<>();
                        ArrayList<Task> rewards = new ArrayList<>();

                        for (Map.Entry<String, Task> entry : taskList.tasks.entrySet()) {
                            switch (entry.getValue().type) {
                                case Task.TYPE_TODO:
                                    todos.add(entry.getValue());
                                    break;
                                case Task.TYPE_HABIT:

                                    habits.add(entry.getValue());
                                    break;
                                case Task.TYPE_DAILY:

                                    dailies.add(entry.getValue());
                                    break;
                                case Task.TYPE_REWARD:

                                    rewards.add(entry.getValue());
                                    break;
                            }
                        }


                        if (!habits.isEmpty()) {
                            Task dividerTask = new Task();
                            dividerTask.setId("divhabits");
                            dividerTask.type = "divider";
                            dividerTask.text = "Challenge Habits";

                            resultList.add(dividerTask);
                            resultList.addAll(habits);
                        }


                        if (!dailies.isEmpty()) {
                            Task dividerTask = new Task();
                            dividerTask.setId("divdailies");
                            dividerTask.type = "divider";
                            dividerTask.text = "Challenge Dailies";

                            resultList.add(dividerTask);
                            resultList.addAll(dailies);
                        }


                        if (!todos.isEmpty()) {
                            Task dividerTask = new Task();
                            dividerTask.setId("divtodos");
                            dividerTask.type = "divider";
                            dividerTask.text = "Challenge To-Dos";

                            resultList.add(dividerTask);
                            resultList.addAll(todos);
                        }

                        if (!rewards.isEmpty()) {
                            Task dividerTask = new Task();
                            dividerTask.setId("divrewards");
                            dividerTask.type = "divider";
                            dividerTask.text = "Challenge Rewards";

                            resultList.add(dividerTask);
                            resultList.addAll(rewards);
                        }


                        fullList.addAll(resultList);
                    }, Throwable::printStackTrace);
        }

        ChallengeTasksRecyclerViewFragment fragment = ChallengeTasksRecyclerViewFragment.newInstance(HabiticaApplication.User, fullList);

        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        }

        challenge = new Select().from(Challenge.class).where(Condition.column("id").is(challengeId)).querySingle();

        ChallengeViewHolder challengeViewHolder = new ChallengeViewHolder(findViewById(R.id.challenge_header));
        challengeViewHolder.bind(challenge);
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_leave:
                showChallengeLeaveDialog();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showChallengeLeaveDialog(){
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.challenge_leave_title))
                .setMessage(this.getString(R.string.challenge_leave_text, challenge.name))
                .setPositiveButton(this.getString(R.string.yes), (dialog, which) -> {
                    dialog.dismiss();

                    showRemoveTasksDialog(keepTasks -> this.apiClient.leaveChallenge(challenge.id, new LeaveChallengeBody(keepTasks))
                            .subscribe(aVoid -> {
                                challenge.user_id = null;
                                challenge.async().save();

                                HabiticaApplication.User.resetChallengeList();
                                finish();

                            }, throwable -> {
                            }));
                })
                .setNegativeButton(this.getString(R.string.no), (dialog, which) -> dialog.dismiss()).show();
    }

    // refactor as an UseCase later - see ChallengeDetailDialogHolder
    private void showRemoveTasksDialog(Action1<String> callback){
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.challenge_remove_tasks_title))
                .setMessage(this.getString(R.string.challenge_remove_tasks_text))
                .setPositiveButton(this.getString(R.string.remove_tasks), (dialog, which) -> {
                    callback.call("remove-all");
                    dialog.dismiss();
                })
                .setNegativeButton(this.getString(R.string.keep_tasks), (dialog, which) -> {
                    callback.call("keep-all");
                    dialog.dismiss();
                }).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.challenge_name)
        EmojiTextView challengeName;

        @BindView(R.id.challenge_description)
        EmojiTextView challengeDescription;

        @BindView(R.id.challenge_member_count)
        TextView memberCountTextView;

        @BindView(R.id.gem_prize_layout)
        LinearLayout gem_prize_layout;

        @BindView(R.id.gem_amount)
        TextView gemPrizeTextView;

        private Challenge challenge;

        ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            if (challengeName != null) {
                challengeName.setText(EmojiParser.parseEmojis(challenge.name));
            }
            challengeDescription.setText(MarkdownParser.parseMarkdown(challenge.description));

            memberCountTextView.setText(String.valueOf(challenge.memberCount));

            if (challenge.prize == 0) {
                gem_prize_layout.setVisibility(View.GONE);
            } else {
                gem_prize_layout.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(String.valueOf(challenge.prize));
            }
        }

        @OnClick(R.id.btn_show_more)
        void onShowMore() {

            ChallengeDetailDialogHolder.showDialog(ChallengeDetailActivity.this, ChallengeDetailActivity.this.apiClient,
                    HabiticaApplication.User, challenge,
                    challenge1 -> {

                    },
                    challenge1 -> ChallengeDetailActivity.this.onBackPressed());
        }
    }

    @Subscribe
    public void onEvent(TaskCheckedCommand event) {
        switch (event.Task.type) {
            case Task.TYPE_DAILY: {
                dailyCheckUseCase.observable(new DailyCheckUseCase.RequestValues(event.Task, !event.Task.getCompleted()))
                        .subscribe(res -> EventBus.getDefault().post(new TaskUpdatedEvent(event.Task)), error -> {
                        });
            }
            break;
            case Task.TYPE_TODO: {
                todoCheckUseCase.observable(new TodoCheckUseCase.RequestValues(event.Task, !event.Task.getCompleted()))
                        .subscribe(res -> EventBus.getDefault().post(new TaskUpdatedEvent(event.Task)), error -> {
                        });
            }
            break;
        }
    }

    @Subscribe
    public void onEvent(ChecklistCheckedCommand event) {
        checklistCheckUseCase.observable(new ChecklistCheckUseCase.RequestValues(event.task.getId(), event.item.getId()))
                .subscribe(res -> EventBus.getDefault().post(new TaskUpdatedEvent(event.task)), error -> {
                });
    }

    @Subscribe
    public void onEvent(HabitScoreEvent event) {
        habitScoreUseCase.observable(new HabitScoreUseCase.RequestValues(event.habit, event.Up))
                .subscribe(res -> onTaskDataReceived(res, event.habit), error -> {
                });
    }

    @Subscribe
    public void onEvent(final BuyRewardCommand event) {
        if (HabiticaApplication.User.getStats().getGp() < event.Reward.getValue()) {
            showSnackbar(this, floatingMenuWrapper, getString(R.string.no_gold), UiUtils.SnackbarDisplayType.FAILURE);
            return;
        }


        if (event.Reward.specialTag == null || !event.Reward.specialTag.equals("item")) {

            buyRewardUseCase.observable(new BuyRewardUseCase.RequestValues(event.Reward))
                    .subscribe(res -> onTaskDataReceived(res, event.Reward), error -> {});
        }

    }

    public void onTaskDataReceived(TaskDirectionData data, Task task) {
        if (task.type.equals("reward")) {
            showSnackbar(this, floatingMenuWrapper, getString(R.string.notification_purchase, task.getText()), UiUtils.SnackbarDisplayType.NORMAL);
        } else {
            if (HabiticaApplication.User != null) {
                notifyUserUseCase.observable(new NotifyUserUseCase.RequestValues(this, floatingMenuWrapper, () -> {
                    // retrieveUser? forward message to MainActivity ? or mark it to refresh ?
                },
                        HabiticaApplication.User, data.getExp(), data.getHp(), data.getGp(), data.getMp(), data.getLvl()));
            }

            displayItemDropUseCase.observable(new DisplayItemDropUseCase.RequestValues(data, this, floatingMenuWrapper))
                    .subscribe(aVoid -> {}, throwable -> {});
        }
    }

}
