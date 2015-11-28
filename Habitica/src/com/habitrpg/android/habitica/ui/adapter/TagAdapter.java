package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mickael Goubin
 */
public class TagAdapter extends BaseAdapter {


    private Context mContext;
    private Map<String, String> mTags;
    private String[] mKeySet;

    public TagAdapter(Context c) {
        this(c, new HashMap<String, String>());
    }

    public TagAdapter(Context c, Map<String, String> tags) {
        this.mContext = c;
        this.mTags = tags;
        if (this.mTags != null)
            this.mKeySet = this.mTags.keySet().toArray(new String[mTags.size()]);
        else {
            mKeySet = new String[0];
        }
    }

    @Override
    public int getCount() {
        return mTags.size();
    }

    @Override
    public String getItem(int i) {

        return mTags.get(mKeySet[i]);
    }

    @Override
    public boolean isEnabled(int position) {

        return super.isEnabled(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public String getTagId(int i) {
        return mKeySet[i];
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        String tag = getItem(i);
        TextView tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.drawer_list_item, parent, false);
        tv.setText(tag);
        convertView = tv;
        return convertView;
    }

    public void updateTags(Map<String, String> tags) {
        this.mTags.clear();
        if (tags != null)
            this.mTags.putAll(tags);
        this.mKeySet = this.mTags.keySet().toArray(new String[mTags.size()]);
    }

    //TODO: Nooooooooooo! this is kinda ugly to do it like that.
    public void updateTags(List<Tag> tags) {
        Map<String, String> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getId(), tag.getName());
        }
        this.updateTags(map);


    }
}
