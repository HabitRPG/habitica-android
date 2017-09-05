package com.habitrpg.android.habitica.ui.adapter.social.challenges;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.underscore.$;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.social.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengesFilterRecyclerViewAdapter extends RecyclerView.Adapter<ChallengesFilterRecyclerViewAdapter.ChallengeViewHolder> {


    private List<Group> entries;
    private List<ChallengesFilterRecyclerViewAdapter.ChallengeViewHolder> holderList;

    public ChallengesFilterRecyclerViewAdapter(Collection<Group> entries) {

        this.entries = new ArrayList<>(entries);
        this.holderList = new ArrayList<>();
    }

    @Override
    public ChallengesFilterRecyclerViewAdapter.ChallengeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_challenge_filter_group_item, parent, false);

        ChallengeViewHolder challengeViewHolder = new ChallengeViewHolder(view);
        holderList.add(challengeViewHolder);

        return challengeViewHolder;
    }

    @Override
    public void onBindViewHolder(ChallengesFilterRecyclerViewAdapter.ChallengeViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void deSelectAll(){
        for (ChallengeViewHolder h : holderList) {
            h.checkbox.setChecked(false);
        }
    }

    public void selectAll(){
        for (ChallengeViewHolder h : holderList) {
            h.checkbox.setChecked(true);
        }
    }

    public void selectAll(List<Group> groupsToCheck){
        for (ChallengeViewHolder h : holderList) {
            h.checkbox.setChecked($.find(groupsToCheck, g -> h.group.id.equals(g.id)).isPresent());
        }
    }
    public List<Group> getCheckedEntries(){
        ArrayList<Group> result = new ArrayList<>();

        for (ChallengeViewHolder h : holderList) {
            if(h.checkbox.isChecked()){
                result.add(h.group);
            }
        }

        return result;
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.challenge_filter_group_checkbox)
        CheckBox checkbox;

        public Group group;

        public ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(Group group) {
            this.group = group;

            checkbox.setText(group.name);
        }
    }
}