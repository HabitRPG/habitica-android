package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.menu.DividerItemDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublicGuildsFragment extends BaseMainFragment implements SearchView.OnQueryTextListener {

    List<String> memberGuildIDs;
    List<Group> guilds;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private View view;
    private PublicGuildsRecyclerViewAdapter viewAdapter;
    private SearchView guildSearchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_guild_recyclerview, container, false);

            unbinder = ButterKnife.bind(this, view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            viewAdapter = new PublicGuildsRecyclerViewAdapter();
            viewAdapter.setMemberGuildIDs(this.memberGuildIDs);
            viewAdapter.apiClient = this.apiClient;
            recyclerView.setAdapter(viewAdapter);
            if (this.guilds != null) {
                this.viewAdapter.setPublicGuildList(this.guilds);
            }
        }
        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.fetchGuilds();
    }

    private void fetchGuilds() {
        if (this.apiClient != null) {
            this.apiClient.listGroups("publicGuilds")

                    .subscribe(groups -> {
                        PublicGuildsFragment.this.guilds = groups;
                        if (PublicGuildsFragment.this.viewAdapter != null) {
                            PublicGuildsFragment.this.viewAdapter.setPublicGuildList(groups);
                        }
                    }, throwable -> {
                    });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_public_guild, menu);

        MenuItem searchItem = menu.findItem(R.id.action_guild_search);
        guildSearchView = (SearchView) searchItem.getActionView();
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) guildSearchView.findViewById(R.id.search_src_text);
        theTextArea.setHintTextColor(ContextCompat.getColor(this.activity, R.color.white));
        guildSearchView.setQueryHint(getString(R.string.guild_search_hint));
        guildSearchView.setOnQueryTextListener(this);

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        viewAdapter.getFilter().filter(s);
        UiUtils.dismissKeyboard(this.activity);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        viewAdapter.getFilter().filter(s);
        return true;
    }

    @Override
    public String customTitle() {
        return getString(R.string.public_guilds);
    }
}
