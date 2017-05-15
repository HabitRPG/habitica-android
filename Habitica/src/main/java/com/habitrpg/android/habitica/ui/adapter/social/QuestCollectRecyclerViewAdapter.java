package com.habitrpg.android.habitica.ui.adapter.social;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.QuestProgress;
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuestCollectRecyclerViewAdapter extends RecyclerView.Adapter<QuestCollectRecyclerViewAdapter.QuestCollectViewHolder> {

    private QuestProgress progress;
    private QuestContent quest;

    public void setQuestProgress(QuestProgress progress) {
        this.progress = progress;
        this.notifyDataSetChanged();
    }

    public void setQuestContent(QuestContent quest) {
        this.quest = quest;
        this.notifyDataSetChanged();
    }

    @Override
    public QuestCollectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_quest_collect, parent, false);

        return new QuestCollectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QuestCollectViewHolder holder, int position) {
        holder.bind(progress.collect.get(position));
    }

    @Override
    public int getItemCount() {
        return progress != null && progress.collect != null ? progress.collect.size() : 0;
    }

    class QuestCollectViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        SimpleDraweeView image;

        @BindView(R.id.name)
        TextView name;

        @BindView(R.id.count)
        TextView count;

        View view;

        public QuestCollectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.view = itemView;
        }

        public void bind(QuestProgressCollect collectProgress) {
            image.setImageURI(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + "quest_" + quest.getKey() + "_" + collectProgress.key + ".png"));
            if (quest != null) {
                QuestCollect collect = quest.getCollectWithKey(collectProgress.key);
                if (collect != null) {
                    name.setText(collect.text);
                    count.setText(collectProgress.count + " / " + collect.count);
                }
            }
        }
    }
}
