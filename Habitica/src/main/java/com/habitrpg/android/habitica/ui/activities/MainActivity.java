package com.habitrpg.android.habitica.ui.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.content.res.Configuration;
import android.database.sqlite.SQLiteDoneException;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amplitude.api.Amplitude;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.NotificationPublisher;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.ItemsCallback;
import com.habitrpg.android.habitica.callbacks.MergeUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskCreationCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.TaskUpdateCallback;
import com.habitrpg.android.habitica.callbacks.UnlockCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.events.ContentReloadedEvent;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.ReloadContentEvent;
import com.habitrpg.android.habitica.events.SelectClassEvent;
import com.habitrpg.android.habitica.events.ShareEvent;
import com.habitrpg.android.habitica.events.TaskRemovedEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.ToggledEditTagsEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand;
import com.habitrpg.android.habitica.events.commands.DeleteTaskCommand;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.events.commands.HatchingCommand;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.events.commands.SellItemCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.events.commands.UnlockPathCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.helpers.AmplitudeManager;
import com.habitrpg.android.habitica.helpers.LanguageHelper;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.TutorialView;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.menu.MainDrawerBuilder;
import com.habitrpg.android.habitica.userpicture.BitmapUtils;
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider;
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider;
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider;
import com.magicmicky.habitrpgwrapper.lib.api.MaintenanceApiService;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;
import com.magicmicky.habitrpgwrapper.lib.models.SuppressedModals;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.responses.MaintenanceResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Days;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Action1;

import static com.habitrpg.android.habitica.ui.helpers.UiUtils.SnackbarDisplayType;
import static com.habitrpg.android.habitica.ui.helpers.UiUtils.showSnackbar;

