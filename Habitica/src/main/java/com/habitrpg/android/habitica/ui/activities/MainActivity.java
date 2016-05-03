package com.habitrpg.android.habitica.ui.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDoneException;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
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
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.UnlockCallback;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.events.ContentReloadedEvent;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.events.ReloadContentEvent;
import com.habitrpg.android.habitica.events.SelectClassEvent;
import com.habitrpg.android.habitica.events.ShareEvent;
import com.habitrpg.android.habitica.events.TaskRemovedEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.DeleteTaskCommand;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.events.commands.HatchingCommand;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.events.commands.SellItemCommand;
import com.habitrpg.android.habitica.events.commands.UnlockPathCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.TutorialView;
import com.habitrpg.android.habitica.ui.UiUtils;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.userpicture.BitmapUtils;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.habitrpg.android.habitica.userpicture.UserPictureRunnable;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.SuppressedModals;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.responses.BuyResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.FeedResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Days;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.habitrpg.android.habitica.ui.UiUtils.SnackbarDisplayType;
import static com.habitrpg.android.habitica.ui.UiUtils.showSnackbar;

public class MainActivity extends BaseActivity implements HabitRPGUserCallback.OnUserReceived,
        TaskScoringCallback.OnTaskScored,
        GemsPurchaseFragment.Listener, TutorialView.OnTutorialReaction {

    private static final int MIN_LEVEL_FOR_SKILLS = 11;
    public static final int SELECT_CLASS_RESULT = 11;

    @Bind(R.id.floating_menu_wrapper)
    FrameLayout floatingMenuWrapper;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.detail_tabs)
    TabLayout detail_tabs;
    @Bind(R.id.avatar_with_bars)
    View avatar_with_bars;
    @Bind(R.id.overlayFrameLayout)
    FrameLayout overlayFrameLayout;

    // Checkout needs to be in the Activity..
    public ActivityCheckout checkout = null;
    public Drawer drawer;
    public Drawer filterDrawer;
    protected HostConfig hostConfig;
    public HabitRPGUser user;
    private AccountHeader accountHeader;
    private BaseMainFragment activeFragment;
    private AvatarWithBarsViewModel avatarInHeader;
    private APIHelper mAPIHelper;
    private AlertDialog faintDialog;

    private UserPicture sideUserPicture;
    private UserPicture dialogUserPicture;

    private Date lastSync;

    private TutorialView activeTutorialView;
    private boolean isloadingContent;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.hostConfig = PrefsActivity.fromContext(this);
        if (!HabiticaApplication.checkUserAuthentication(this, hostConfig))
            return;

        //Check if reminder alarm is set
        scheduleReminder(this);

        HabiticaApplication.ApiHelper = this.mAPIHelper = new APIHelper(hostConfig);

        new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);

        setupToolbar(toolbar);

        avatarInHeader = new AvatarWithBarsViewModel(this, avatar_with_bars);
        accountHeader = MainDrawerBuilder.CreateDefaultAccountHeader(this).build();
        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar, accountHeader)
                .build();
        drawer.setSelectionAtPosition(1, false);
        this.sideUserPicture = new UserPicture(this, true, false);
        this.dialogUserPicture = new UserPicture(this, false, false);

        if (this.filterDrawer == null) {
            filterDrawer = new DrawerBuilder()
                    .withActivity(this)
                    .withDrawerGravity(Gravity.END)
                    .withCloseOnClick(false)
                    .append(this.drawer);
        }

        setupCheckout();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //resync, if last sync was more than 10 minutes ago
        if (this.lastSync == null || (new Date().getTime() - this.lastSync.getTime()) > 600000) {
            if (this.mAPIHelper != null) {
                this.mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
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

    private void setupCheckout() {
        checkout = Checkout.forActivity(this, HabiticaApplication.getInstance(this).getCheckout());
        checkout.start();
    }

    @Override
    public ActivityCheckout getActivityCheckout() {
        return checkout;
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
        fragment.mAPIHelper = mAPIHelper;
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

    private void setUserData(boolean fromLocalDb) {
        if (user != null) {
            Calendar mCalendar = new GregorianCalendar();
            TimeZone mTimeZone = mCalendar.getTimeZone();
            long offset = -TimeUnit.MINUTES.convert(mTimeZone.getOffset(mCalendar.getTimeInMillis()), TimeUnit.MILLISECONDS);
            if (offset != user.getPreferences().getTimezoneOffset()) {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("preferences.timezoneOffset", String.valueOf(offset));
                mAPIHelper.apiService.updateUser(updateData, new HabitRPGUserCallback(this));
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
                            if (t.tags != null) {
                                allTaskTags.addAll(t.tags);
                            }
                        }
                        loadAndRemoveOldTaskTags(allTaskTags);

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

    private void updateOwnedDataForUser(HabitRPGUser user) {
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
        List<T> items = new Select().from(itemClass).queryList();
        List<T> updates = new ArrayList<>();
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
        List<ItemData> items = new Select().from(ItemData.class).queryList();
        List<ItemData> updates = new ArrayList<>();
        for (ItemData item : items) {
            if (ownedMapping.containsKey(item.key) && !Boolean.TRUE.equals(item.owned)) {
                item.owned = true;
                updates.add(item);
            } else if (!ownedMapping.containsKey(item.key) && Boolean.TRUE.equals(item.owned)) {
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
        sideUserPicture.setUser(this.user);
        sideUserPicture.setPictureWithRunnable(avatar -> {
            profile.withIcon(avatar);
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

    @Override
    public void onUserFail() {
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
        } else {
            super.onBackPressed();
            if (this.activeFragment != null) {
                this.activeFragment.updateUserData(user);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkout.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SELECT_CLASS_RESULT) {
            if (this.mAPIHelper != null) {
                this.mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
            }
        }
    }

    @Override
    public void onDestroy() {
        if (checkout != null) {
            checkout.stop();
        }

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    // region Events

    @Subscribe
    public void onEvent(ToggledInnStateEvent evt) {
        avatarInHeader.updateData(user);
    }

    @Subscribe
    public void onEvent(UpdateUserCommand event) {
        mAPIHelper.apiService.updateUser(event.updateData, new HabitRPGUserCallback(this));
    }

    @Subscribe
    public void onEvent(EquipCommand event) {
        this.mAPIHelper.apiService.equipItem(event.type, event.key, new ItemsCallback(this, this.user));
    }

    @Subscribe
    public void onEvent(UnlockPathCommand event) {
        this.user.setBalance(this.user.getBalance() - event.balanceDiff);
        this.setUserData(false);
        mAPIHelper.apiService.unlockPath(event.path, new UnlockCallback(this, this.user));
    }

    @Subscribe
    public void onEvent(OpenMenuItemCommand event) {
        drawer.setSelection(event.identifier);
    }

    @Subscribe
    public void onEvent(final BuyRewardCommand event) {
        final String rewardKey = event.Reward.getId();

        if (user.getStats().getGp() < event.Reward.getValue()) {
            showSnackbar(this, floatingMenuWrapper, "Not enough Gold", SnackbarDisplayType.FAILURE);
            return;
        }


        if (rewardKey.equals("potion")) {
            int currentHp = user.getStats().getHp().intValue();
            int maxHp = user.getStats().getMaxHealth();

            if (currentHp == maxHp) {
                UiUtils.showSnackbar(this, floatingMenuWrapper, "You don't need to buy an health potion", SnackbarDisplayType.FAILURE_BLUE);
                return;
            }
        }

        if (event.Reward.specialTag != null && event.Reward.specialTag.equals("item")) {
            mAPIHelper.apiService.buyItem(event.Reward.getId(), new Callback<BuyResponse>() {

                @Override
                public void success(BuyResponse buyResponse, Response response) {
                    String snackbarMessage = event.Reward.getText() + " successfully purchased!";

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

                    user.setItems(buyResponse.items);
                    user.setStats(buyResponse.stats);
                    user.setFlags(buyResponse.flags);

                    user.async().save();
                    MainActivity.this.setUserData(true);

                    showSnackbar(MainActivity.this, floatingMenuWrapper, snackbarMessage, SnackbarDisplayType.NORMAL);
                }

                @Override
                public void failure(RetrofitError error) {

                    showSnackbar(MainActivity.this, floatingMenuWrapper, "Buy Reward Error " + event.Reward.getText(), SnackbarDisplayType.FAILURE);
                }
            });
        } else {
            // user created Rewards
            mAPIHelper.updateTaskDirection(rewardKey, TaskDirection.down, new TaskScoringCallback(this, rewardKey));
        }

        avatarInHeader.updateData(user);
        user.async().save();
    }

    @Subscribe
    public void onEvent(final DeleteTaskCommand cmd) {
        mAPIHelper.apiService.deleteTask(cmd.TaskIdToDelete, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                EventBus.getDefault().post(new TaskRemovedEvent(cmd.TaskIdToDelete));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Subscribe
    public void onEvent(OpenGemPurchaseFragmentCommand cmd) {
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
        this.mAPIHelper.apiService.sellItem(event.item.getType(), event.item.getKey(), new Callback<HabitRPGUser>() {
            @Override
            public void success(HabitRPGUser habitRPGUser, Response response) {
                user.setItems(habitRPGUser.getItems());
                user.save();
                user.setStats(habitRPGUser.getStats());
                setUserData(false);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Subscribe
    public void onEvent(final HatchingCommand event) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            return;
        }
        this.mAPIHelper.apiService.hatchPet(event.usingEgg.getKey(), event.usingHatchingPotion.getKey(), new ItemsCallback(new HabitRPGUserCallback.OnUserReceived() {
            @Override
            public void onUserReceived(HabitRPGUser user) {
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
                            ShareEvent event = new ShareEvent();
                            event.sharedMessage = getString(R.string.share_hatched, potionName, eggName) + " https://habitica.com/social/hatch-pet";
                            Bitmap animalBitmap = ((BitmapDrawable)petImageView.getDrawable()).getBitmap();
                            Bitmap sharedImage = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(sharedImage);
                            canvas.drawColor(getResources().getColor(R.color.brand_300));
                            canvas.drawBitmap(animalBitmap, new Rect(0, 0, animalBitmap.getWidth(), animalBitmap.getHeight()),
                                    new Rect(30, 0, animalBitmap.getWidth() + 30, animalBitmap.getHeight()), new Paint());
                            event.shareImage = sharedImage;
                            EventBus.getDefault().post(event);
                            hatchingDialog.dismiss();
                        })
                        .create();
                dialog.show();
            }

            @Override
            public void onUserFail() {
            }
        }, this.user));
    }

    @Subscribe
    public void onEvent(FeedCommand event) {
        if (event.usingFood == null || event.usingPet == null) {
            return;
        }
        final Pet pet = event.usingPet;
        this.mAPIHelper.apiService.feedPet(event.usingPet.getKey(), event.usingFood.getKey(), new Callback<FeedResponse>() {
            @Override
            public void success(FeedResponse feedResponse, Response response) {
                MainActivity.this.user.getItems().getPets().put(pet.getKey(), feedResponse.value);
                MainActivity.this.user.getItems().getFood().put(event.usingFood.getKey(), event.usingFood.getOwned()-1);
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
                                ShareEvent event = new ShareEvent();
                                event.sharedMessage = getString(R.string.share_raised, colorName, animalName) + " https://habitica.com/social/raise-pet";
                                Bitmap animalBitmap = ((BitmapDrawable)mountImageView.getDrawable()).getBitmap();
                                Bitmap sharedImage = Bitmap.createBitmap(99, 99, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(sharedImage);
                                canvas.drawColor(getResources().getColor(R.color.brand_300));
                                canvas.drawBitmap(animalBitmap, new Rect(0, 0, animalBitmap.getWidth(), animalBitmap.getHeight()),
                                        new Rect(9, 0, animalBitmap.getWidth() + 9, animalBitmap.getHeight()), new Paint());
                                event.shareImage = sharedImage;
                                EventBus.getDefault().post(event);
                                hatchingDialog.dismiss();
                            })
                            .create();
                    dialog.show();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Subscribe
    public void reloadContent(ReloadContentEvent event) {
        if (!this.isloadingContent) {
            this.isloadingContent = true;
            this.mAPIHelper.apiService.getContent(new Callback<ContentResult>() {
                @Override
                public void success(ContentResult contentResult, Response response) {
                    isloadingContent = false;
                    ContentReloadedEvent event = new ContentReloadedEvent();
                    EventBus.getDefault().post(event);
                }

                @Override
                public void failure(RetrofitError error) {
                    isloadingContent = false;
                }
            });
        }
    }

    // endregion

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

            this.mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
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

    @Override
    public void onTaskScoringFailed() {
        //Do nothing
    }

    static public Double round(Double value, int n) {
        return (Math.round(value * Math.pow(10, n))) / (Math.pow(10, n));
    }

    private void displayDeathDialogIfNeeded() {

        if (user.getStats().getHp() > 0) {
            return;
        }

        if (this.faintDialog == null) {

            View customView = getLayoutInflater().inflate(R.layout.dialog_faint, null);
            if (customView != null) {
                View hpBarView = customView.findViewById(R.id.hpBar);

                ValueBarBinding hpBar = DataBindingUtil.bind(hpBarView);
                hpBar.setPartyMembers(true);
                AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats(), this);

                ImageView avatarView = (ImageView) customView.findViewById(R.id.avatarView);
                this.dialogUserPicture.setUser(this.user);
                this.dialogUserPicture.setPictureOn(avatarView);
            }

            this.faintDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.faint_header)
                    .setView(customView)
                    .setPositiveButton(R.string.faint_button, (dialog, which) -> {
                        faintDialog = null;
                        mAPIHelper.reviveUser(new HabitRPGUserCallback(MainActivity.this));
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
            ImageView avatarView = (ImageView) customView.findViewById(R.id.avatarView);
            this.dialogUserPicture.setUser(this.user);
            this.dialogUserPicture.setPictureOn(avatarView);
        }

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.levelup_header)
                .setView(customView)
                .setPositiveButton(R.string.levelup_button, (dialog, which) -> {
                    checkClassSelection();
                })
                .setNeutralButton(R.string.share, (dialog, which) -> {
                    ShareEvent event = new ShareEvent();
                    event.sharedMessage = getString(R.string.share_levelup, level) + " https://habitica.com/social/level-up";
                    UserPicture picture = new UserPicture(this, true, true);
                    picture.setUser(this.user);
                    picture.setPictureWithRunnable(avatarBitmap -> {
                        event.shareImage = avatarBitmap;
                        EventBus.getDefault().post(event);
                        dialog.dismiss();
                    });
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

        JSONObject eventProperties = new JSONObject();
        try {
            eventProperties.put("eventAction", "tutorial");
            eventProperties.put("eventCategory", "behaviour");
            eventProperties.put("hitType", "event");
            eventProperties.put("eventLabel", step.getIdentifier() + "-android");
            eventProperties.put("eventValue", step.getIdentifier());
            eventProperties.put("complete", false);
        } catch (JSONException exception) {
        }
        Amplitude.getInstance().logEvent("tutorial", eventProperties);
    }

    @Override
    public void onTutorialCompleted(TutorialStep step) {
        String path = "flags.tutorial." + step.getTutorialGroup() + "." + step.getIdentifier();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(path, true);
        mAPIHelper.apiService.updateUser(updateData, new HabitRPGUserCallback(this));
        this.overlayFrameLayout.removeView(this.activeTutorialView);
        this.removeActiveTutorialView();

        JSONObject eventProperties = new JSONObject();
        try {
            eventProperties.put("eventAction", "tutorial");
            eventProperties.put("eventCategory", "behaviour");
            eventProperties.put("hitType", "event");
            eventProperties.put("eventLabel", step.getIdentifier() + "-android");
            eventProperties.put("eventValue", step.getIdentifier());
            eventProperties.put("complete", true);
        } catch (JSONException exception) {
        }
        Amplitude.getInstance().logEvent("tutorial", eventProperties);
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
}
