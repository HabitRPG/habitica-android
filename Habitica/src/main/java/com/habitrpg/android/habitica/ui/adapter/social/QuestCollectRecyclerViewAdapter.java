package com.habitrpg.android.habitica.ui.adapter.social;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.QuestProgress;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuestCollectRecyclerViewAdapter extends RecyclerView.Adapter<QuestCollectRecyclerViewAdapter.QuestCollectViewHolder> {

    private ArrayList<String> collect = new ArrayList<>();

    private QuestProgress progress;
    private QuestContent quest;

    public void setQuestProgress(QuestProgress progress) {
        this.progress = progress;
        collect.clear();
        collect.addAll(progress.collect.keySet());
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
        holder.bind(collect.get(position));
    }

    @Override
    public int getItemCount() {
        return collect == null ? 0 : collect.size();
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

        public void bind(String key) {
            image.setImageURI(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + "quest_" + quest.getKey() + "_" + key + ".png"));
            if (quest.getCollect().get(key) != null) {
                name.setText(quest.getCollect().get(key).text);
                count.setText(progress.collect.get(key) + " / " + quest.getCollect().get(key).count);
            }
        }
    }
}
