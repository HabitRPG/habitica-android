package com.habitrpg.android.habitica.ui.activities;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.api.MaintenanceApiService;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.events.DisplayTutorialEvent;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.OpenMysteryItemEvent;
import com.habitrpg.android.habitica.events.OpenedMysteryItemEvent;
import com.habitrpg.android.habitica.events.ReloadContentEvent;
import com.habitrpg.android.habitica.events.SelectClassEvent;
import com.habitrpg.android.habitica.events.ShareEvent;
import com.habitrpg.android.habitica.events.TaskRemovedEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.events.commands.HatchingCommand;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.events.commands.UnlockPathCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.helpers.AmplitudeManager;
import com.habitrpg.android.habitica.helpers.LanguageHelper;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;
import com.habitrpg.android.habitica.interactors.BuyRewardUseCase;
import com.habitrpg.android.habitica.interactors.CheckClassSelectionUseCase;
import com.habitrpg.android.habitica.interactors.ChecklistCheckUseCase;
import com.habitrpg.android.habitica.interactors.DailyCheckUseCase;
import com.habitrpg.android.habitica.interactors.DisplayItemDropUseCase;
import com.habitrpg.android.habitica.interactors.HabitScoreUseCase;
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase;
import com.habitrpg.android.habitica.interactors.TodoCheckUseCase;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.responses.MaintenanceResponse;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.user.SpecialItems;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.TutorialView;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.menu.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.views.ValueBar;
import com.habitrpg.android.habitica.userpicture.BitmapUtils;
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider;
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider;
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider;
import com.habitrpg.android.habitica.widget.TodoListWidgetProvider;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.roughike.bottombar.BottomBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Observable;
import rx.functions.Action1;

import static android.os.Build.VERSION.SDK_INT;
import static com.habitrpg.android.habitica.interactors.NotifyUserUseCase.MIN_LEVEL_FOR_SKILLS;
import static com.habitrpg.android.habitica.ui.helpers.UiUtils.SnackbarDisplayType;
import static com.habitrpg.android.habitica.ui.helpers.UiUtils.showSnackbar;

public class MainActivity extends BaseActivity implements Action1<Throwable>, TutorialView.OnTutorialReaction {

    public static final int SELECT_CLASS_RESULT = 11;
    public static final int GEM_PURCHASE_REQUEST = 111;
    @Inject
    public ApiClient apiClient;

    @Inject
    public SoundManager soundManager;

