package com.habitrpg.android.habitica;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskCreationCallback;
import com.habitrpg.android.habitica.callbacks.TaskDeletionCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.TaskUpdateCallback;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.EditTextDrawer;
import com.habitrpg.android.habitica.ui.adapter.HabitItemRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.RecyclerViewFragment;
import com.instabug.wrapper.support.activity.InstabugAppCompatActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends InstabugAppCompatActivity implements OnTaskCreationListener, HabitRPGUserCallback.OnUserReceived,
        TaskScoringCallback.OnTaskScored, TaskCreationCallback.OnHabitCreated, TaskUpdateCallback.OnHabitUpdated, TaskDeletionCallback.OnTaskDeleted, Callback<Void> {
    static final int ABOUT = 12;

    //region View Elements
    @InjectView(R.id.materialViewPager)
    MaterialViewPager materialViewPager;

    Toolbar toolbar;
    Drawer drawer;

    Drawer filterDrawer;
    //endregion

    Map<Integer, RecyclerViewFragment> ViewFragmentsDictionary = new HashMap<Integer, RecyclerViewFragment>();

    List<HabitItem> TaskList = new ArrayList<HabitItem>();

    private HostConfig hostConfig;
    APIHelper mAPIHelper;

    android.support.v4.view.ViewPager viewPager;

    // just to test the view
    public HabitRPGUser User = null;

    AvatarWithBarsViewModel avatarInHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inject Controls
        ButterKnife.inject(this);

        this.hostConfig = PrefsActivity.fromContext(this);
        if(hostConfig==null|| hostConfig.getApi()==null || hostConfig.getApi().equals("") || hostConfig.getUser() == null ||hostConfig.getUser().equals("")) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        toolbar = materialViewPager.getToolbar();

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();

            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }

            toolbar.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tool_bar_top_padding), 0, 0);
        }

        materialViewPager.setBackgroundColor(getResources().getColor(R.color.white));

        View mPagerRootView = materialViewPager.getRootView();

        View avatarHeaderView = mPagerRootView.findViewById(R.id.avatar_with_bars_layout);

        avatarInHeader = new AvatarWithBarsViewModel(this, avatarHeaderView);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeaderDivider(false)
                .withAnimateDrawerItems(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Tasks"),

                        new SectionDrawerItem().withName("Social"),
                        new PrimaryDrawerItem().withName("Tavern"),
                        new PrimaryDrawerItem().withName("Party"),
                        new PrimaryDrawerItem().withName("Guilds"),
                        new PrimaryDrawerItem().withName("Challenges"),


                        new SectionDrawerItem().withName("Inventory"),

                        new PrimaryDrawerItem().withName("Avatar"),
                        new PrimaryDrawerItem().withName("Equipment"),
                        new PrimaryDrawerItem().withName("Stable"),

                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("News"),
                        new SecondaryDrawerItem().withName("Settings"),
                        new SecondaryDrawerItem().withName("About").withIdentifier(ABOUT)

                )
                .withStickyFooterDivider(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        switch (drawerItem.getIdentifier()) {
                            case ABOUT:
                                startActivity(new Intent(MainActivity.this, AboutActivity.class));

                                return false;
                        }

                        return true;
                    }
                })

                .build();

        final android.content.Context context = getApplicationContext();

        filterDrawer = new DrawerBuilder()
                .withActivity(this)
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                        Toast toast = Toast.makeText(context, "Long Pressed", Toast.LENGTH_LONG);

                        toast.show();

                        return true;
                    }
                })
                .withDrawerGravity(Gravity.RIGHT)
                .append(drawer);


        viewPager = materialViewPager.getViewPager();
        viewPager.setOffscreenPageLimit(6);




        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {

                Log.d("PageSelected", "P=" + position);

                RecyclerViewFragment fragment = ViewFragmentsDictionary.get(position);

                if (fragment == null || fragment.mRecyclerView == null)
                    return;

                // fragment.mRecyclerView.smoothScrollToPosition(r.nextInt(fragment.mRecyclerView.getAdapter().getItemCount()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        materialViewPager.getViewPager().setCurrentItem(0);


        List<Habit> users = new Select().from(Habit.class).queryList();
        Log.d("MainActivity", users.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.mAPIHelper = new APIHelper(this, hostConfig);

        mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
    }

    public void FillTasks()
    {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            int oldPosition = -1;

            @Override
            public Fragment getItem(int position) {
                int layoutOfType;
                RecyclerViewFragment fragment;

                String fragmentkey = "Recycler$" + position;

                switch (position) {
                    case 0:
                        layoutOfType = R.layout.habit_item_card;
                        fragment = RecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(User.getHabits(), layoutOfType, HabitItemRecyclerViewAdapter.HabitViewHolder.class), fragmentkey);
                        break;
                    case 1:
                        layoutOfType = R.layout.daily_item_card;
                        fragment = RecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(User.getDailys(), layoutOfType, HabitItemRecyclerViewAdapter.DailyViewHolder.class), fragmentkey);
                        break;
                    case 3:
                        layoutOfType = R.layout.reward_item_card;
                        fragment = RecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(User.getRewards(), layoutOfType, HabitItemRecyclerViewAdapter.RewardViewHolder.class), fragmentkey);
                        break;
                    default:
                        layoutOfType = R.layout.todo_item_card;
                        fragment = RecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(User.getTodos(), layoutOfType, HabitItemRecyclerViewAdapter.TodoViewHolder.class), fragmentkey);
                }

                // ViewFragmentsDictionary.put(position, fragment);

                return fragment;
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Habits";
                    case 1:
                        return "Dailies";
                    case 2:
                        return "Todos";
                    case 3:
                        return "Rewards";
                }
                return "";
            }


        });

        materialViewPager.getPagerTitleStrip().setViewPager(viewPager);
    }

    public void FillTagFilterDrawer() {
        filterDrawer.removeAllItems();
        filterDrawer.addItems(
                new SectionDrawerItem().withName("Filter by Tag"),
                new EditTextDrawer()

        );

        for (Tag t : User.getTags()) {
            filterDrawer.addItem(
                    new PrimaryDrawerItem().withName(t.getName()).withBadge("" + CountTagUsedInTasks(t.getId()))
            );
        }


    }

    /**
     * Anyone a better solution to count the Tag?
     */
    public int CountTagUsedInTasks(String tagId) {
        int count = 0;

        for (HabitItem task : TaskList) {
            if (task.getTags().contains(tagId))
                count++;
        }

        return count;
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                filterDrawer.openDrawer();

                return true;
            case R.id.action_toggle_sleep:
                mAPIHelper.toggleSleep(this);

                User.getPreferences().setSleep(!User.getPreferences().getSleep());

                updateUserAvatars();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUserAvatars()
    {
        avatarInHeader.UpdateData(User);
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        TaskList.clear();

        User = user;

        if(user == null)
            return;

        toolbar.setTitle(User.getProfile().getName() + " - Lv" + User.getStats().getLvl());

        TaskList.addAll(User.getHabits());
        TaskList.addAll(User.getDailys());
        TaskList.addAll(User.getTodos());
        TaskList.addAll(User.getRewards());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FillTasks();

                updateUserAvatars();

                android.support.v7.app.ActionBarDrawerToggle actionBarDrawerToggle = drawer.getActionBarDrawerToggle();

                if (actionBarDrawerToggle != null) {
                    actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
                }


                FillTagFilterDrawer();
            }
        }, 100);
    }

    @Override
    public void onUserFail() {

    }

    @Override
    public void success(Void aVoid, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }

    @Override
    public void onTaskCreated(HabitItem habit) {

    }

    @Override
    public void onTaskCreationFail() {

    }

    @Override
    public void onTaskUpdated(HabitItem habit) {

    }

    @Override
    public void onTaskUpdateFail() {

    }

    @Override
    public void onTaskCreation(HabitItem task, boolean editMode) {

    }

    @Override
    public void onTaskCreationFail(String message) {

    }

    @Override
    public void onTaskDeleted(HabitItem deleted) {

    }

    @Override
    public void onTaskDeletionFail() {

    }

    @Override
    public void onTaskDataReceived(TaskDirectionData data) {

    }

    @Override
    public void onTaskScoringFailed() {

    }
}