package com.habitrpg.android.habitica.ui.viewHolders;

import com.habitrpg.android.habitica.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.label)
    TextView label;

    @BindView(R.id.purchaseSetButton)
    Button purchaseSetButton;

    Context context;

    public SectionViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        ButterKnife.bind(this, itemView);
        this.purchaseSetButton.setVisibility(View.GONE);
    }

    public void bind(String title) {
        try {
            Integer stringID = context.getResources().getIdentifier("section" + title, "string", context.getPackageName());
            this.label.setText(context.getString(stringID));
        } catch (Exception e) {
            this.label.setText(title);
        }
    }
}
