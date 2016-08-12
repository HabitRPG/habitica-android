package com.habitrpg.android.habitica.ui.menu;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.DeleteTagCommand;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.BasePrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by jjbillings on 8/12/16.
 */
public class EditTagDrawerItem extends BasePrimaryDrawerItem<EditTagDrawerItem, EditTagDrawerItem.ViewHolder> {

    private StringHolder name;

    public EditTagDrawerItem withName(StringHolder name) {
        this.name = name;
        return this;
    }

    public EditTagDrawerItem withName(String name) {
        this.name = new StringHolder(name);
        return this;
    }

    public EditTagDrawerItem withName(@StringRes int nameRes) {
        this.name = new StringHolder(nameRes);
        return this;
    }

    public StringHolder getName() {
        return name;
    }


    @Override
    public int getType() {
        return R.id.material_drawer_item_secondary_switch;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.edit_tag_drawer_item;
    }

    @Override
    public void bindView(final ViewHolder viewHolder) {
        Context ctx = viewHolder.itemView.getContext();

        //define the text color
        viewHolder.editTagText.setTextColor(ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_secondary_text, com.mikepenz.materialdrawer.R.color.material_drawer_secondary_text));

        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.editTagText);

        //Setup the Delete Button
        ViewHelper.SetBackgroundTint(viewHolder.btnDelete, ContextCompat.getColor(ctx, R.color.worse_10));
        viewHolder.btnDelete.setEnabled(true);

        viewHolder.tag = (Tag)this.getTag();
        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView);
    }

    @Override
    public ViewHolderFactory<ViewHolder> getFactory() {
        return new ItemFactory();
    }

    public static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }

    public static class ViewHolder extends BaseViewHolder implements View.OnClickListener {
        private EditText editTagText;
        private Button btnDelete;
        private Tag tag;

        private ViewHolder(View view) {
            super(view);
            editTagText = (EditText)view.findViewById(R.id.editTagText);

            btnDelete = (Button)view.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(this.tag != null) {
                EventBus.getDefault().post(new DeleteTagCommand(this.tag.getId()));
            }
        }
    }


}
