package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.habitrpg.android.habitica.R;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;

/**
 * Created by Negue on 18.06.2015.
 */
public class EditTextDrawer extends BaseDrawerItem<EditTextDrawer> {
    @Override
    public String getType() {
        return "EDIT_TEXT_DRAWER";
    }

    @Override
    public int getLayoutRes() {
        return R.layout.edit_text_drawer_item;
    }

    @Override
    public View convertView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        Context ctx = parent.getContext();

        //get the viewHolder
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(getLayoutRes(), parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }


    private static class ViewHolder {
        private View view;
        private EditText editText;

        private ViewHolder(View view) {
            this.view = view;
            this.editText = (EditText) view.findViewById(R.id.editText);
        }
    }
}
