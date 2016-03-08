package com.habitrpg.android.habitica.ui.fragments.social;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PublicGuildsFragment extends BaseMainFragment implements Callback<ArrayList<Group>> {

    ArrayList<String> memberGuildIDs;
    ArrayList <Group> guilds;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    private View view;
    private PublicGuildsRecyclerViewAdapter viewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            ButterKnife.bind(this, view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
            viewAdapter = new PublicGuildsRecyclerViewAdapter();
            viewAdapter.setMemberGuildIDs(this.memberGuildIDs);
            viewAdapter.apiHelper = this.mAPIHelper;
            recyclerView.setAdapter(viewAdapter);
            if (this.guilds != null) {
                this.viewAdapter.setPublicGuildList(this.guilds);
            }
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.fetchGuilds();
    }

    private void fetchGuilds() {
        this.mAPIHelper.apiService.listGroups("public", this);
    }

    @Override
    public void success(ArrayList<Group> groups, Response response) {
        this.guilds = groups;
        if (this.viewAdapter!= null) {
            this.viewAdapter.setPublicGuildList(groups);
        }
    }

    @Override
    public void failure(RetrofitError error) {

    }
}
