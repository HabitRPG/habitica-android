//package com.habitrpg.android.habitica.api;
//
//
//import com.habitrpg.android.habitica.BuildConfig;
//import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
//import com.habitrpg.android.habitica.models.tasks.Task;
//import com.habitrpg.android.habitica.models.tasks.TaskList;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.annotation.Config;
//
//import android.os.Build;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import io.realm.RealmList;
//import rx.observers.TestSubscriber;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
//@RunWith(RobolectricTestRunner.class)
//public class TaskAPITests extends BaseAPITests {
//
//    private Task habit1;
//    private Task daily1;
//    private Task todo1;
//    private Task reward1;
//
//    @Override
//    public void setUp() {
//        super.setUp();
//        /*TestSubscriber<TaskList> oldTaskSubscriber = new TestSubscriber<>();
//        apiClient.getTasks()
//                .subscribe(oldTaskSubscriber);
//        oldTaskSubscriber.awaitTerminalEvent();
//        TaskList tasks = oldTaskSubscriber.getOnNextEvents().get(0);
//        for (Task task : tasks.tasks.values()) {
//            apiClient.deleteTask(task.getId()).subscribe(new TestSubscriber<>());
//        }
//
//        List<Task> randomTasks = new ArrayList<>();
//        randomTasks.add(createRandomTask("1", "habit"));
//        habit1 = randomTasks.get(0);
//        randomTasks.add(createRandomTask("2", "habit"));
//        randomTasks.add(createRandomTask("3", "daily"));
//        daily1 = randomTasks.get(2);
//        randomTasks.add(createRandomTask("4", "daily"));
//        randomTasks.add(createRandomTask("5", "todo"));
//        todo1 = randomTasks.get(4);
//        randomTasks.add(createRandomTask("6", "todo"));
//        randomTasks.add(createRandomTask("7", "reward"));
//        reward1 = randomTasks.get(6);
//        randomTasks.add(createRandomTask("8", "reward"));
//        TestSubscriber<List<Task>> testSubscriber = new TestSubscriber<>();
//        apiClient.createTasks(randomTasks)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();*/
//    }
//
//    private Task createRandomTask(String number, String type) {
//        Task task = new Task();
//        task.setId(String.valueOf(UUID.randomUUID()));
//        task.setText("task-"+number);
//        task.setType(type);
//        task.setTags(new RealmList<>());
//        task.setChecklist(new RealmList<>());
//        task.setReminders(new RealmList<>());
//        return task;
//    }
//
//    /*@Test
//    public void shouldLoadAllTasksFromServer() {
//        TestSubscriber<TaskList> testSubscriber = new TestSubscriber<>();
//        apiClient.getTasks()
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        TaskList taskList = testSubscriber.getOnNextEvents().get(0);
//        assertEquals(8, taskList.tasks.size());
//    }
//
//    @Test
//    public void shouldBeAbleToScoreTaskUp() {
//        TestSubscriber<TaskDirectionData> testSubscriber = new TestSubscriber<>();
//        apiClient.postTaskDirection(habit1.getId(), "UP")
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        TaskDirectionData data = testSubscriber.getOnNextEvents().get(0);
//        assertTrue(data.getDelta() > 0);
//    }
//
//    @Test
//    public void shouldBeAbleToScoreTaskDown() {
//        TestSubscriber<TaskDirectionData> testSubscriber = new TestSubscriber<>();
//        apiClient.postTaskDirection(habit1.getId(), "DOWN")
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        TaskDirectionData data = testSubscriber.getOnNextEvents().get(0);
//        assertTrue(data.getDelta() < 0);
//    }
//
//    @Test
//    public void shouldBeAbleToDeleteATask() {
//        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
//        apiClient.deleteTask(habit1.getId())
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertCompleted();
//        testSubscriber.assertNoErrors();
//        TestSubscriber<TaskList> newTaskListSubscriber = new TestSubscriber<>();
//        apiClient.getTasks().subscribe(newTaskListSubscriber);
//        TaskList taskList = newTaskListSubscriber.getOnNextEvents().get(0);
//        assertEquals(7, taskList.tasks.size());
//    }*/
//
//}
