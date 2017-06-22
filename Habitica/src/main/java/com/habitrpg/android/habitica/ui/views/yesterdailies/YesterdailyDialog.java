package com.habitrpg.android.habitica.ui.views.yesterdailies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

public class YesterdailyDialog extends AlertDialog {

    private static boolean isDisplaying = false;

    private final List<Task> tasks;
    private UserRepository userRepository;

    @BindView(R.id.yesterdailies_list)
    LinearLayout yesterdailiesList;

    @BindColor(R.color.task_gray)
    int taskGray;

    private YesterdailyDialog(@NonNull Context context, UserRepository userRepository, List<Task> tasks) {
        super(context);
        this.userRepository = userRepository;
        this.tasks = tasks;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_yesterdaily, null);
        ButterKnife.bind(this, view);
        this.setView(view);
        this.setButton(AlertDialog.BUTTON_POSITIVE,
                context.getString(R.string.start_day),
                (dialog, which) -> this.runCron());

        this.setOnDismissListener(dialog -> runCron());

        createTaskViews(inflater);
    }

    private void runCron() {
        List<Task> completedTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.completed) {
                completedTasks.add(task);
            }
        }
        userRepository.runCron(completedTasks);
    }

    private void createTaskViews(LayoutInflater inflater) {
        for (Task task : tasks) {
            View taskView = createNewTaskView(inflater);
            taskView.setClipToOutline(true);
            configureTaskView(taskView, task);
            taskView.setOnClickListener(v -> {
                task.completed = !task.completed;
                configureTaskView(taskView, task);
            });
            yesterdailiesList.addView(taskView);
        }
    }

    private void configureTaskView(View taskView, Task task) {
        boolean completed = !task.isDisplayedActive(0);
        CheckBox checkbox = (CheckBox) taskView.findViewById(R.id.checkBox);
        View checkboxHolder = taskView.findViewById(R.id.checkBoxHolder);
        checkbox.setChecked(completed);
        if (completed) {
            checkboxHolder.setBackgroundColor(this.taskGray);
        } else {
            checkboxHolder.setBackgroundResource(task.getLightTaskColor());
        }
        TextView textView = (TextView) taskView.findViewById(R.id.text_view);
        textView.setText(task.getText());
    }


    private View createNewTaskView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.dialog_yesterdaily_task, yesterdailiesList, false);
    }

    public static void showDialogIfNeeded(Activity activity, String userId, UserRepository userRepository, TaskRepository taskRepository) {
        if (userRepository != null && userId != null) {
            userRepository.getUser(userId).first()
                    .filter(User::getNeedsCron)
                    .flatMap(user -> taskRepository.getTasks(Task.TYPE_DAILY, userId))
                    .first()
                    .map(tasks -> tasks.where().equalTo("isDue", true).equalTo("completed", false).equalTo("yesterDaily", true).findAll())
                    .flatMap(taskRepository::getTaskCopies)
                    .subscribe(tasks -> {
                        if (isDisplaying) {
                            return;
                        }
                        if (tasks.size() > 0) {
                            YesterdailyDialog dialog = new YesterdailyDialog(activity, userRepository, tasks);
                            dialog.show();
                            isDisplaying = true;
                        } else {
                            userRepository.runCron();
                        }
                    }, RxErrorHandler.handleEmptyError());
        }
    }
}
