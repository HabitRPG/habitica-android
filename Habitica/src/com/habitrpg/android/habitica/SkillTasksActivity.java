package com.habitrpg.android.habitica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.SkillTasksRecyclerViewFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillTasksActivity extends AppCompatActivity {
    @Bind(R.id.viewpager)
    public ViewPager viewPager;

    @Bind(R.id.tab_layout)
    public TabLayout tabLayout;

    Map<Integer, SkillTasksRecyclerViewFragment> ViewFragmentsDictionary = new HashMap<>();
    protected HabitRPGUser user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_tasks);

        ButterKnife.bind(this);

        loadTaskLists();
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                SkillTasksRecyclerViewFragment fragment;

                switch (position) {
                    case 0:
                        fragment = SkillTasksRecyclerViewFragment.newInstance(new SkillTasksRecyclerViewAdapter(Task.TYPE_HABIT, SkillTasksActivity.this), Task.TYPE_HABIT);
                        break;
                    case 1:
                        fragment = SkillTasksRecyclerViewFragment.newInstance(new SkillTasksRecyclerViewAdapter(Task.TYPE_DAILY, SkillTasksActivity.this), Task.TYPE_DAILY);

                        break;
                    default:
                        fragment = SkillTasksRecyclerViewFragment.newInstance(new SkillTasksRecyclerViewAdapter(Task.TYPE_TODO, SkillTasksActivity.this), Task.TYPE_TODO);
                }

                ViewFragmentsDictionary.put(position, fragment);

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
