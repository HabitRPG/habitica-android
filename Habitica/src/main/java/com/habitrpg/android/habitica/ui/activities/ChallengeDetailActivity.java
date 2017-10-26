package com.habitrpg.android.habitica.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.interactors.BuyRewardUseCase;
import com.habitrpg.android.habitica.interactors.ChecklistCheckUseCase;
import com.habitrpg.android.habitica.interactors.DailyCheckUseCase;
import com.habitrpg.android.habitica.interactors.DisplayItemDropUseCase;
import com.habitrpg.android.habitica.interactors.HabitScoreUseCase;
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase;
import com.habitrpg.android.habitica.interactors.TodoCheckUseCase;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeDetailDialogHolder;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeTasksRecyclerViewFragment;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;


public class ChallengeDetailActivity extends BaseActivity {

    public static String CHALLENGE_ID = "CHALLENGE_ID";

    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    ChallengeRepository challengeRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;
    @Inject
    UserRepository userRepository;

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

    @Nullable
    private Challenge challenge;
    private User user;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_challenge_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_challenge_details, menu);

        if(challenge != null && challenge.leaderId != null && !challenge.leaderId.equals(userId)){
            menu.setGroupVisible(R.id.challenge_edit_action_group, false);
        }

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

        List<Task> fullList = new ArrayList<>();

        userRepository.getUser(userId).first().subscribe(user -> {
            ChallengeDetailActivity.this.user = user;
            createTaskRecyclerFragment(fullList);
        }, RxErrorHandler.handleEmptyError());

        if (challengeId != null) {
            challengeRepository.getChallengeTasks(challengeId)
                    .first()
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
                    }, RxErrorHandler.handleEmptyError());
        }

        if (challengeId != null) {
            challengeRepository.getChallenge(challengeId).subscribe(challenge -> {
                ChallengeDetailActivity.this.challenge = challenge;
                ChallengeViewHolder challengeViewHolder = new ChallengeViewHolder(findViewById(R.id.challenge_header));
                challengeViewHolder.bind(challenge);
            }, RxErrorHandler.handleEmptyError());
        }
    }

    private void createTaskRecyclerFragment(List<Task> fullList) {
        ChallengeTasksRecyclerViewFragment fragment = ChallengeTasksRecyclerViewFragment.newInstance(user, fullList);

        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onDestroy() {
        challengeRepository.close();
        userRepository.close();
        super.onDestroy();
    }

    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                openChallengeEditActivity();
                return true;
            case R.id.action_leave:
                showChallengeLeaveDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openChallengeEditActivity(){
        Intent intent = new Intent(this, CreateChallengeActivity.class);
        if (challenge != null) {
            intent.putExtra(CreateChallengeActivity.CHALLENGE_ID_KEY, challenge.id);
        }

        startActivity(intent);

    }

    private void showChallengeLeaveDialog(){
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.challenge_leave_title))
                .setMessage(this.getString(R.string.challenge_leave_text, challenge != null ? challenge.name : ""))
                .setPositiveButton(this.getString(R.string.yes), (dialog, which) -> {
                    dialog.dismiss();

                    showRemoveTasksDialog(keepTasks -> this.challengeRepository.leaveChallenge(challenge, new LeaveChallengeBody(keepTasks))
                            .subscribe(aVoid -> finish(), RxErrorHandler.handleEmptyError()));
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

        @BindView(R.id.gem_amount)
        TextView gemPrizeTextView;

        private Challenge challenge;

        ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            Drawable gemDrawable = new BitmapDrawable(itemView.getResources(), HabiticaIconsHelper.imageOfGem());
            gemPrizeTextView.setCompoundDrawablesWithIntrinsicBounds(gemDrawable, null, null, null);
        }

        public void bind(Challenge challenge) {
            this.challenge = challenge;

            if (challengeName != null) {
                challengeName.setText(EmojiParser.parseEmojis(challenge.name));
            }
            challengeDescription.setText(MarkdownParser.parseMarkdown(challenge.description));

            memberCountTextView.setText(String.valueOf(challenge.memberCount));

            if (challenge.prize == 0) {
                gemPrizeTextView.setVisibility(View.GONE);
            } else {
                gemPrizeTextView.setVisibility(View.VISIBLE);
                gemPrizeTextView.setText(String.valueOf(challenge.prize));
            }
        }

        @OnClick(R.id.btn_show_more)
        void onShowMore() {
            ChallengeDetailDialogHolder.showDialog(ChallengeDetailActivity.this,
                    ChallengeDetailActivity.this.challengeRepository,
                    challenge,
                    challenge1 -> ChallengeDetailActivity.this.onBackPressed());
        }
    }

    @Subscribe
    public void onEvent(TaskCheckedCommand event) {
        switch (event.Task.type) {
            case Task.TYPE_DAILY: {
                dailyCheckUseCase.observable(new DailyCheckUseCase.RequestValues(user, event.Task, !event.Task.getCompleted()))
                        .subscribe(this::onTaskDataReceived, RxErrorHandler.handleEmptyError());
            }
            break;
            case Task.TYPE_TODO: {
                todoCheckUseCase.observable(new TodoCheckUseCase.RequestValues(user, event.Task, !event.Task.getCompleted()))
                        .subscribe(this::onTaskDataReceived, RxErrorHandler.handleEmptyError());
            }
            break;
        }
    }

    @Subscribe
    public void onEvent(ChecklistCheckedCommand event) {
        checklistCheckUseCase.observable(new ChecklistCheckUseCase.RequestValues(event.task.getId(), event.item.getId()))
                .subscribe(res -> EventBus.getDefault().post(new TaskUpdatedEvent(event.task)), RxErrorHandler.handleEmptyError());
    }

    @Subscribe
    public void onEvent(HabitScoreEvent event) {
        habitScoreUseCase.observable(new HabitScoreUseCase.RequestValues(user, event.habit, event.Up))
                .subscribe(this::onTaskDataReceived, RxErrorHandler.handleEmptyError());
    }

    @Subscribe
    public void onEvent(final BuyRewardCommand event) {
        if (user.getStats().getGp() < event.Reward.getValue()) {
            HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.no_gold), HabiticaSnackbar.SnackbarDisplayType.FAILURE);
            return;
        }


        if (event.Reward.specialTag == null || !event.Reward.specialTag.equals("item")) {

            buyRewardUseCase.observable(new BuyRewardUseCase.RequestValues(user, event.Reward))
                    .subscribe(res -> HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.notification_purchase_reward), HabiticaSnackbar.SnackbarDisplayType.NORMAL), RxErrorHandler.handleEmptyError());
        }

    }

    public void onTaskDataReceived(TaskScoringResult data) {
        if (user != null) {
            notifyUserUseCase.observable(new NotifyUserUseCase.RequestValues(this, floatingMenuWrapper,
                    user, data.experienceDelta, data.healthDelta, data.goldDelta, data.manaDelta, data.hasLeveledUp));
        }

        displayItemDropUseCase.observable(new DisplayItemDropUseCase.RequestValues(data, this, floatingMenuWrapper))
                .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
    }
}