    @Inject
    public MaintenanceApiService maintenanceService;
    public User user;
    @BindView(R.id.floating_menu_wrapper)
    public ViewGroup floatingMenuWrapper;
    @BindView(R.id.bottom_navigation)
    BottomBar bottomNavigation;
    @Inject
    protected HostConfig hostConfig;
    @Inject
    protected SharedPreferences sharedPreferences;
    @Inject
    CrashlyticsProxy crashlyticsProxy;
    @BindView(R.id.appbar)
    AppBarLayout appBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.detail_tabs)
    TabLayout detail_tabs;
    @BindView(R.id.avatar_with_bars)
    View avatar_with_bars;
    @BindView(R.id.overlayFrameLayout)
    ViewGroup overlayLayout;

    @Inject
    PushNotificationManager pushNotificationManager;
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
    CheckClassSelectionUseCase checkClassSelectionUseCase;

    @Inject
    DisplayItemDropUseCase displayItemDropUseCase;

    @Inject
    NotifyUserUseCase notifyUserUseCase;

    @Inject
    TaskRepository taskRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    TagRepository tagRepository;
    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    TaskAlarmManager taskAlarmManager;

    // endregion

    @Nullable
    private Drawer drawer;
    @Nullable
    private AccountHeader accountHeader;
    @Nullable
    private BaseMainFragment activeFragment;
    private AvatarWithBarsViewModel avatarInHeader;
    private AlertDialog faintDialog;
    private AvatarView sideAvatarView;
    private TutorialView activeTutorialView;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        LanguageHelper languageHelper = new LanguageHelper(sharedPreferences.getString("language", "en"));
        Locale.setDefault(languageHelper.getLocale());
        Configuration configuration = new Configuration();
        if (SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            configuration.locale = languageHelper.getLocale();
        } else {
            configuration.setLocale(languageHelper.getLocale());
        }
        getResources().updateConfiguration(configuration,
                getResources().getDisplayMetrics());


        if (!HabiticaApplication.checkUserAuthentication(this, hostConfig)) {
            return;
        }

        setupToolbar(toolbar);

        avatarInHeader = new AvatarWithBarsViewModel(this, avatar_with_bars);
        accountHeader = MainDrawerBuilder.CreateDefaultAccountHeader(this).build();
        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, sharedPreferences, toolbar, accountHeader)
                .build();
        drawer.setSelectionAtPosition(1, false);
        sideAvatarView = new AvatarView(this, true, false, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_10_alpha));
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            avatar_with_bars.setPadding((int)px, getStatusBarHeight(), (int)px, 0);
        }

        userRepository.getUser(hostConfig.getUser())
                .subscribe(newUser -> {
                    MainActivity.this.user = newUser;
                    MainActivity.this.setUserData(true);
                }, ReactiveErrorHandler.handleEmptyError());

        EventBus.getDefault().register(this);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //resync, if last sync was more than 10 minutes ago
        if (this.apiClient != null && this.apiClient.hasAuthenticationKeys()) {
            retrieveUser();
            this.checkMaintenance();
        }

        reloadContent(null);

        if (this.sharedPreferences.getLong("lastReminderSchedule", 0) < new Date().getTime() - 86400000) {
            try {
                taskAlarmManager.scheduleAllSavedAlarms();
            } catch (Exception e) {
                crashlyticsProxy.logException(e);
            }
        }

        //after the activity has been stopped and is thereafter resumed,
        //a state can arise in which the active fragment no longer has a
        //reference to the tabLayout (and all its adapters are null).
        //Recreate the fragment as a result.
        if (activeFragment != null && activeFragment.tabLayout == null) {
            activeFragment = null;
            if (drawer != null) {
                drawer.setSelectionAtPosition(this.sharedPreferences.getInt("lastActivePosition", 1));
            }
        }

        if (isAlwaysFinishActivitiesOptionEnabled()) {
            if (!sharedPreferences.getBoolean("showedFinishActivitiesWarning", false)) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.warning))
                        .setMessage(R.string.dont_keep_activities_warning)
                        .setNeutralButton(R.string.close, (warningDialog, which) -> warningDialog.dismiss())
                        .setPositiveButton(R.string.open_settings, (hatchingDialog, which) -> {
                            showDeveloperOptionsScreen();
                            hatchingDialog.dismiss();
                        })
                        .create();
                dialog.show();
                sharedPreferences.edit().putBoolean("showedFinishActivitiesWarning", true).apply();
            }
        }
    }

    @Override
    protected void onPause() {
        updateWidget(AvatarStatsWidgetProvider.class);
        updateWidget(TodoListWidgetProvider.class);
        updateWidget(DailiesWidgetProvider.class);
        updateWidget(HabitButtonWidgetProvider.class);
        super.onPause();
    }

    private void updateWidget(Class widgetClass) {
        Intent intent = new Intent(this, widgetClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), widgetClass));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }


    private void saveLoginInformation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        boolean ans = editor.putString(getString(R.string.SP_username), user.getAuthentication().getLocalAuthentication().getUsername())
                .putString(getString(R.string.SP_email), user.getAuthentication().getLocalAuthentication().getEmail())
                .commit();

        if (!ans) {
            Log.e("SHARED PREFERENCES", "Shared Preferences Username and Email error");
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public void displayFragment(BaseMainFragment fragment) {
        if (this.activeFragment != null && fragment.getClass() == this.activeFragment.getClass()) {
            return;
        }
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && this.isDestroyed()) {
            return;
        }
        this.activeFragment = fragment;
        fragment.setArguments(getIntent().getExtras());
        fragment.setUser(user);
        fragment.setActivity(this);
        fragment.setTabLayout(detail_tabs);
        fragment.setBottomNavigation(bottomNavigation);
        fragment.setFloatingMenuWrapper(floatingMenuWrapper);


        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        }
    }

    protected void setUserData(boolean fromLocalDb) {
        if (user != null) {

            Preferences preferences = user.getPreferences();

            if (preferences != null) {
                apiClient.setLanguageCode(preferences.getLanguage());
                soundManager.setSoundTheme(preferences.getSound());
            }

            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            long offset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.getTimeInMillis()), TimeUnit.MILLISECONDS);
            if (offset != user.getPreferences().getTimezoneOffset()) {
                userRepository.updateUser(user, "preferences.timezoneOffset", String.valueOf(offset))
                        .subscribe(user1 -> {}, throwable -> {
                        });
            }
            runOnUiThread(() -> {
                updateHeader();
                updateSidebar();
                saveLoginInformation();
                if (activeFragment != null) {
                    activeFragment.updateUserData(user);
                } else {
                    if (drawer != null) {
                        drawer.setSelectionAtPosition(1);
                    }
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

                        /*taskRepository.removeOldTasks(user.getId(), allTasks);

                        ArrayList<ChecklistItem> allChecklistItems = new ArrayList<>();
                        for (Task t : allTasks) {
                            if (t.checklist != null) {
                                allChecklistItems.addAll(t.checklist);
                            }
                        }
                        taskRepository.removeOldChecklists(allChecklistItems);

                        ArrayList<TaskTag> allTaskTags = new ArrayList<>();
                        for (Task t : allTasks) {
                            if (t.getTags() != null) {
                                allTaskTags.addAll(t.getTags());
                            }
                        }
                        taskRepository.removeOldTaskTags(allTaskTags);

                        ArrayList<RemindersItem> allReminders = new ArrayList<>();
                        for (Task t : allTasks) {
                            if (t.getReminders() != null) {
                                allReminders.addAll(t.getReminders());
                            }
                        }
                        taskRepository.removeOldReminders(allReminders);

                        tagRepository.removeOldTags(user.getTags());*/

                        inventoryRepository.updateOwnedEquipment(user);
                    }
                }).start();
            }
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

        if (this.drawer != null) {
            this.drawer.updateItemAtPosition(newInboxItem, this.drawer.getPosition(MainDrawerBuilder.SIDEBAR_INBOX));
        }
    }

    private void updateUserAvatars() {
        if (avatarInHeader != null) {
            avatarInHeader.updateData(user);
        }
    }

    private void updateHeader() {
        updateUserAvatars();
        if (activeFragment != null) {
            setTranslatedFragmentTitle(activeFragment);
        }

        if (drawer != null) {
            android.support.v7.app.ActionBarDrawerToggle actionBarDrawerToggle = drawer.getActionBarDrawerToggle();
            if (actionBarDrawerToggle != null) {
                actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        }
    }

    public void updateSidebar() {
        if (accountHeader != null) {
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
        }

        if (user.getPreferences() == null || user.getFlags() == null) {
            return;
        }

        SpecialItems specialItems = user.getItems().getSpecial();
        Boolean hasSpecialItems = false;
        if (specialItems != null) {
            hasSpecialItems = specialItems.hasSpecialItems();
        }

        if (drawer != null) {
            IDrawerItem item = drawer.getDrawerItem(MainDrawerBuilder.SIDEBAR_SKILLS);
            if (((user.getPreferences() != null && user.getPreferences().getDisableClasses())
                    || (user.getFlags() != null && !user.getFlags().getClassSelected()))
                    && !hasSpecialItems) {
                if (item != null) {
                    drawer.removeItem(MainDrawerBuilder.SIDEBAR_SKILLS);
                }
            } else {
                IDrawerItem newItem;
                if (user.getStats().getLvl() < MIN_LEVEL_FOR_SKILLS && !hasSpecialItems) {
                    newItem = new PrimaryDrawerItem()
                            .withName(this.getString(R.string.sidebar_skills))
                            .withEnabled(false)
                            .withBadge(this.getString(R.string.unlock_lvl_11))
                            .withIdentifier(MainDrawerBuilder.SIDEBAR_SKILLS);
                } else {
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
    }

    public void setActiveFragment(@Nullable BaseMainFragment fragment) {
        this.activeFragment = fragment;
        setTranslatedFragmentTitle(fragment);
        if (this.drawer != null && this.activeFragment != null) {
            this.drawer.setSelectionAtPosition(this.activeFragment.fragmentSidebarPosition, false);
        }
    }

    private void setTranslatedFragmentTitle(@Nullable BaseMainFragment fragment) {
        if (getSupportActionBar() == null) {
            return;
        }
        if (fragment != null && fragment.customTitle() != null) {
            getSupportActionBar().setTitle(fragment.customTitle());
        } else if (user != null && user.getProfile() != null) {
            getSupportActionBar().setTitle(user.getProfile().getName());
        }
    }

    public void onBackPressed() {
        if (this.activeTutorialView != null) {
            this.removeActiveTutorialView();
        }
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
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
            retrieveUser();
        } else if (requestCode == GEM_PURCHASE_REQUEST) {
            retrieveUser();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region Events

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        taskRepository.close();
        userRepository.close();
        tagRepository.close();
        inventoryRepository.close();
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(ToggledInnStateEvent evt) {
        avatarInHeader.updateData(user);
    }

    @Subscribe
    public void onEvent(UpdateUserCommand event) {
        userRepository.updateUser(user, event.updateData)
                .subscribe(user1 -> {}, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(UnlockPathCommand event) {
        this.user.setBalance(this.user.getBalance() - event.balanceDiff);
        this.setUserData(false);
        apiClient.unlockPath(event.path)
                .subscribe(unlockResponse -> {}, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(OpenMenuItemCommand event) {
        if (drawer != null) {
            drawer.setSelection(event.identifier);
        }
    }

    @Subscribe
    public void onEvent(final BuyGemItemCommand event) {
        if (event.item.canBuy(user) || !event.item.getCurrency().equals("gems")) {
            Observable<Void> observable;
            if (event.shopIdentifier.equals(Shop.TIME_TRAVELERS_SHOP)) {
                if (event.item.purchaseType.equals("gear")) {
                    observable = apiClient.purchaseMysterySet(event.item.categoryIdentifier);
                } else {
                    observable = apiClient.purchaseHourglassItem(event.item.purchaseType, event.item.key);
                }
            } else if (event.item.purchaseType.equals("quests") && event.item.getCurrency().equals("gold")) {
                observable = apiClient.purchaseQuest(event.item.key);
            } else {
                observable = apiClient.purchaseItem(event.item.purchaseType, event.item.key);
            }
            observable
                    .doOnNext(aVoid -> showSnackbar(this, floatingMenuWrapper, getString(R.string.successful_purchase, event.item.text), SnackbarDisplayType.NORMAL))
                    .subscribe(buyResponse -> userRepository.retrieveUser(false)
                            .subscribe(user1 -> {}, throwable -> {
                            }), throwable -> {
                        retrofit2.HttpException error = (retrofit2.HttpException) throwable;
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
            apiClient.buyItem(event.Reward.getId())
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
                                    soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop);
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

                                MainActivity.this.setUserData(true);

                                showSnackbar(MainActivity.this, floatingMenuWrapper, snackbarMessage, SnackbarDisplayType.NORMAL);
                            }, throwable -> {
                            }
                    );
        } else {
            buyRewardUseCase.observable(new BuyRewardUseCase.RequestValues(user, event.Reward))
                    .subscribe(res -> showSnackbar(this, floatingMenuWrapper, getString(R.string.notification_purchase, event.Reward.getText()), SnackbarDisplayType.NORMAL), error -> {
                    });
        }

        //Update the users gold
        Stats stats = user.getStats();
        Double gp = stats.getGp() - event.Reward.getValue();
        stats.setGp(gp);

        avatarInHeader.updateData(user);
    }

    @Subscribe
    public void openMysteryItem(OpenMysteryItemEvent event) {
        apiClient.openMysteryItem()
                .subscribe(mysteryItem -> userRepository.retrieveUser(false)
                        .subscribe(user1 -> {
                            OpenedMysteryItemEvent openedEvent = new OpenedMysteryItemEvent();
                            openedEvent.numberLeft = user1.getPurchased().getPlan().mysteryItems.size();
                            openedEvent.mysteryItem = mysteryItem;
                            EventBus.getDefault().post(openedEvent);
                        }, throwable -> {
                        }), throwable -> {
                });
    }

    @Subscribe
    public void openGemPurchaseFragment(@Nullable OpenGemPurchaseFragmentCommand cmd) {
        if (drawer != null) {
            drawer.setSelection(MainDrawerBuilder.SIDEBAR_PURCHASE);
        }
    }

    @Subscribe
    public void onEvent(DisplayTutorialEvent tutorialEvent) {
        if (tutorialEvent.tutorialText != null) {
            this.displayTutorialStep(tutorialEvent.step, tutorialEvent.tutorialText, tutorialEvent.canBeDeferred);
        } else {
            this.displayTutorialStep(tutorialEvent.step, tutorialEvent.tutorialTexts, tutorialEvent.canBeDeferred);
        }
    }

    @Subscribe
    public void onEvent(DisplayFragmentEvent event) {
        this.displayFragment(event.fragment);
    }

    @Subscribe
    public void onEvent(final HatchingCommand event) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            return;
        }
        this.inventoryRepository.hatchPet(event.usingEgg, event.usingHatchingPotion)
                .subscribe(items -> {
                    FrameLayout petWrapper = (FrameLayout) View.inflate(this, R.layout.pet_imageview, null);
                    SimpleDraweeView petImageView = (SimpleDraweeView) petWrapper.findViewById(R.id.pet_imageview);

                    DataBindingUtils.loadImage(petImageView, "Pet-" + event.usingEgg.getKey() + "-" + event.usingHatchingPotion.getKey());
                    String potionName = event.usingHatchingPotion.getText();
                    String eggName = event.usingEgg.getText();
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.hatched_pet_title, potionName, eggName))
                            .setView(petWrapper)
                            .setPositiveButton(R.string.close, (hatchingDialog, which) -> hatchingDialog.dismiss())
                            .setNeutralButton(R.string.share, (hatchingDialog, which) -> {
                                ShareEvent event1 = new ShareEvent();
                                event1.sharedMessage = getString(R.string.share_hatched, potionName, eggName) + " https://habitica.com/social/hatch-pet";
                                Bitmap sharedImage = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(sharedImage);
                                canvas.drawColor(ContextCompat.getColor(this, R.color.brand_300));
                                petImageView.getDrawable().draw(canvas);
                                event1.shareImage = sharedImage;
                                EventBus.getDefault().post(event1);
                                hatchingDialog.dismiss();
                            })
                            .create();
                    dialog.show();
                }, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(FeedCommand event) {
        if (event.usingFood == null || event.usingPet == null) {
            return;
        }
        final Pet pet = event.usingPet;
        this.inventoryRepository.feedPet(event.usingPet, event.usingFood)
                .subscribe(feedResponse -> {
                    showSnackbar(MainActivity.this, floatingMenuWrapper, getString(R.string.notification_pet_fed, pet.getColorText(), pet.getAnimalText()), SnackbarDisplayType.NORMAL);
                    if (feedResponse.value == -1) {
                        FrameLayout mountWrapper = (FrameLayout) View.inflate(this, R.layout.pet_imageview, null);
                        SimpleDraweeView mountImageView = (SimpleDraweeView) mountWrapper.findViewById(R.id.pet_imageview);

                        DataBindingUtils.loadImage(mountImageView, "Mount_Icon_" + event.usingPet.getKey());
                        String colorName = event.usingPet.getColorText();
                        String animalName = event.usingPet.getAnimalText();
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.hatched_pet_title, colorName, animalName))
                                .setView(mountWrapper)
                                .setPositiveButton(R.string.close, (hatchingDialog, which) -> hatchingDialog.dismiss())
                                .setNeutralButton(R.string.share, (hatchingDialog, which) -> {
                                    ShareEvent event1 = new ShareEvent();
                                    event1.sharedMessage = getString(R.string.share_raised, colorName, animalName) + " https://habitica.com/social/raise-pet";
                                    Bitmap sharedImage = Bitmap.createBitmap(99, 99, Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(sharedImage);
                                    canvas.drawColor(ContextCompat.getColor(this, R.color.brand_300));
                                    mountImageView.getDrawable().draw(canvas);
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
        this.inventoryRepository.retrieveContent()
                .subscribe(contentResult -> {}, ReactiveErrorHandler.handleEmptyError());
    }

    public void displayTaskScoringResponse(TaskScoringResult data) {
        if (user != null) {
            notifyUserUseCase.observable(new NotifyUserUseCase.RequestValues(this, floatingMenuWrapper,
                    user, data.experienceDelta, data.healthDelta, data.goldDelta, data.manaDelta, data.hasLeveledUp))
                    .subscribe(aVoid -> {}, ReactiveErrorHandler.handleEmptyError());
        }

        displayItemDropUseCase.observable(new DisplayItemDropUseCase.RequestValues(data, this, floatingMenuWrapper))
                .subscribe(aVoid -> {}, ReactiveErrorHandler.handleEmptyError());
    }


    private void displayDeathDialogIfNeeded() {

        if (user.getStats().getHp() == null || user.getStats().getHp() > 0) {
            return;
        }

        if (this.faintDialog == null) {

            View customView = View.inflate(this, R.layout.dialog_faint, null);
            if (customView != null) {
                ValueBar hpBarView = (ValueBar) customView.findViewById(R.id.hpBar);

                hpBarView.setLightBackground(true);

                AvatarView dialogAvatarView = (AvatarView) customView.findViewById(R.id.avatarView);
                dialogAvatarView.setUser(user);
            }

            this.faintDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.faint_header)
                    .setView(customView)
                    .setPositiveButton(R.string.faint_button, (dialog, which) -> {
                        faintDialog = null;
                        userRepository.revive(user)
                                .subscribe(user1 -> {}, throwable -> {
                                });
                    })
                    .create();

            soundManager.loadAndPlayAudio(SoundManager.SoundDeath);
            this.faintDialog.show();
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

    public ViewGroup getFloatingMenuWrapper() {
        return floatingMenuWrapper;
    }

    protected void retrieveUser() {
        if (this.userRepository != null) {
            this.userRepository.retrieveUser(true)
                    .subscribe(user1 -> {}, ReactiveErrorHandler.handleEmptyError());
        }
    }

    @Subscribe
    public void displayClassSelectionActivity(SelectClassEvent event) {
        checkClassSelectionUseCase.observable(new CheckClassSelectionUseCase.RequestValues(user, event))
                .subscribe(aVoid -> {
                }, throwable -> {
                });
    }

    private void displayTutorialStep(TutorialStep step, String text, boolean canBeDeferred) {
        TutorialView view = new TutorialView(this, step, this);
        view.setTutorialText(text);
        view.onReaction = this;
        view.setCanBeDeferred(canBeDeferred);
        this.overlayLayout.addView(view);
        this.activeTutorialView = view;

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("eventLabel", step.getIdentifier() + "-android");
        additionalData.put("eventValue", step.getIdentifier());
        additionalData.put("complete", false);
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData);
    }

    private void displayTutorialStep(TutorialStep step, List<String> texts, boolean canBeDeferred) {
        TutorialView view = new TutorialView(this, step, this);
        view.setTutorialTexts(texts);
        view.onReaction = this;
        view.setCanBeDeferred(canBeDeferred);
        this.overlayLayout.addView(view);
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
        userRepository.updateUser(user,  updateData)
                .subscribe(user1 -> {}, throwable -> {
                });
        this.overlayLayout.removeView(this.activeTutorialView);
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

        this.removeActiveTutorialView();
    }

    private void removeActiveTutorialView() {
        if (this.activeTutorialView != null) {
            this.overlayLayout.removeView(this.activeTutorialView);
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
        Uri fileUri = FileProvider.getUriForFile(this, getString(R.string.content_provider), f);
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
        switch (event.Task.type) {
            case Task.TYPE_DAILY: {
                dailyCheckUseCase.observable(new DailyCheckUseCase.RequestValues(user, event.Task, !event.Task.getCompleted()))
                        .subscribe(this::displayTaskScoringResponse, error -> {
                        });
            }
            break;
            case Task.TYPE_TODO: {
                todoCheckUseCase.observable(new TodoCheckUseCase.RequestValues(user, event.Task, !event.Task.getCompleted()))
                        .subscribe(this::displayTaskScoringResponse, error -> {
                        });
            }
            break;
        }
    }

    @Subscribe
    public void onEvent(ChecklistCheckedCommand event) {
        checklistCheckUseCase.observable(new ChecklistCheckUseCase.RequestValues(event.task.getId(), event.item.getId()))
                .subscribe(task -> {}, error -> {
                });
    }

    @Subscribe
    public void onEvent(HabitScoreEvent event) {
        habitScoreUseCase.observable(new HabitScoreUseCase.RequestValues(user, event.habit, event.Up))
                .subscribe(this::displayTaskScoringResponse, error -> {
                });
    }

    private void checkMaintenance() {
        this.maintenanceService.getMaintenanceStatus()
                .compose(apiClient.configureApiCallObserver())
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

    @Subscribe
    public void onEvent(OpenFullProfileCommand cmd) {
        if (cmd.MemberId.equals("system"))
            return;

        Bundle bundle = new Bundle();
        bundle.putString("userId", cmd.MemberId);

        Intent intent = new Intent(this, FullProfileActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public boolean isAppBarExpanded() {
        return (appBar.getHeight() - appBar.getBottom()) == 0;
    }
}
