package com.habitrpg.android.habitica.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.mikepenz.materialdrawer.model.BasePrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.utils.ViewHolderFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by Negue on 18.06.2015.
 */
public class EditTextDrawer extends BasePrimaryDrawerItem<EditTextDrawer> {
    @Override
    public String getType() {
        return "EDIT_TEXT_DRAWER";
    }

    @Override
    public int getLayoutRes() {
        return R.layout.edit_text_drawer_item;
    }

    @Override
    public void bindView(RecyclerView.ViewHolder viewHolder) {
        final ViewHolder holder = (ViewHolder) viewHolder;
//        ((ViewHolder) viewHolder).btnAdd

        onPostBindView(this, holder.itemView);

    }


    @Override
    public ViewHolderFactory getFactory() {
        return new ItemFactory();
    }

    public static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder factory(View v) {
            return new ViewHolder(v);
        }
    }


    public static class ViewHolder extends BaseViewHolder implements View.OnClickListener {

        View view;

        @InjectView(R.id.editText)
        EditText editText;

        @InjectView(R.id.btnAdd)
        Button btnAdd;

        private ViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.inject(this, view);

            ViewHelper.SetBackgroundTint(btnAdd, view.getResources().getColor(R.color.brand));

            btnAdd.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (editText.getText().equals(""))
                return;

            EventBus.getDefault().post(new CreateTagCommand(editText.getText().toString()));

            editText.setText("");
        }
    }
}
