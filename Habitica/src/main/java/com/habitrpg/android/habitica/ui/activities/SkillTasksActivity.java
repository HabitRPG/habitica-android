package com.habitrpg.android.habitica.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.skills.SkillTasksRecyclerViewFragment;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

public class SkillTasksActivity extends BaseActivity implements TaskClickActivity {

    @Inject
    TaskRepository taskRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.viewpager)
    public ViewPager viewPager;

    @BindView(R.id.tab_layout)
    public TabLayout tabLayout;
    SparseArray<SkillTasksRecyclerViewFragment> viewFragmentsDictionary = new SparseArray<>();

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_skill_tasks;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTaskLists();
    }

    @Override
    protected void onDestroy() {
        taskRepository.close();
        super.onDestroy();
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                SkillTasksRecyclerViewFragment fragment;

                switch (position) {
                    case 0:
                        fragment = SkillTasksRecyclerViewFragment.newInstance(new SkillTasksRecyclerViewAdapter(taskRepository, Task.TYPE_HABIT, SkillTasksActivity.this, userId), Task.TYPE_HABIT);
                        break;
                    case 1:
                        fragment = SkillTasksRecyclerViewFragment.newInstance(new SkillTasksRecyclerViewAdapter(taskRepository, Task.TYPE_DAILY, SkillTasksActivity.this, userId), Task.TYPE_DAILY);
                        break;
                    default:
                        fragment = SkillTasksRecyclerViewFragment.newInstance(new SkillTasksRecyclerViewAdapter(taskRepository, Task.TYPE_TODO, SkillTasksActivity.this, userId), Task.TYPE_TODO);
                }

                viewFragmentsDictionary.put(position, fragment);

                return fragment;
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.habits);
                    case 1:
                        return getString(R.string.dailies);
                    case 2:
                        return getString(R.string.todos);
                }
                return "";
            }
        });


        tabLayout.setupWithViewPager(viewPager);
    }

    public void taskSelected(String taskId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("task_id", taskId);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}

