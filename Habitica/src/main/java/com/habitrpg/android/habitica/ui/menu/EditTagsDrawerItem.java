package com.habitrpg.android.habitica.ui.menu;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EditTagCommand;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.BasePrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jjbillings on 8/12/16.
 */
public class EditTagsDrawerItem extends BasePrimaryDrawerItem<EditTagsDrawerItem, EditTagsDrawerItem.ViewHolder> {

    private StringHolder name;

    public EditTagsDrawerItem withName(StringHolder name) {
        this.name = name;
        return this;
    }

    public EditTagsDrawerItem withName(String name) {
        this.name = new StringHolder(name);
        return this;
    }

    public EditTagsDrawerItem withName(@StringRes int nameRes) {
        this.name = new StringHolder(nameRes);
        return this;
    }

    public StringHolder getName() {
        return name;
    }


    @Override
    public int getType() {
        return R.id.material_drawer_item_primary_toggle;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.edit_tags_drawer_item;
    }

    @Override
    public void bindView(final ViewHolder viewHolder) {
        Context ctx = viewHolder.itemView.getContext();

        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.tagTextView);
        viewHolder.tagTextView.setTextColor(ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_primary_text, com.mikepenz.materialdrawer.R.color.material_drawer_primary_text));

        //Setup the Delete Button
        ViewHelper.SetBackgroundTint(viewHolder.btnEdit, ContextCompat.getColor(ctx, R.color.brand));
        viewHolder.btnEdit.setEnabled(true);

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
        private TextView tagTextView;
        private Button btnEdit;
        private Tag tag;

        private ViewHolder(View view) {
            super(view);
            tagTextView = (TextView)view.findViewById(R.id.tagTextView);

            btnEdit = (Button)view.findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(this.tag != null) {
                EventBus.getDefault().post(new EditTagCommand(this.tag));
                //EventBus.getDefault().post(new DeleteTagCommand(this.tag));
            }
        }

    }


}
