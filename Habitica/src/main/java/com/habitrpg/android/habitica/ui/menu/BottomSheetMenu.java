package com.habitrpg.android.habitica.ui.menu;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class BottomSheetMenu extends BottomSheetDialog implements View.OnClickListener {

    private LayoutInflater inflater;
    private Context context;
    private LinearLayout contentView;
    private BottomSheetMenuSelectionRunnable runnable;

    public BottomSheetMenu(Context context) {
        super(context);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contentView = (LinearLayout)inflater.inflate(R.layout.menu_bottom_sheet, null);
        this.setContentView(this.contentView);
    }

    public void setSelectionRunnable(BottomSheetMenuSelectionRunnable runnable) {
        this.runnable = runnable;
    }

    public void addMenuItems(BottomSheetMenuItem... menuItems) {
        for (BottomSheetMenuItem menuItem : menuItems) {
            this.addMenuItem(menuItem);
        }
    }

    public void addMenuItem(BottomSheetMenuItem menuItem) {
        View item = menuItem.inflate(this.context, this.inflater, this.contentView);
        item.setOnClickListener(this);
        this.contentView.addView(item);
    }

    public void removeMenuItem(Integer index) {
        this.contentView.removeViewAt(index);
    }

    @Override
    public void onClick(View v) {
        if (this.runnable != null) {
            Integer index = this.contentView.indexOfChild(v);
            if (index != -1) {
                this.runnable.selectedItemAt(index);
                this.dismiss();
            }
        }
    }
}
