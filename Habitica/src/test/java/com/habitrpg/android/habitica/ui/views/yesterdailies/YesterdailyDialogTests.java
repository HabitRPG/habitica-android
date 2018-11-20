//package com.habitrpg.android.habitica.ui.views.yesterdailies;
//
//import android.app.Activity;
//import android.content.Context;
//
//import com.habitrpg.android.habitica.BaseTestCase;
//import com.habitrpg.android.habitica.data.TaskRepository;
//import com.habitrpg.android.habitica.data.UserRepository;
//import com.habitrpg.android.habitica.models.tasks.Task;
//import com.habitrpg.android.habitica.models.user.User;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Answers;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import java.util.List;
//
//import io.realm.RealmResults;
//import rx.Observable;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.verifyNew;
//import static org.powermock.api.mockito.PowerMockito.whenNew;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(YesterdailyDialog.class)
//public class YesterdailyDialogTests extends BaseTestCase {
//
//    @Mock
//    Activity mockContext;
//    @Mock
//    UserRepository mockUserRepository;
//    @Mock
//    TaskRepository mockTaskRepository;
//    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
//    RealmResults<Task> mockResultsTasks;
//    @Mock
//    List<Task> mockTasks;
//
//    @Mock
//    YesterdailyDialog mockDialog;
//
//    User testUser;
//
//    @Before
//    public void setUp() throws Exception {
//        testUser = new User();
//        testUser.setId("123");
//        testUser.setNeedsCron(true);
//        whenNew(YesterdailyDialog.class).withAnyArguments().thenReturn(mockDialog);
//        when(mockUserRepository.getUser(anyString())).thenReturn(Observable.just(testUser));
//        when(mockTaskRepository.getTasks(anyString(), anyString())).thenReturn(Observable.just(mockResultsTasks));
//        when(mockTaskRepository.getTaskCopies(any(RealmResults.class))).thenReturn(Observable.just(mockTasks));
//        when(mockResultsTasks.where().equalTo("isDue", true).equalTo("completed", false).equalTo("yesterDaily", true).findAll()).thenReturn(mockResultsTasks);
//    }
//
//
//
//    @Test
//    public void showsDialogIfNeededAndTasks() throws Exception {
//        when(mockTasks.size()).thenReturn(1);
//        YesterdailyDialog.Companion.showDialogIfNeeded(mockContext, "", mockUserRepository, mockTaskRepository);
//        verifyNew(YesterdailyDialog.class).withArguments(any(Context.class), any(UserRepository.class), any(List.class));
//    }
//
//    @Test
//    public void doesntShowDialogIfAlreadyShown() throws Exception {
//        when(mockTasks.size()).thenReturn(1);
//        YesterdailyDialog.Companion.setIsDisplaying(true);
//        YesterdailyDialog.Companion.showDialogIfNeeded(mockContext, "", mockUserRepository, mockTaskRepository);
//        verifyNew(YesterdailyDialog.class, times(0)).withArguments(any(Context.class), any(UserRepository.class), any(List.class));
//    }
//
//    @Test
//    public void doesntShowDialogIfNoTasks() throws Exception {
//        when(mockTasks.size()).thenReturn(0);
//        YesterdailyDialog.Companion.setIsDisplaying(true);
//        YesterdailyDialog.Companion.showDialogIfNeeded(mockContext, "", mockUserRepository, mockTaskRepository);
//        verifyNew(YesterdailyDialog.class, times(0)).withArguments(any(Context.class), any(UserRepository.class), any(List.class));
//    }
//
//    @Test
//    public void doesntShowDialogIfNotNeeded() throws Exception {
//        testUser.setNeedsCron(false);
//        when(mockTasks.size()).thenReturn(1);
//        YesterdailyDialog.Companion.setIsDisplaying(true);
//        YesterdailyDialog.Companion.showDialogIfNeeded(mockContext, "", mockUserRepository, mockTaskRepository);
//        verifyNew(YesterdailyDialog.class, times(0)).withArguments(any(Context.class), any(UserRepository.class), any(List.class));
//    }
//
//}