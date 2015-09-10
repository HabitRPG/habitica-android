package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

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


    public static class ViewHolder implements View.OnClickListener {

        View view;

        @InjectView(R.id.editText)
        EditText editText;

        @InjectView(R.id.btnAdd)
        Button btnAdd;

        private ViewHolder(View view) {
            this.view = view;
            ButterKnife.inject(this, view);

            ViewHelper.SetBackgroundTint(btnAdd, view.getResources().getColor(R.color.brand));

            btnAdd.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(editText.getText().equals(""))
                return;

            EventBus.getDefault().post(new CreateTagCommand(editText.getText().toString()));

            editText.setText("");
        }
    }
}
