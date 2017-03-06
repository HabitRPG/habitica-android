package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class ChallegeDetailDialogHolder {

    @BindView(R.id.challenge_not_joined_header)
    LinearLayout notJoinedHeader;

    @BindView(R.id.challenge_joined_header)
    LinearLayout joinedHeader;

    @BindView(R.id.challenge_join_btn)
    Button joinButton;

    @BindView(R.id.challenge_leave_btn)
    Button leaveButton;

    @BindView(R.id.challenge_name)
    EmojiTextView challengeName;

    @BindView(R.id.challenge_description)
    EmojiTextView challengeDescription;

    @BindView(R.id.challenge_leader)
    TextView challengeLeader;

    @BindView(R.id.gem_amount)
    TextView gem_amount;

    @BindView(R.id.challenge_member_count)
    TextView member_count;

    @BindView(R.id.task_group_layout)
    LinearLayout task_group_layout;

    private AlertDialog dialog;
    private ApiClient apiClient;
    private HabitRPGUser user;
    private Challenge challenge;
    private Action1<Challenge> challengeJoinedAction;
    private Action1<Challenge> challengeLeftAction;
    private Activity context;


    protected ChallegeDetailDialogHolder(View view, Activity context) {
        this.context = context;
        ButterKnife.bind(this, view);
    }

    public void bind(AlertDialog dialog, ApiClient apiClient, HabitRPGUser user, Challenge challenge,
                     Action1<Challenge> challengeJoinedAction, Action1<Challenge> challengeLeftAction) {
        this.dialog = dialog;
        this.apiClient = apiClient;
        this.user = user;
        this.challenge = challenge;
        this.challengeJoinedAction = challengeJoinedAction;
        this.challengeLeftAction = challengeLeftAction;

        changeViewsByChallenge(challenge);
    }

    public void changeViewsByChallenge(Challenge challenge) {
        setJoined(challenge.user_id != null && !challenge.user_id.isEmpty());

        challengeName.setText(EmojiParser.parseEmojis(challenge.name));
        challengeDescription.setText(MarkdownParser.parseMarkdown(challenge.description));
        challengeLeader.setText(challenge.leaderName);

        gem_amount.setText(challenge.prize + "");
        member_count.setText(challenge.memberCount + "");

        apiClient.getChallengeTasks(challenge.id)
                .subscribe(taskList -> {
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

                            if (habits.size() > 0) {
                                addHabits(habits);
                            }

                            if (dailies.size() > 0) {
                                addDailys(dailies);
                            }

                            if (todos.size() > 0) {
                                addTodos(todos);
                            }

                            if (rewards.size() > 0) {
                                addRewards(rewards);
                            }
                        }
                        , Throwable::printStackTrace);
    }

    private void addHabits(ArrayList<Task> habits) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, null);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        groupName.setText(habits.size() + " " + ChallengesListViewAdapter.ChallengeViewHolder.getLabelByTypeAndCount(context, Challenge.TASK_ORDER_HABITS, habits.size()));

        int size = habits.size();
        for (int i = 0; i < size; i++) {
            Task task = habits.get(i);

            View habitEntry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_habit, null);
            TextView habitTitle = (TextView) habitEntry.findViewById(R.id.habit_title);
            ImageView plusImg = (ImageView) habitEntry.findViewById(task.up ? R.id.plus_img_tinted : R.id.plus_img);
            ImageView minusImg = (ImageView) habitEntry.findViewById(task.down ? R.id.minus_img_tinted : R.id.minus_img);

            plusImg.setVisibility(View.VISIBLE);
            minusImg.setVisibility(View.VISIBLE);

            habitTitle.setText(EmojiParser.parseEmojis(task.text));

            tasks_layout.addView(habitEntry);
        }

        task_group_layout.addView(taskGroup);
    }

    private void addDailys(ArrayList<Task> dailys) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, null);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        int size = dailys.size();
        groupName.setText(dailys.size() + " " + ChallengesListViewAdapter.ChallengeViewHolder.getLabelByTypeAndCount(context, Challenge.TASK_ORDER_DAILYS, size));

        for (int i = 0; i < size; i++) {
            Task task = dailys.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_daily, null);
            TextView title = (TextView) entry.findViewById(R.id.daily_title);
            title.setText(EmojiParser.parseEmojis(task.text));

            if (task.checklist != null && !task.checklist.isEmpty()) {
                View checklistIndicatorWrapper = entry.findViewById(R.id.checklistIndicatorWrapper);

                checklistIndicatorWrapper.setVisibility(View.VISIBLE);

                TextView checkListAllTextView = (TextView) entry.findViewById(R.id.checkListAllTextView);
                checkListAllTextView.setText(task.checklist.size() + "");
            }

            tasks_layout.addView(entry);
        }

        task_group_layout.addView(taskGroup);
    }

    private void addTodos(ArrayList<Task> todos) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, null);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        int size = todos.size();
        groupName.setText(todos.size() + " " + ChallengesListViewAdapter.ChallengeViewHolder.getLabelByTypeAndCount(context, Challenge.TASK_ORDER_TODOS, size));

        for (int i = 0; i < size; i++) {
            Task task = todos.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_todo, null);
            TextView title = (TextView) entry.findViewById(R.id.todo_title);
            title.setText(EmojiParser.parseEmojis(task.text));

            tasks_layout.addView(entry);

            if (task.checklist != null && !task.checklist.isEmpty()) {
                View checklistIndicatorWrapper = entry.findViewById(R.id.checklistIndicatorWrapper);

                checklistIndicatorWrapper.setVisibility(View.VISIBLE);

                TextView checkListAllTextView = (TextView) entry.findViewById(R.id.checkListAllTextView);
                checkListAllTextView.setText(task.checklist.size() + "");
            }
        }

        task_group_layout.addView(taskGroup);
    }

    private void addRewards(ArrayList<Task> rewards) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, null);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        int size = rewards.size();
        groupName.setText(rewards.size() + " " + ChallengesListViewAdapter.ChallengeViewHolder.getLabelByTypeAndCount(context, Challenge.TASK_ORDER_REWARDS, size));

        for (int i = 0; i < size; i++) {
            Task task = rewards.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_reward, null);
            TextView title = (TextView) entry.findViewById(R.id.reward_title);
            title.setText(EmojiParser.parseEmojis(task.text));

            tasks_layout.addView(entry);
        }

        task_group_layout.addView(taskGroup);
    }

    public void setJoined(boolean joined) {
        joinedHeader.setVisibility(joined ? View.VISIBLE : View.GONE);
        leaveButton.setVisibility(joined ? View.VISIBLE : View.GONE);

        notJoinedHeader.setVisibility(joined ? View.GONE : View.VISIBLE);
        joinButton.setVisibility(joined ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.challenge_leader)
    public void openLeaderProfile() {
        EventBus.getDefault().post(new OpenFullProfileCommand(challenge.leaderId));
    }

    @OnClick(R.id.challenge_go_to_btn)
    public void openChallengeActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, challenge.id);

        Intent intent = new Intent(HabiticaApplication.currentActivity, ChallengeDetailActivity.class);
        intent.putExtras(bundle);
        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
        this.dialog.dismiss();
    }

    @OnClick(R.id.challenge_join_btn)
    public void joinChallenge() {
        this.apiClient.joinChallenge(challenge.id)
                .subscribe(challenge -> {
                    challenge.user_id = this.user.getId();
                    challenge.async().save();

                    if (challengeJoinedAction != null) {
                        challengeJoinedAction.call(challenge);
                    }

                    changeViewsByChallenge(challenge);
                }, throwable -> {
                });
    }

    @OnClick(R.id.challenge_leave_btn)
    public void leaveChallenge() {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.challenge_leave_title))
                .setMessage(String.format(context.getString(R.string.challenge_leave_text), challenge.name))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> this.apiClient.leaveChallenge(challenge.id)
                        .subscribe(aVoid -> {
                            challenge.user_id = null;
                            challenge.async().save();

                            this.user.resetChallengeList();

                            if (challengeLeftAction != null) {
                                challengeLeftAction.call(challenge);
                            }

                            this.dialog.dismiss();
                        }, throwable -> {
                        })).setNegativeButton(context.getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        }).show();
    }


    public static void showDialog(Activity activity, ApiClient apiClient, HabitRPGUser user, Challenge challenge,
                                  Action1<Challenge> challengeJoinedAction, Action1<Challenge> challengeLeftAction) {
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_challenge_detail, null);

        ChallegeDetailDialogHolder challegeDetailDialogHolder = new ChallegeDetailDialogHolder(dialogLayout, activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(dialogLayout);

        challegeDetailDialogHolder.bind(builder.show(), apiClient, user, challenge, challengeJoinedAction, challengeLeftAction);
    }
}