public class MainActivity extends BaseActivity implements Action1<Throwable>, HabitRPGUserCallback.OnUserReceived,
        TaskScoringCallback.OnTaskScored, TutorialView.OnTutorialReaction {

    public static final int SELECT_CLASS_RESULT = 11;
    public static final int GEM_PURCHASE_REQUEST = 111;
    public static final int MIN_LEVEL_FOR_SKILLS = 11;
    @Inject
    public APIHelper apiHelper;
    @Inject
    public MaintenanceApiService maintenanceService;
    public HabitRPGUser user;
    @Inject
    protected HostConfig hostConfig;
    @BindView(R.id.floating_menu_wrapper)
    FrameLayout floatingMenuWrapper;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;
    @BindView(R.id.avatar_with_bars)
    View avatar_with_bars;
    @BindView(R.id.overlayFrameLayout)
    FrameLayout overlayFrameLayout;
    private Drawer drawer;
    private Drawer filterDrawer;
    private AccountHeader accountHeader;
    private BaseMainFragment activeFragment;
    private AvatarWithBarsViewModel avatarInHeader;
    private AlertDialog faintDialog;

    private AvatarView sideAvatarView;
    private AvatarView dialogAvatarView;

    private Date lastSync;

    private TutorialView activeTutorialView;
    private boolean isloadingContent;
    private TransactionListener<HabitRPGUser> userTransactionListener = new TransactionListener<HabitRPGUser>() {
        @Override
        public void onResultReceived(HabitRPGUser habitRPGUser) {
            MainActivity.this.user = habitRPGUser;
            MainActivity.this.setUserData(true);
        }

        @Override
        public boolean onReady(BaseTransaction<HabitRPGUser> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<HabitRPGUser> baseTransaction, HabitRPGUser habitRPGUser) {
            return true;
        }
    };

    static public Double round(Double value, int n) {
        return (Math.round(value * Math.pow(10, n))) / (Math.pow(10, n));
    }

    PushNotificationManager pushNotificationManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        LanguageHelper languageHelper = new LanguageHelper(sharedPreferences.getString("language","en"));
        Locale.setDefault(languageHelper.getLocale());
        Configuration configuration = new Configuration();
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN){
            configuration.locale = languageHelper.getLocale();
        } else {
            configuration.setLocale(languageHelper.getLocale());
        }
        getResources().updateConfiguration(configuration,
                getResources().getDisplayMetrics());

        if (!HabiticaApplication.checkUserAuthentication(this, hostConfig)) {
            return;
        }

        //Check if reminder alarm is set
        scheduleReminder(this);

        pushNotificationManager = PushNotificationManager.getInstance(this);

        new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);

        setupToolbar(toolbar);

        avatarInHeader = new AvatarWithBarsViewModel(this, avatar_with_bars);
        accountHeader = MainDrawerBuilder.CreateDefaultAccountHeader(this).build();
        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar, accountHeader)
                .build();
        drawer.setSelectionAtPosition(1, false);
        sideAvatarView = new AvatarView(this, true, false, false);

        if (this.filterDrawer == null) {
            filterDrawer = new DrawerBuilder()
                    .withActivity(this)
                    .withDrawerGravity(Gravity.END)
                    .withCloseOnClick(false)
                    .append(this.drawer);
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //resync, if last sync was more than 10 minutes ago
        if (this.lastSync == null || (new Date().getTime() - this.lastSync.getTime()) > 180000) {
            if (this.apiHelper != null && this.apiHelper.hasAuthenticationKeys()) {
                this.apiHelper.retrieveUser(true)
                        .compose(apiHelper.configureApiCallObserver())
                        .subscribe(new HabitRPGUserCallback(this), throwable -> {
                        });
                this.checkMaintenance();
            }
        }

        //after the activity has been stopped and is thereafter resumed,
        //a state can arise in which the active fragment no longer has a
        //reference to the tabLayout (and all its adapters are null).
        //Recreate the fragment as a result.
        if (activeFragment != null && activeFragment.tabLayout == null) {
            activeFragment = null;
            drawer.setSelectionAtPosition(1);
        }
    }

    @Override
    protected void onPause() {
        updateWidget(AvatarStatsWidgetProvider.class);
        updateWidget(DailiesWidgetProvider.class);
        updateWidget(HabitButtonWidgetProvider.class);
        super.onPause();
    }

    private void updateWidget(Class widgetClass) {
        Intent intent = new Intent(this,widgetClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), widgetClass));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
    }


    private void saveLoginInformation() {
        HabiticaApplication.User = user;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        boolean ans = editor.putString(getString(R.string.SP_username), user.getAuthentication().getLocalAuthentication().getUsername())
                .putString(getString(R.string.SP_email), user.getAuthentication().getLocalAuthentication().getEmail())
                .commit();

        if (!ans) {
            Log.e("SHARED PREFERENCES", "Shared Preferences Username and Email error");
        }
    }

    public void displayFragment(BaseMainFragment fragment) {
        if (this.activeFragment != null && fragment.getClass() == this.activeFragment.getClass()) {
            return;
        }
        if (this.isDestroyed()) {
            return;
        }
        this.activeFragment = fragment;
        fragment.setArguments(getIntent().getExtras());
        fragment.setUser(user);
        fragment.setActivity(this);
        fragment.setTabLayout(detail_tabs);
        fragment.setFloatingMenuWrapper(floatingMenuWrapper);


        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        }
    }

    private void setUserData(boolean fromLocalDb) {
        if (user != null) {

            Preferences preferences = user.getPreferences();

            if(preferences!= null) {
                apiHelper.languageCode = preferences.getLanguage();
            }

            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            long offset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.getTimeInMillis()), TimeUnit.MILLISECONDS);
            if (offset != user.getPreferences().getTimezoneOffset()) {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("preferences.timezoneOffset", String.valueOf(offset));
                apiHelper.apiService.updateUser(updateData).compose(apiHelper.configureApiCallObserver())
                        .subscribe(new MergeUserCallback(this, user), throwable -> {
                        });
            }
            runOnUiThread(() -> {
                updateHeader();
                updateSidebar();
                saveLoginInformation();
                if (activeFragment != null) {
                    activeFragment.updateUserData(user);
                } else {
                    drawer.setSelectionAtPosition(1);
                }
            });

            displayDeathDialogIfNeeded();

            if (!fromLocalDb) {
                displayNewInboxMessagesBadge();
                pushNotificationManager.setUser(user);
                pushNotificationManager.addPushDeviceUsingStoredToken();

                // Update the oldEntries
                new Thread(() -> {

                    // multiple crashes because user is null
                    if (user != null) {
                        ArrayList<Task> allTasks = new ArrayList<>();
                        allTasks.addAll(user.getDailys());
                        allTasks.addAll(user.getTodos());
                        allTasks.addAll(user.getHabits());
                        allTasks.addAll(user.getRewards());

                        loadAndRemoveOldTasks(user.getId(), allTasks);

                        ArrayList<ChecklistItem> allChecklistItems = new ArrayList<>();
                        for (Task t : allTasks) {
                            if (t.checklist != null) {
                                allChecklistItems.addAll(t.checklist);
                            }
                        }
                        loadAndRemoveOldChecklists(allChecklistItems);

                        ArrayList<TaskTag> allTaskTags = new ArrayList<>();
                        for (Task t : allTasks) {
                            if (t.getTags() != null) {
                                allTaskTags.addAll(t.getTags());
                            }
                        }
                        loadAndRemoveOldTaskTags(allTaskTags);

                        ArrayList<RemindersItem> allReminders = new ArrayList<>();
                        for (Task t : allTasks) {
                            if (t.getReminders() != null) {
                                allReminders.addAll(t.getReminders());
                            }
                        }
                        loadAndRemoveOldReminders(allReminders);

                        loadAndRemoveOldTags(user.getTags());

                        updateOwnedDataForUser(user);
                    }
                }).start();
            }
        }
    }

    private void loadAndRemoveOldTasks(String userId, final List<Task> onlineEntries) {
        final ArrayList<String> onlineTaskIdList = new ArrayList<>();

        for (Task oTask : onlineEntries) {
            onlineTaskIdList.add(oTask.getId());
        }

        Where<Task> query = new Select().from(Task.class).where(Condition.column("user_id").eq(userId));
        try {
            if (query.count() != onlineEntries.size()) {

                // Load Database Tasks
                query.async().queryList(new TransactionListener<List<Task>>() {
                    @Override
                    public void onResultReceived(List<Task> tasks) {

                        ArrayList<Task> tasksToDelete = new ArrayList<>();

                        for (Task dbTask : tasks) {
                            if (!onlineTaskIdList.contains(dbTask.getId())) {
                                tasksToDelete.add(dbTask);
                            }
                        }

                        for (Task delTask : tasksToDelete) {
                            // TaskTag
                            new Delete().from(TaskTag.class).where(Condition.column("task_id").eq(delTask.getId())).async().execute();

                            // ChecklistItem
                            new Delete().from(ChecklistItem.class).where(Condition.column("task_id").eq(delTask.getId())).async().execute();

                            // Days
                            new Delete().from(Days.class).where(Condition.column("task_id").eq(delTask.getId())).async().execute();

                            // TASK
                            delTask.async().delete();

                            EventBus.getDefault().post(new TaskRemovedEvent(delTask.getId()));
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<Task>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<Task>> baseTransaction, List<Task> tasks) {
                        return tasks != null && tasks.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }
    }

    private void displayNewInboxMessagesBadge() {
        Integer numberOfUnreadPms = this.user.getInbox().getNewMessages();
        IDrawerItem newInboxItem;

        if (numberOfUnreadPms <= 0) {
            newInboxItem = new PrimaryDrawerItem()
                    .withName(this.getString(R.string.sidebar_inbox))
                    .withIdentifier(MainDrawerBuilder.SIDEBAR_INBOX);
        } else {
            String numberOfUnreadPmsLabel = String.valueOf(numberOfUnreadPms);
            BadgeStyle badgeStyle = new BadgeStyle()
                    .withTextColor(Color.WHITE)
                    .withColorRes(R.color.md_red_700);

            newInboxItem = new PrimaryDrawerItem()
                    .withName(this.getString(R.string.sidebar_inbox))
                    .withIdentifier(MainDrawerBuilder.SIDEBAR_INBOX)
                    .withBadge(numberOfUnreadPmsLabel)
                    .withBadgeStyle(badgeStyle);
        }

        this.drawer.updateItemAtPosition(newInboxItem, this.drawer.getPosition(MainDrawerBuilder.SIDEBAR_INBOX));
    }


    private void loadAndRemoveOldChecklists(final List<ChecklistItem> onlineEntries) {
        final ArrayList<String> onlineChecklistItemIdList = new ArrayList<>();

        for (ChecklistItem item : onlineEntries) {
            onlineChecklistItemIdList.add(item.getId());
        }

        From<ChecklistItem> query = new Select().from(ChecklistItem.class);
        try {
            if (query.count() != onlineEntries.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<ChecklistItem>>() {
                    @Override
                    public void onResultReceived(List<ChecklistItem> items) {

                        ArrayList<ChecklistItem> checkListItemsToDelete = new ArrayList<>();

                        for (ChecklistItem chItem : items) {
                            if (!onlineChecklistItemIdList.contains(chItem.getId())) {
                                checkListItemsToDelete.add(chItem);
                            }
                        }

                        for (ChecklistItem chItem : checkListItemsToDelete) {
                            chItem.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<ChecklistItem>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<ChecklistItem>> baseTransaction, List<ChecklistItem> items) {
                        return items != null && items.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }

    }

    private void loadAndRemoveOldTaskTags(final List<TaskTag> onlineEntries) {
        final ArrayList<String> onlineTaskTagItemIdList = new ArrayList<>();

        for (TaskTag item : onlineEntries) {
            onlineTaskTagItemIdList.add(item.getId());
        }

        From<TaskTag> query = new Select().from(TaskTag.class);
        try {
            if (query.count() != onlineEntries.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<TaskTag>>() {
                    @Override
                    public void onResultReceived(List<TaskTag> items) {

                        ArrayList<TaskTag> checkListItemsToDelete = new ArrayList<>();

                        for (TaskTag ttag : items) {
                            if (!onlineTaskTagItemIdList.contains(ttag.getId())) {
                                checkListItemsToDelete.add(ttag);
                            }
                        }

                        for (TaskTag ttag : checkListItemsToDelete) {
                            ttag.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<TaskTag>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<TaskTag>> baseTransaction, List<TaskTag> items) {
                        return items != null && items.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }

    }

    private void loadAndRemoveOldReminders(final List<RemindersItem> onlineEntries) {
        final ArrayList<String> onlineTaskTagItemIdList = new ArrayList<>();

        for (RemindersItem item : onlineEntries) {
            onlineTaskTagItemIdList.add(item.getId());
        }

        From<RemindersItem> query = new Select().from(RemindersItem.class);
        try {
            if (query.count() != onlineEntries.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<RemindersItem>>() {
                    @Override
                    public void onResultReceived(List<RemindersItem> items) {

                        ArrayList<RemindersItem> remindersToDelete = new ArrayList<>();

                        for (RemindersItem reminder : items) {
                            if (!onlineTaskTagItemIdList.contains(reminder.getId())) {
                                remindersToDelete.add(reminder);
                            }
                        }

                        for (RemindersItem reminder : remindersToDelete) {
                            reminder.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<RemindersItem>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<RemindersItem>> baseTransaction, List<RemindersItem> items) {
                        return items != null && items.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }

    }

    private void loadAndRemoveOldTags(final List<Tag> onlineEntries) {
        final ArrayList<String> onlineTaskTagItemIdList = new ArrayList<>();

        for (Tag item : onlineEntries) {
            onlineTaskTagItemIdList.add(item.getId());
        }

        From<Tag> query = new Select().from(Tag.class);
        try {
            if (query.count() != onlineEntries.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<Tag>>() {
                    @Override
                    public void onResultReceived(List<Tag> items) {

                        ArrayList<Tag> tagsToDelete = new ArrayList<>();

                        for (Tag tag : items) {
                            if (!onlineTaskTagItemIdList.contains(tag.getId())) {
                                tagsToDelete.add(tag);
                            }
                        }

                        for (Tag tag : tagsToDelete) {
                            tag.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<Tag>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<Tag>> transaction, List<Tag> result) {
                        return result != null && result.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }

    }

    private void updateOwnedDataForUser(HabitRPGUser user) {
        if (user == null || user.getItems() == null) {
            return;
        }
        List<BaseModel> updates = new ArrayList<>();
        updates.addAll(this.updateOwnedData(Egg.class, user.getItems().getEggs()));
        updates.addAll(this.updateOwnedData(Food.class, user.getItems().getFood()));
        updates.addAll(this.updateOwnedData(HatchingPotion.class, user.getItems().getHatchingPotions()));
        updates.addAll(this.updateOwnedData(QuestContent.class, user.getItems().getQuests()));

        updates.addAll(this.updateOwnedData(user.getItems().getGear().owned));
        if (!updates.isEmpty()) {
            TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(updates)));
        }
    }

    private <T extends Item> List<T> updateOwnedData(Class<T> itemClass, HashMap<String, Integer> ownedMapping) {
        List<T> updates = new ArrayList<>();
        if (ownedMapping == null) {
            return updates;
        }
        List<T> items = new Select().from(itemClass).queryList();
        for (T item : items) {
            if (ownedMapping.containsKey(item.getKey()) && !item.getOwned().equals(ownedMapping.get(item.getKey()))) {
                item.setOwned(ownedMapping.get(item.getKey()));
                updates.add(item);
            } else if (!ownedMapping.containsKey(item.getKey()) && item.getOwned() > 0) {
                item.setOwned(0);
                updates.add(item);
            }
        }
        return updates;
    }

    private List<ItemData> updateOwnedData(HashMap<String, Boolean> ownedMapping) {
        List<ItemData> updates = new ArrayList<>();
        if (ownedMapping == null) {
            return updates;
        }
        List<ItemData> items = new Select().from(ItemData.class).queryList();
        for (ItemData item : items) {
            if (ownedMapping.containsKey(item.key) && item.owned != ownedMapping.get(item.key)) {
                item.owned = ownedMapping.get(item.key);
                updates.add(item);
            } else if (!ownedMapping.containsKey(item.key) && item.owned != null) {
                item.owned = null;
                updates.add(item);
            }
        }
        return updates;
    }

    private void updateUserAvatars() {
        avatarInHeader.updateData(user);
    }

    private void updateHeader() {
        updateUserAvatars();
        setTitle(user.getProfile().getName());

        android.support.v7.app.ActionBarDrawerToggle actionBarDrawerToggle = drawer.getActionBarDrawerToggle();

        if (actionBarDrawerToggle != null) {
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        }
    }

    public void updateSidebar() {
        final IProfile profile = accountHeader.getProfiles().get(0);
        if (user.getAuthentication() != null) {
            if (user.getAuthentication().getLocalAuthentication() != null) {
                profile.withEmail(user.getAuthentication().getLocalAuthentication().getEmail());
            }
        }
        profile.withName(user.getProfile().getName());
        sideAvatarView.setUser(user);
        sideAvatarView.onAvatarImageReady(avatarImage -> {
            profile.withIcon(avatarImage);
            accountHeader.updateProfile(profile);
        });
        accountHeader.updateProfile(profile);

        if (user.getPreferences() == null || user.getFlags() == null) {
            return;
        }

        IDrawerItem item = drawer.getDrawerItem(MainDrawerBuilder.SIDEBAR_SKILLS);
        if ((user.getPreferences() != null && user.getPreferences().getDisableClasses())
                || (user.getFlags() != null && !user.getFlags().getClassSelected())) {
            if (item != null) {
                drawer.removeItem(MainDrawerBuilder.SIDEBAR_SKILLS);
            }
        } else {
            IDrawerItem newItem = item;
            if (user.getStats().getLvl() < MIN_LEVEL_FOR_SKILLS) {
                newItem = new PrimaryDrawerItem()
                        .withName(this.getString(R.string.sidebar_skills))
                        .withEnabled(false)
                        .withBadge(this.getString(R.string.unlock_lvl_11))
                        .withIdentifier(MainDrawerBuilder.SIDEBAR_SKILLS);
            } else if (user.getStats().getLvl() >= MIN_LEVEL_FOR_SKILLS) {
                newItem = new PrimaryDrawerItem()
                        .withName(this.getString(R.string.sidebar_skills))
                        .withIdentifier(MainDrawerBuilder.SIDEBAR_SKILLS);
            }
            if (item == null) {
                drawer.addItemAtPosition(newItem, 1);
            } else {
                drawer.updateItem(newItem);

            }
        }
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.user = user;
        this.lastSync = new Date();
        MainActivity.this.setUserData(false);
    }

    public void setActiveFragment(BaseMainFragment fragment) {
        this.activeFragment = fragment;
        this.drawer.setSelectionAtPosition(this.activeFragment.fragmentSidebarPosition, false);
    }

    public void onBackPressed() {
        if (this.activeTutorialView != null) {
            this.removeActiveTutorialView();
        }
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (drawer.getDrawerLayout().isDrawerOpen(GravityCompat.END)) {
            drawer.getDrawerLayout().closeDrawer(GravityCompat.END);
            EventBus.getDefault().post(new ToggledEditTagsEvent(false));
        } else {
            super.onBackPressed();
            if (this.activeFragment != null) {
                this.activeFragment.updateUserData(user);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SELECT_CLASS_RESULT) {
            if (this.apiHelper != null) {
                this.apiHelper.retrieveUser(true)
                        .compose(apiHelper.configureApiCallObserver())
                        .subscribe(new HabitRPGUserCallback(this), throwable -> {
                        });
            }
        } else if (requestCode == GEM_PURCHASE_REQUEST) {
            this.apiHelper.retrieveUser(true)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new HabitRPGUserCallback(this), throwable -> {
                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region Events

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(ToggledInnStateEvent evt) {
        avatarInHeader.updateData(user);
    }

    @Subscribe
    public void onEvent(UpdateUserCommand event) {
        apiHelper.apiService.updateUser(event.updateData).compose(apiHelper.configureApiCallObserver())
                .subscribe(new MergeUserCallback(this, user), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(EquipCommand event) {
        this.apiHelper.apiService.equipItem(event.type, event.key)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new ItemsCallback(this, this.user), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(UnlockPathCommand event) {
        this.user.setBalance(this.user.getBalance() - event.balanceDiff);
        this.setUserData(false);
        apiHelper.apiService.unlockPath(event.path)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new UnlockCallback(this, this.user), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(OpenMenuItemCommand event) {
        drawer.setSelection(event.identifier);
    }

    @Subscribe
    public void onEvent(final BuyGemItemCommand event) {
        if (event.item.canBuy(user) || !event.item.getCurrency().equals("gems")) {
            Observable<Void> observable;
            if (event.shopIdentifier.equals(Shop.TIME_TRAVELERS_SHOP)) {
                if (event.item.purchaseType.equals("gear")) {
                    observable = apiHelper.apiService.purchaseMysterySet(event.item.categoryIdentifier);
                } else {
                    observable = apiHelper.apiService.purchaseHourglassItem(event.item.purchaseType, event.item.key);
                }
            } else if (event.item.purchaseType.equals("quests") && event.item.getCurrency().equals("gold")) {
                observable = apiHelper.apiService.purchaseQuest(event.item.key);
            } else {
                observable = apiHelper.apiService.purchaseItem(event.item.purchaseType, event.item.key);
            }
            observable
                    .compose(apiHelper.configureApiCallObserver())
                    .doOnNext(aVoid -> {
                        showSnackbar(this, floatingMenuWrapper, getString(R.string.successful_purchase, event.item.text), SnackbarDisplayType.NORMAL);
                    })
                    .subscribe(buyResponse -> {
                        apiHelper.retrieveUser(false)
                                .compose(apiHelper.configureApiCallObserver())
                                .subscribe(new HabitRPGUserCallback(this), throwable -> {
                                });
                    }, throwable -> {
                        HttpException error = (HttpException) throwable;
                        if (error.code() == 401 && event.item.getCurrency().equals("gems")) {
                            openGemPurchaseFragment(null);
                        }
                    });
        } else {
            openGemPurchaseFragment(null);
        }
    }

    @Subscribe
    public void onEvent(final BuyRewardCommand event) {
        final String rewardKey = event.Reward.getId();

        if (user.getStats().getGp() < event.Reward.getValue()) {
            showSnackbar(this, floatingMenuWrapper, getString(R.string.no_gold), SnackbarDisplayType.FAILURE);
            return;
        }

        if (rewardKey.equals("potion")) {
            int currentHp = user.getStats().getHp().intValue();
            int maxHp = user.getStats().getMaxHealth();

            if (currentHp == maxHp) {
                UiUtils.showSnackbar(this, floatingMenuWrapper, getString(R.string.no_potion), SnackbarDisplayType.FAILURE_BLUE);
                return;
            }
        }

        if (event.Reward.specialTag != null && event.Reward.specialTag.equals("item")) {
            apiHelper.apiService.buyItem(event.Reward.getId())
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(buyResponse -> {
                        String snackbarMessage = getString(R.string.successful_purchase, event.Reward.getText());

                        if (event.Reward.getId().equals("armoire")) {
                            if (buyResponse.armoire.get("type").equals("gear")) {
                                snackbarMessage = getApplicationContext().getString(R.string.armoireEquipment, buyResponse.armoire.get("dropText"));
                            } else if (buyResponse.armoire.get("type").equals("food")) {
                                snackbarMessage = getApplicationContext().getString(R.string.armoireFood, buyResponse.armoire.get("dropArticle"), buyResponse.armoire.get("dropText"));
                            } else {
                                snackbarMessage = getApplicationContext().getString(R.string.armoireExp);
                            }
                        } else if (!event.Reward.getId().equals("potion")) {
                            EventBus.getDefault().post(new TaskRemovedEvent(event.Reward.getId()));
                        }
                        if (buyResponse.items != null) {
                            user.setItems(buyResponse.items);
                        }
                        if (buyResponse.hp != null) {
                            user.getStats().setHp(buyResponse.hp);
                        }
                        if (buyResponse.exp != null) {
                            user.getStats().setExp(buyResponse.exp);
                        }
                        if (buyResponse.mp != null) {
                            user.getStats().setMp(buyResponse.mp);
                        }
                        if (buyResponse.gp != null) {
                            user.getStats().setGp(buyResponse.gp);
                        }
                        if (buyResponse.lvl != null) {
                            user.getStats().setLvl(buyResponse.lvl);
                        }

                        user.async().save();
                        MainActivity.this.setUserData(true);

                        showSnackbar(MainActivity.this, floatingMenuWrapper, snackbarMessage, SnackbarDisplayType.NORMAL);
                    }, throwable -> {
                    });
        } else {
            // user created Rewards
            apiHelper.apiService.postTaskDirection(rewardKey, TaskDirection.down.toString())
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new TaskScoringCallback(this, rewardKey), throwable -> {
                    });
        }

        //Update the users gold
        Stats stats = user.getStats();
        Double gp = stats.getGp() - event.Reward.getValue();
        stats.setGp(gp);

        avatarInHeader.updateData(user);
        user.async().save();
    }

    @Subscribe
    public void onEvent(final DeleteTaskCommand cmd) {
        apiHelper.apiService.deleteTask(cmd.TaskIdToDelete).compose(apiHelper.configureApiCallObserver())
                .subscribe(aVoid -> {
                    EventBus.getDefault().post(new TaskRemovedEvent(cmd.TaskIdToDelete));
                }, throwable -> {
                });
    }

    @Subscribe
    public void openGemPurchaseFragment(OpenGemPurchaseFragmentCommand cmd) {
        drawer.setSelection(MainDrawerBuilder.SIDEBAR_PURCHASE);
    }

    @Subscribe
    public void onEvent(DisplayTutorialEvent tutorialEvent) {
        this.displayTutorialStep(tutorialEvent.step, tutorialEvent.tutorialText);
    }

    @Subscribe
    public void onEvent(DisplayFragmentEvent event) {
        this.displayFragment(event.fragment);
    }

    @Subscribe
    public void onEvent(SellItemCommand event) {
        this.apiHelper.apiService.sellItem(event.item.getType(), event.item.getKey())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(habitRPGUser -> {
                    user.setItems(habitRPGUser.getItems());
                    user.save();
                    user.setStats(habitRPGUser.getStats());
                    setUserData(false);
                }, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(final HatchingCommand event) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            return;
        }
        this.apiHelper.apiService.hatchPet(event.usingEgg.getKey(), event.usingHatchingPotion.getKey())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new ItemsCallback(user1 -> {
                    FrameLayout petWrapper = (FrameLayout) getLayoutInflater().inflate(R.layout.pet_imageview, null);
                    ImageView petImageView = (ImageView) petWrapper.findViewById(R.id.pet_imageview);

                    DataBindingUtils.loadImage(petImageView, "Pet-" + event.usingEgg.getKey() + "-" + event.usingHatchingPotion.getKey());
                    String potionName = event.usingHatchingPotion.getText();
                    String eggName = event.usingEgg.getText();
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.hatched_pet_title, potionName, eggName))
                            .setView(petWrapper)
                            .setPositiveButton(R.string.close, (hatchingDialog, which) -> {
                                hatchingDialog.dismiss();
                            })
                            .setNeutralButton(R.string.share, (hatchingDialog, which) -> {
                                ShareEvent event1 = new ShareEvent();
                                event1.sharedMessage = getString(R.string.share_hatched, potionName, eggName) + " https://habitica.com/social/hatch-pet";
                                Bitmap animalBitmap = ((BitmapDrawable) petImageView.getDrawable()).getBitmap();
                                Bitmap sharedImage = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(sharedImage);
                                canvas.drawColor(getResources().getColor(R.color.brand_300));
                                canvas.drawBitmap(animalBitmap, new Rect(0, 0, animalBitmap.getWidth(), animalBitmap.getHeight()),
                                        new Rect(30, 0, animalBitmap.getWidth() + 30, animalBitmap.getHeight()), new Paint());
                                event1.shareImage = sharedImage;
                                EventBus.getDefault().post(event1);
                                hatchingDialog.dismiss();
                            })
                            .create();
                    dialog.show();
                }, this.user), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(FeedCommand event) {
        if (event.usingFood == null || event.usingPet == null) {
            return;
        }
        final Pet pet = event.usingPet;
        this.apiHelper.apiService.feedPet(event.usingPet.getKey(), event.usingFood.getKey())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(feedResponse -> {
                    MainActivity.this.user.getItems().getPets().put(pet.getKey(), feedResponse.value);
                    MainActivity.this.user.getItems().getFood().put(event.usingFood.getKey(), event.usingFood.getOwned() - 1);
                    MainActivity.this.setUserData(false);
                    showSnackbar(MainActivity.this, floatingMenuWrapper, getString(R.string.notification_pet_fed, pet.getColorText(), pet.getAnimalText()), SnackbarDisplayType.NORMAL);
                    if (feedResponse.value == -1) {
                        FrameLayout mountWrapper = (FrameLayout) getLayoutInflater().inflate(R.layout.pet_imageview, null);
                        ImageView mountImageView = (ImageView) mountWrapper.findViewById(R.id.pet_imageview);

                        DataBindingUtils.loadImage(mountImageView, "Mount_Icon_" + event.usingPet.getKey());
                        String colorName = event.usingPet.getColorText();
                        String animalName = event.usingPet.getAnimalText();
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.hatched_pet_title, colorName, animalName))
                                .setView(mountWrapper)
                                .setPositiveButton(R.string.close, (hatchingDialog, which) -> {
                                    hatchingDialog.dismiss();
                                })
                                .setNeutralButton(R.string.share, (hatchingDialog, which) -> {
                                    ShareEvent event1 = new ShareEvent();
                                    event1.sharedMessage = getString(R.string.share_raised, colorName, animalName) + " https://habitica.com/social/raise-pet";
                                    Bitmap animalBitmap = ((BitmapDrawable) mountImageView.getDrawable()).getBitmap();
                                    Bitmap sharedImage = Bitmap.createBitmap(99, 99, Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(sharedImage);
                                    canvas.drawColor(getResources().getColor(R.color.brand_300));
                                    canvas.drawBitmap(animalBitmap, new Rect(0, 0, animalBitmap.getWidth(), animalBitmap.getHeight()),
                                            new Rect(9, 0, animalBitmap.getWidth() + 9, animalBitmap.getHeight()), new Paint());
                                    event1.shareImage = sharedImage;
                                    EventBus.getDefault().post(event1);
                                    hatchingDialog.dismiss();
                                })
                                .create();
                        dialog.show();
                    }
                }, throwable -> {
                });
    }

    // endregion

    @Subscribe
    public void reloadContent(ReloadContentEvent event) {
        if (!this.isloadingContent) {
            this.isloadingContent = true;
            this.apiHelper.apiService.getContent(apiHelper.languageCode)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(contentResult -> {
                        isloadingContent = false;
                        ContentReloadedEvent event1 = new ContentReloadedEvent();
                        EventBus.getDefault().post(event1);
                    }, throwable -> {
                    });
        }
    }

    @Override
    public void onTaskDataReceived(TaskDirectionData data, Task task) {
        if (task.type.equals("reward")) {

            showSnackbar(this, floatingMenuWrapper, getString(R.string.notification_purchase, task.getText()), SnackbarDisplayType.NORMAL);

        } else {

            if (user != null) {
                notifyUser(data.getExp(), data.getHp(), data.getGp(), data.getMp(), data.getLvl());
            }

            showSnackBarForDataReceived(data);
        }
    }

    private void showSnackBarForDataReceived(final TaskDirectionData data) {
        if (data.get_tmp() != null) {
            if (data.get_tmp().getDrop() != null) {
                new Handler().postDelayed(() -> showSnackbar(MainActivity.this, floatingMenuWrapper, data.get_tmp().getDrop().getDialog(), SnackbarDisplayType.DROP), 3000L);
            }
        }
    }

    private void notifyUser(double xp, double hp, double gold, double mp, int lvl) {
        StringBuilder message = new StringBuilder();
        SnackbarDisplayType displayType = SnackbarDisplayType.NORMAL;
        if (lvl > user.getStats().getLvl()) {
            displayLevelUpDialog(lvl);

            this.apiHelper.retrieveUser(true)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new HabitRPGUserCallback(this), throwable -> {
                    });
            user.getStats().setLvl(lvl);

            showSnackbar(this, floatingMenuWrapper, message.toString(), SnackbarDisplayType.NORMAL);
        } else {
            com.magicmicky.habitrpgwrapper.lib.models.Stats stats = user.getStats();

            if (xp > stats.getExp()) {
                message.append(" + ").append(round(xp - stats.getExp(), 2)).append(" XP");
                user.getStats().setExp(xp);
            }
            if (hp != stats.getHp()) {
                displayType = SnackbarDisplayType.FAILURE;
                message.append(" - ").append(round(stats.getHp() - hp, 2)).append(" HP");
                user.getStats().setHp(hp);
            }
            if (gold > stats.getGp()) {
                message.append(" + ").append(round(gold - stats.getGp(), 2)).append(" GP");
                stats.setGp(gold);
            } else if (gold < stats.getGp()) {
                displayType = SnackbarDisplayType.FAILURE;
                message.append(" - ").append(round(stats.getGp() - gold, 2)).append(" GP");
                stats.setGp(gold);
            }
            if (mp > stats.getMp() && stats.getLvl() >= MIN_LEVEL_FOR_SKILLS) {
                message.append(" + ").append(round(mp - stats.getMp(), 2)).append(" MP");
                stats.setMp(mp);
            }
            showSnackbar(this, floatingMenuWrapper, message.toString(), displayType);
        }
        setUserData(true);
    }

    private void displayDeathDialogIfNeeded() {

        if (user.getStats().getHp() == null || user.getStats().getHp() > 0) {
            return;
        }

        if (this.faintDialog == null) {

            View customView = getLayoutInflater().inflate(R.layout.dialog_faint, null);
            if (customView != null) {
                View hpBarView = customView.findViewById(R.id.hpBar);

                ValueBarBinding hpBar = DataBindingUtil.bind(hpBarView);
                hpBar.setPartyMembers(true);
                AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats(), this);

                dialogAvatarView = (AvatarView) customView.findViewById(R.id.avatarView);
                dialogAvatarView.setUser(user);
            }

            this.faintDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.faint_header)
                    .setView(customView)
                    .setPositiveButton(R.string.faint_button, (dialog, which) -> {
                        faintDialog = null;
                        apiHelper.apiService.revive()
                                .compose(apiHelper.configureApiCallObserver())
                                .subscribe(new MergeUserCallback(MainActivity.this, MainActivity.this.user), throwable -> {
                                });
                    })
                    .create();


            this.faintDialog.show();
        }
    }

    private void displayLevelUpDialog(int level) {
        SuppressedModals suppressedModals = user.getPreferences().getSuppressModals();
        if (suppressedModals != null) {
            if (suppressedModals.getLevelUp()) {
                checkClassSelection();
                return;
            }
        }

        View customView = getLayoutInflater().inflate(R.layout.dialog_levelup, null);
        if (customView != null) {
            TextView detailView = (TextView) customView.findViewById(R.id.levelupDetail);
            detailView.setText(this.getString(R.string.levelup_detail, level));
            dialogAvatarView = (AvatarView) customView.findViewById(R.id.avatarView);
            dialogAvatarView.setUser(user);
        }

        final ShareEvent event = new ShareEvent();
        event.sharedMessage = getString(R.string.share_levelup, level) + " https://habitica.com/social/level-up";
        AvatarView avatarView = new AvatarView(this, true, true, true);
        avatarView.setUser(user);
        avatarView.onAvatarImageReady(avatarImage -> event.shareImage = avatarImage);

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.levelup_header)
                .setView(customView)
                .setPositiveButton(R.string.levelup_button, (dialog, which) -> {
                    checkClassSelection();
                })
                .setNeutralButton(R.string.share, (dialog, which) -> {
                    EventBus.getDefault().post(event);
                    dialog.dismiss();
                })
                .create();

        alert.show();
    }

    private void checkClassSelection() {
        if (user.getStats().getLvl() > 10 &&
                !user.getPreferences().getDisableClasses() &&
                !user.getFlags().getClassSelected()) {
            SelectClassEvent event = new SelectClassEvent();
            event.isInitialSelection = true;
            displayClassSelectionActivity(event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && drawer != null) {
            drawer.openDrawer();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    public FrameLayout getFloatingMenuWrapper() {
        return floatingMenuWrapper;
    }

    private void scheduleReminder(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean("use_reminder", false)) {

            String timeval = prefs.getString("reminder_time", "19:00");

            String[] pieces = timeval.split(":");
            int hour = Integer.parseInt(pieces[0]);
            int minute = Integer.parseInt(pieces[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            long trigger_time = cal.getTimeInMillis();

            Intent notificationIntent = new Intent(context, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.CHECK_DAILIES, false);

            if (PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE) == null) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger_time, AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        }
    }

    @Subscribe
    public void displayClassSelectionActivity(SelectClassEvent event) {
        Bundle bundle = new Bundle();
        bundle.putString("size", user.getPreferences().getSize());
        bundle.putString("skin", user.getPreferences().getSkin());
        bundle.putString("shirt", user.getPreferences().getShirt());
        bundle.putInt("hairBangs", user.getPreferences().getHair().getBangs());
        bundle.putInt("hairBase", user.getPreferences().getHair().getBase());
        bundle.putString("hairColor", user.getPreferences().getHair().getColor());
        bundle.putInt("hairMustache", user.getPreferences().getHair().getMustache());
        bundle.putInt("hairBeard", user.getPreferences().getHair().getBeard());
        bundle.putBoolean("isInitialSelection", event.isInitialSelection);

        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, SELECT_CLASS_RESULT);
    }

    private void displayTutorialStep(TutorialStep step, String text) {
        TutorialView view = new TutorialView(this, step, this);
        view.setTutorialText(text);
        view.onReaction = this;
        this.overlayFrameLayout.addView(view);
        this.activeTutorialView = view;

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("eventLabel", step.getIdentifier() + "-android");
        additionalData.put("eventValue", step.getIdentifier());
        additionalData.put("complete", false);
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData);
    }

    @Override
    public void onTutorialCompleted(TutorialStep step) {
        String path = "flags.tutorial." + step.getTutorialGroup() + "." + step.getIdentifier();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(path, true);
        apiHelper.apiService.updateUser(updateData)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new MergeUserCallback(this, user), throwable -> {
                });
        this.overlayFrameLayout.removeView(this.activeTutorialView);
        this.removeActiveTutorialView();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("eventLabel", step.getIdentifier() + "-android");
        additionalData.put("eventValue", step.getIdentifier());
        additionalData.put("complete", true);
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData);
    }

    @Override
    public void onTutorialDeferred(TutorialStep step) {
        step.setDisplayedOn(new Date());
        step.save();

        this.removeActiveTutorialView();
    }

    private void removeActiveTutorialView() {
        if (this.activeTutorialView != null) {
            this.overlayFrameLayout.removeView(this.activeTutorialView);
            this.activeTutorialView = null;
        }
    }

    public String getUserID() {
        if (this.user != null) {
            return user.getId();
        } else {
            return "";
        }
    }

    @Subscribe
    public void shareEvent(ShareEvent event) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("*/*");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, event.sharedMessage);
        File f = BitmapUtils.saveToShareableFile(getFilesDir() + "/shared_images", "share.png", event.shareImage);
        Uri fileUri = FileProvider.getUriForFile(this, "com.habitrpg.android.habitica.fileprovider", f);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)));
    }

    @Subscribe
    public void onEvent(TaskCheckedCommand event) {
        apiHelper.apiService.postTaskDirection(event.Task.getId(), (event.Task.getCompleted() ? TaskDirection.down : TaskDirection.up).toString())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskScoringCallback(this, event.Task.getId()), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(ChecklistCheckedCommand event) {
        apiHelper.apiService.scoreChecklistItem(event.task.getId(), event.item.getId())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskUpdateCallback(), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(HabitScoreEvent event) {
        apiHelper.apiService.postTaskDirection(event.habit.getId(), (event.Up ? TaskDirection.up : TaskDirection.down).toString())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskScoringCallback(this, event.habit.getId()), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(final TaskSaveEvent event) {
        Task task = event.task;
        if (event.created) {
            this.apiHelper.apiService.createItem(task)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new TaskCreationCallback(), throwable -> {
                    });
        } else {
            this.apiHelper.apiService.updateTask(task.getId(), task)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new TaskUpdateCallback(), throwable -> {
                    });
        }
    }

    private void checkMaintenance() {
        this.maintenanceService.getMaintenanceStatus()
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(maintenanceResponse -> {
                    if (maintenanceResponse.activeMaintenance) {
                        Intent intent = createMaintenanceIntent(maintenanceResponse, false);
                        startActivity(intent);
                    } else {
                        if (maintenanceResponse.minBuild != null) {
                            PackageInfo packageInfo = null;
                            try {
                                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                if (packageInfo.versionCode < maintenanceResponse.minBuild) {
                                    Intent intent = createMaintenanceIntent(maintenanceResponse, true);
                                    startActivity(intent);
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, throwable -> {
                });
    }

    private Intent createMaintenanceIntent(MaintenanceResponse maintenanceResponse, Boolean isDeprecationNotice) {
        Intent intent = new Intent(this, MaintenanceActivity.class);
        Bundle data = new Bundle();
        data.putString("title", maintenanceResponse.title);
        data.putString("imageUrl", maintenanceResponse.imageUrl);
        data.putString("description", maintenanceResponse.description);
        data.putBoolean("deprecationNotice", isDeprecationNotice);
        intent.putExtras(data);
        return intent;
    }

    @Override
    public void call(Throwable throwable) {

    }

    public void unlockDrawer(int gravity) {
        if (this.drawer != null) {
            this.drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, gravity);
        }
    }

    public void lockDrawer(int gravity) {
        if (this.drawer != null) {
            this.drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, gravity);
        }
    }

    public void closeDrawer(int gravity) {
        Drawer drawer;
        if (gravity == GravityCompat.START) {
            drawer = this.drawer;
        } else {
            drawer = this.filterDrawer;
        }
        if (drawer != null) {
            drawer.closeDrawer();
        }
    }

    public void openDrawer(int gravity) {
        Drawer drawer;
        if (gravity == GravityCompat.START) {
            drawer = this.drawer;
        } else {
            drawer = this.filterDrawer;
        }
        if (drawer != null) {
            EventBus.getDefault().post(new ToggledEditTagsEvent(false));
            drawer.openDrawer();
        }
    }

    public void fillFilterDrawer(List<IDrawerItem> items) {
        if (this.filterDrawer != null) {
            this.filterDrawer.removeAllItems();
            for (IDrawerItem item : items) {
                this.filterDrawer.addItem(item);
            }
        }
    }

    public void addFilterDrawerItem(IDrawerItem item) {
        this.filterDrawer.addItem(item);
    }


    @Subscribe
    public void onEvent(OpenFullProfileCommand cmd) {
        if(cmd.MemberId.equals("system"))
            return;

        Bundle bundle = new Bundle();
        bundle.putString("userId", cmd.MemberId);

        Intent intent = new Intent(this, FullProfileActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void removeFilterDrawerItem(int position) {
        this.filterDrawer.removeItemByPosition(position);
    }

    public void updateFilterDrawerItem (IDrawerItem item, int position) {
        this.filterDrawer.removeItemByPosition(position);
        this.filterDrawer.addItemAtPosition(item,position);
    }
}
