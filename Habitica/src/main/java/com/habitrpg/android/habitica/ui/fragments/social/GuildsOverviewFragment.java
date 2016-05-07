package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GuildsOverviewFragment extends BaseMainFragment implements Callback<ArrayList<Group>>, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.my_guilds_listview)
    LinearLayout  guildsListView;

    @BindView(R.id.publicGuildsButton)
    Button publicGuildsButton;

    @BindView(R.id.chat_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private List<Group> guilds;
    private ArrayList<String> guildIDs;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.fetchGuilds();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_guilds_overview, container, false);
        ButterKnife.bind(this, v);
        swipeRefreshLayout.setOnRefreshListener(this);
        this.publicGuildsButton.setOnClickListener(this);
        if (this.guilds != null) {
            this.setGuildsOnListView();
        }
        return v;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        fetchGuilds();
    }

    private void fetchGuilds() {
        if (this.mAPIHelper != null && this.mAPIHelper.apiService != null) {
            this.mAPIHelper.apiService.listGroups("guilds", this);
        }
    }

    private void setGuildsOnListView() {
        if (this.guildsListView == null) {
            return;
        }
        this.guildIDs = new ArrayList<>();
        this.guildsListView.removeAllViewsInLayout();
        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Group guild : this.guilds) {
            TextView entry = (TextView) inflater.inflate(R.layout.plain_list_item, this.guildsListView, false);
            entry.setText(guild.name);
            entry.setOnClickListener(this);
            this.guildsListView.addView(entry);
            this.guildIDs.add(guild.id);
        }
    }

    @Override
    public void success(ArrayList<Group> groups, Response response) {
        this.guilds = groups;
        this.setGuildsOnListView();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void failure(RetrofitError error) {

    }

    @Override
    public void onClick(View v) {
        if (v == this.publicGuildsButton) {
            PublicGuildsFragment publicGuildsFragment = new PublicGuildsFragment();
            publicGuildsFragment.memberGuildIDs = this.guildIDs;
            this.activity.displayFragment(publicGuildsFragment);
        } else {
            Integer guildIndex = ((ViewGroup)v.getParent()).indexOfChild(v);
            GuildFragment guildFragment = new GuildFragment();
            guildFragment.setGuild(this.guilds.get(guildIndex));
            guildFragment.isMember = true;
            this.activity.displayFragment(guildFragment);
        }
    }
}
