package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity;
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import net.pherth.android.emoji_library.EmojiParser;
import net.pherth.android.emoji_library.EmojiTextView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class ChallengeDetailDialogHolder {


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
    private ChallengeRepository challengeRepository;
    @Nullable
    private Challenge challenge;
    private Action1<Challenge> challengeLeftAction;
    private Activity context;


    private ChallengeDetailDialogHolder(View view, Activity context) {
        this.context = context;
        ButterKnife.bind(this, view);
    }

    public static void showDialog(Activity activity, ChallengeRepository challengeRepository, Challenge challenge, Action1<Challenge> challengeLeftAction) {
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_challenge_detail, null);

        ChallengeDetailDialogHolder challengeDetailDialogHolder = new ChallengeDetailDialogHolder(dialogLayout, activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(dialogLayout);

        challengeDetailDialogHolder.bind(builder.show(), challengeRepository, challenge, challengeLeftAction);
    }

    public void bind(AlertDialog dialog, ChallengeRepository challengeRepository, Challenge challenge,
                     Action1<Challenge> challengeLeftAction) {
        this.dialog = dialog;
        this.challengeRepository = challengeRepository;
        this.challenge = challenge;
        this.challengeLeftAction = challengeLeftAction;

        changeViewsByChallenge(challenge);
    }

    private void changeViewsByChallenge(Challenge challenge) {
        setJoined(challenge.isParticipating);

        challengeName.setText(EmojiParser.parseEmojis(challenge.name));
        challengeDescription.setText(MarkdownParser.parseMarkdown(challenge.description));
        challengeLeader.setText(challenge.leaderName);

        gem_amount.setText(String.valueOf(challenge.prize));
        member_count.setText(String.valueOf(challenge.memberCount));

        challengeRepository.getChallengeTasks(challenge.id)
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
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, task_group_layout, false);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        groupName.setText(habits.size() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_HABITS, habits.size()));

        int size = habits.size();
        for (int i = 0; i < size; i++) {
            Task task = habits.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_habit, tasks_layout, false);
            TextView habitTitle = (TextView) entry.findViewById(R.id.habit_title);
            ImageView plusImg = (ImageView) entry.findViewById(task.up ? R.id.plus_img_tinted : R.id.plus_img);
            ImageView minusImg = (ImageView) entry.findViewById(task.down ? R.id.minus_img_tinted : R.id.minus_img);

            plusImg.setVisibility(View.VISIBLE);
            minusImg.setVisibility(View.VISIBLE);

            habitTitle.setText(EmojiParser.parseEmojis(task.text));
            tasks_layout.addView(entry);
        }
        task_group_layout.addView(taskGroup);
    }

    private void addDailys(ArrayList<Task> dailies) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, task_group_layout, false);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        int size = dailies.size();
        groupName.setText(dailies.size() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_DAILYS, size));

        for (int i = 0; i < size; i++) {
            Task task = dailies.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_daily, tasks_layout, false);
            TextView title = (TextView) entry.findViewById(R.id.daily_title);
            title.setText(EmojiParser.parseEmojis(task.text));

            if (task.checklist != null && !task.checklist.isEmpty()) {
                View checklistIndicatorWrapper = entry.findViewById(R.id.checklistIndicatorWrapper);

                checklistIndicatorWrapper.setVisibility(View.VISIBLE);

                TextView checkListAllTextView = (TextView) entry.findViewById(R.id.checkListAllTextView);
                checkListAllTextView.setText(String.valueOf(task.checklist.size()));
            }
            tasks_layout.addView(entry);
        }
        task_group_layout.addView(taskGroup);
    }

    private void addTodos(ArrayList<Task> todos) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, task_group_layout, false);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        int size = todos.size();
        groupName.setText(todos.size() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_TODOS, size));

        for (int i = 0; i < size; i++) {
            Task task = todos.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_todo, tasks_layout, false);
            TextView title = (TextView) entry.findViewById(R.id.todo_title);
            title.setText(EmojiParser.parseEmojis(task.text));

            if (task.checklist != null && !task.checklist.isEmpty()) {
                View checklistIndicatorWrapper = entry.findViewById(R.id.checklistIndicatorWrapper);

                checklistIndicatorWrapper.setVisibility(View.VISIBLE);

                TextView checkListAllTextView = (TextView) entry.findViewById(R.id.checkListAllTextView);
                checkListAllTextView.setText(String.valueOf(task.checklist.size()));
            }
            tasks_layout.addView(entry);
        }
        task_group_layout.addView(taskGroup);
    }

    private void addRewards(ArrayList<Task> rewards) {
        LinearLayout taskGroup = (LinearLayout) context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_task_group, task_group_layout, false);
        TextView groupName = (TextView) taskGroup.findViewById(R.id.task_group_name);

        LinearLayout tasks_layout = (LinearLayout) taskGroup.findViewById(R.id.tasks_layout);

        int size = rewards.size();
        groupName.setText(rewards.size() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_REWARDS, size));

        for (int i = 0; i < size; i++) {
            Task task = rewards.get(i);

            View entry = context.getLayoutInflater().inflate(R.layout.dialog_challenge_detail_reward, tasks_layout, false);
            TextView title = (TextView) entry.findViewById(R.id.reward_title);
            title.setText(EmojiParser.parseEmojis(task.text));
            tasks_layout.addView(entry);
        }
        task_group_layout.addView(taskGroup);
    }

    private String getLabelByTypeAndCount(String type, int count) {
        if (Challenge.TASK_ORDER_DAILYS.equals(type)) {
            return context.getString(count == 1 ? R.string.daily : R.string.dailies);
        } else if (Challenge.TASK_ORDER_HABITS.equals(type)) {
            return context.getString(count == 1 ? R.string.habit : R.string.habits);
        } else if (Challenge.TASK_ORDER_REWARDS.equals(type)) {
            return context.getString(count == 1 ? R.string.reward : R.string.rewards);
        } else {
            return context.getString(count == 1 ? R.string.todo : R.string.todos);
        }
    }

    private void setJoined(boolean joined) {
        joinedHeader.setVisibility(joined ? View.VISIBLE : View.GONE);
        leaveButton.setVisibility(joined ? View.VISIBLE : View.GONE);

        notJoinedHeader.setVisibility(joined ? View.GONE : View.VISIBLE);
        joinButton.setVisibility(joined ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.challenge_leader)
    void openLeaderProfile() {
        FullProfileActivity.open(context, challenge.leaderId);
    }

    @OnClick(R.id.challenge_go_to_btn)
    void openChallengeActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, challenge.id);

        Intent intent = new Intent(context, ChallengeDetailActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
        this.dialog.dismiss();
    }

    @OnClick(R.id.challenge_join_btn)
    void joinChallenge() {
        this.challengeRepository.joinChallenge(challenge).subscribe(this::changeViewsByChallenge, RxErrorHandler.handleEmptyError());
    }

    @OnClick(R.id.challenge_leave_btn)
    void leaveChallenge() {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.challenge_leave_title))
                .setMessage(context.getString(R.string.challenge_leave_text, challenge.name))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) ->
                        showRemoveTasksDialog(keepTasks -> this.challengeRepository.leaveChallenge(challenge, new LeaveChallengeBody(keepTasks))
                                .subscribe(aVoid -> {
                                    if (challengeLeftAction != null) {
                                        challengeLeftAction.call(challenge);
                                    }
                                    this.dialog.dismiss();
                                }, RxErrorHandler.handleEmptyError())))
                .setNegativeButton(context.getString(R.string.no), (dialog, which) -> dialog.dismiss()).show();
    }

    // refactor as an UseCase later - see ChallengeDetailActivity
    private void showRemoveTasksDialog(Action1<String> callback){
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.challenge_remove_tasks_title))
                .setMessage(context.getString(R.string.challenge_remove_tasks_text))
                .setPositiveButton(context.getString(R.string.remove_tasks), (dialog, which) -> {
                    callback.call("remove-all");
                    dialog.dismiss();
                })
                .setNegativeButton(context.getString(R.string.keep_tasks), (dialog, which) -> {
                    callback.call("keep-all");
                    dialog.dismiss();
                }).show();
    }
}
