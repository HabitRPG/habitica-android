package com.habitrpg.android.habitica.ui.menu;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.ToggledEditTagsEvent;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.AbstractDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.model.interfaces.Typefaceable;
import com.mikepenz.materialize.util.UIUtils;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jjbillings on 8/12/16.
 */
public class EditTagsSectionDrawer extends AbstractDrawerItem<EditTagsSectionDrawer, EditTagsSectionDrawer.ViewHolder> implements Nameable<EditTagsSectionDrawer>, Typefaceable<EditTagsSectionDrawer> {

    private StringHolder name;
    private boolean divider = true;
    private boolean editing = false;

    private ColorHolder textColor;

    private Typeface typeface = null;

    public EditTagsSectionDrawer withName(StringHolder name) {
        this.name = name;
        return this;
    }

    public EditTagsSectionDrawer withName(String name) {
        this.name = new StringHolder(name);
        return this;
    }

    public EditTagsSectionDrawer withName(@StringRes int nameRes) {
        this.name = new StringHolder(nameRes);
        return this;
    }

    public EditTagsSectionDrawer withEditing(boolean edit) {
        this.editing = edit;
        return this;
    }

    public EditTagsSectionDrawer withDivider(boolean divider) {
        this.divider = divider;
        return this;
    }

    public EditTagsSectionDrawer withTextColor(int textColor) {
        this.textColor = ColorHolder.fromColor(textColor);
        return this;
    }

    public EditTagsSectionDrawer withTextColorRes(int textColorRes) {
        this.textColor = ColorHolder.fromColorRes(textColorRes);
        return this;
    }

    public EditTagsSectionDrawer withTypeface(Typeface typeface) {
        this.typeface = typeface;
        return this;
    }

    public boolean hasDivider() {
        return divider;
    }

    public ColorHolder getTextColor() {
        return textColor;
    }

    public StringHolder getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public int getType() {
        return R.id.material_drawer_item_section;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.edit_tags_section_drawer_item;
    }

    @Override
    public Typeface getTypeface() {
        return typeface;
    }

    @Override
    public void bindView(ViewHolder viewHolder) {
        Context ctx = viewHolder.itemView.getContext();

        //set the identifier from the drawerItem here. It can be used to run tests
        viewHolder.itemView.setId(hashCode());

        //define this item to be not clickable nor enabled
        viewHolder.view.setClickable(false);
        viewHolder.view.setEnabled(false);

        //define the text color
        viewHolder.name.setTextColor(ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_secondary_text, com.mikepenz.materialdrawer.R.color.material_drawer_secondary_text));

        viewHolder.editing = this.editing;

        if (this.editing) {
            viewHolder.btnEdit.setText(ctx.getString(R.string.edit_tag_btn_done));
        } else {
            viewHolder.btnEdit.setText(ctx.getString(R.string.edit_tag_btn_edit));
        }

        ViewHelper.SetBackgroundTint(viewHolder.btnEdit, ContextCompat.getColor(ctx, R.color.brand));

        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.name);

        //define the typeface for our textViews
        if (getTypeface() != null) {
            viewHolder.name.setTypeface(getTypeface());
        }

        //hide the divider if we do not need one
        if (this.hasDivider()) {
            viewHolder.divider.setVisibility(View.VISIBLE);
        } else {
            viewHolder.divider.setVisibility(View.GONE);
        }

        //set the color for the divider
        viewHolder.divider.setBackgroundColor(UIUtils.getThemeColorFromAttrOrRes(ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_divider, com.mikepenz.materialdrawer.R.color.material_drawer_divider));

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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private View divider;
        private TextView name;
        private Button btnEdit;

        private boolean editing;

        private ViewHolder(View view) {
            super(view);
            this.view = view;
            this.divider = view.findViewById(R.id.material_drawer_divider);
            this.name = (TextView) view.findViewById(R.id.drawer_section_name);

            this.btnEdit = (Button) view.findViewById(R.id.btnEdit);
            this.btnEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ToggledEditTagsEvent editTagsEvent = new ToggledEditTagsEvent(!this.editing);
            EventBus.getDefault().post(editTagsEvent);
        }
    }
}
