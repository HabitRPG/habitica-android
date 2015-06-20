package com.habitrpg.android.habitica.ui.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ui.fragments.CardFragment;
import com.habitrpg.android.habitica.ui.fragments.DailyFragment;
import com.habitrpg.android.habitica.ui.fragments.HabitFragment;
import com.habitrpg.android.habitica.ui.fragments.RewardFragment;
import com.habitrpg.android.habitica.ui.fragments.ToDoFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;

import java.util.List;

public class HabitItemAdapter extends FragmentPagerAdapter {
    private SparseArray<CardFragment> fragments = new SparseArray<CardFragment>();
    List<HabitItem> items;

    private static final String TAG = "MainActivity";

    public HabitItemAdapter(FragmentManager fm) {
        super(fm);
        Log.v(TAG + "_FP", "Reinstanciating items");
        CardFragment h = new HabitFragment();
        CardFragment d = new DailyFragment();
        CardFragment t = new ToDoFragment();
        CardFragment r = new RewardFragment();
        fragments.put(0, h);
        fragments.put(1, d);
        fragments.put(2, t);
        fragments.put(3, r);
    }

    public int getCount() {
        return 4;
    }

    @Override
    public CardFragment getItem(int position) {
        Log.v(TAG + "_FP", "instantiating fragment " + position);
        return fragments.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CardFragment fragment = (CardFragment) super.instantiateItem(container, position);
        Log.v(TAG + "_FP", "adding fragment" + position);
        fragments.put(position, fragment);
        if (items != null) {
            fragment.onChange(items);
            //fragment.onTagFilter(selectedTags);
        }
        return fragment;
    }
        /*@Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        	Log.v(TAG + "_FragmentPager", "removing fragment" + position);
            fragments.remove(position);
            super.destroyItem(container, position, object);
        }*/


    public void notifyFragments(List<HabitItem> items) {
        this.items = items;
        for (int i = 0; i <= 3; i++) { // f : fragments) {
            CardFragment f = fragments.get(i);
            if (f != null) {
                f.onChange(items);
                //f.onTagFilter(selectedTags);
            } else {
                Log.w(TAG + "_Notify", "no fragment " + i);
            }
            this.notifyDataSetChanged();
        }
    }

    public void filterFragments(List<String> tags) {
        for (int i = 0; i <= 3; i++) { // f : fragments) {
            CardFragment f = fragments.get(i);
            if (f != null)
                f.onTagFilter(tags);
            else
                Log.w(TAG + "_Filters", "no fragment " + i);
            this.notifyDataSetChanged();
        }

    }
}