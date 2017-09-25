package com.habitrpg.android.habitica.ui.views.yesterdailies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class YesterdailyDialog extends AlertDialog {

    static boolean isDisplaying = false;

    private final List<Task> tasks;
    private final TaskRepository taskRepository;
    private UserRepository userRepository;

    @BindView(R.id.yesterdailies_list)
    LinearLayout yesterdailiesList;

    @BindColor(R.color.task_gray)
    int taskGray;

    private YesterdailyDialog(@NonNull Context context, UserRepository userRepository, TaskRepository taskRepository, List<Task> tasks) {
        super(context);
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.tasks = tasks;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_yesterdaily, null);
        ButterKnife.bind(this, view);
        this.setView(view);
        this.setButton(AlertDialog.BUTTON_POSITIVE,
                context.getString(R.string.start_day),
                (dialog, which) -> {});

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
        isDisplaying = false;
    }

    private void createTaskViews(LayoutInflater inflater) {
        for (Task task : tasks) {
            View taskView = createNewTaskView(inflater);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                taskView.setClipToOutline(true);
            }
            configureTaskView(taskView, task);
            View taskContainer = taskView.findViewById(R.id.taskHolder);
            taskContainer.setOnClickListener(v -> {
                task.completed = !task.completed;
                configureTaskView(taskView, task);
            });

            if (task.checklist.size() > 0) {
                View checklistDivider = taskView.findViewById(R.id.checklistDivider);
                ViewGroup checklistContainer = taskView.findViewById(R.id.checklistView);
                for (ChecklistItem item : task.checklist) {
                    View checklistView = inflater.inflate(R.layout.checklist_item_row, yesterdailiesList, false);
                    configureChecklistView(checklistView, item);
                    checklistView.setOnClickListener(v -> {
                        item.setCompleted(!item.getCompleted());
                        taskRepository.scoreChecklistItem(task.getId(), item.getId()).subscribe(task1 -> {}, RxErrorHandler.handleEmptyError());
                        configureChecklistView(checklistView, item);
                    });
                    checklistContainer.addView(checklistView);
                }
            }
            CheckBox checkBox = (CheckBox) taskView.findViewById(R.id.checkBox);
            checkBox.setEnabled(false);
            checkBox.setClickable(false);
            yesterdailiesList.addView(taskView);
        }
    }

    private void configureChecklistView(View checklistView, ChecklistItem item) {
        CheckBox checkbox = (CheckBox) checklistView.findViewById(R.id.checkBox);
        checkbox.setChecked(item.getCompleted());
        View checkboxHolder = checklistView.findViewById(R.id.checkBoxHolder);
        checkboxHolder.setBackgroundResource(R.color.gray_700);
        TextView textView = (TextView) checklistView.findViewById(R.id.checkedTextView);
        textView.setText(item.getText());
    }

    private void configureTaskView(View taskView, Task task) {
        boolean completed = !task.isDisplayedActive();
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
            Observable.just(null)
                    .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .filter(aVoid -> !userRepository.isClosed())
                    .flatMap(aVoid -> userRepository.getUser(userId))
                    .first()
                    .filter(user -> user != null && user.getNeedsCron() != null && user.getNeedsCron())
                    .flatMap(user -> {
                        final Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, -1);
                        return taskRepository.updateDailiesIsDue(cal.getTime());
                    })
                    .flatMap(user -> taskRepository.getTasks(Task.TYPE_DAILY, userId).first())
                    .map(tasks -> tasks.where().equalTo("isDue", true).equalTo("completed", false).equalTo("yesterDaily", true).findAll())
                    .flatMap(taskRepository::getTaskCopies)
                    .retry(1)
                    .subscribe(tasks -> {
                        if (isDisplaying) {
                            return;
                        }
                        if (tasks.size() > 0) {
                            showDialog(activity, userRepository, taskRepository, tasks);
                        } else {
                            userRepository.runCron();
                        }
                    }, RxErrorHandler.handleEmptyError());
        }
    }

    private static void showDialog(Activity activity, UserRepository userRepository, TaskRepository taskRepository, List<Task> tasks) {
        if (activity.isFinishing()) {
            return;
        }
        YesterdailyDialog dialog = new YesterdailyDialog(activity, userRepository, taskRepository, tasks);
        dialog.show();
        isDisplaying = true;
    }
}
