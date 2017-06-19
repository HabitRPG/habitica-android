package com.habitrpg.android.habitica.ui.fragments.social;

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

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.menu.DividerItemDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublicGuildsFragment extends BaseMainFragment implements SearchView.OnQueryTextListener {

    @Inject
    SocialRepository socialRepository;

    List<String> memberGuildIDs;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private View view;
    private PublicGuildsRecyclerViewAdapter viewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            unbinder = ButterKnife.bind(this, view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            viewAdapter = new PublicGuildsRecyclerViewAdapter(null, true);
            viewAdapter.setMemberGuildIDs(this.memberGuildIDs);
            viewAdapter.apiClient = this.apiClient;
            recyclerView.setAdapter(viewAdapter);
            this.fetchGuilds();
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
    }

    @Override
    public void onDestroy() {
        socialRepository.close();
        super.onDestroy();
    }

    private void fetchGuilds() {
        if (this.socialRepository != null) {
            this.socialRepository.getPublicGuilds()
                    .first()
                    .subscribe(groups -> {
                        if (PublicGuildsFragment.this.viewAdapter != null) {
                            PublicGuildsFragment.this.viewAdapter.updateData(groups);
                        }
                    }, throwable -> {
                    });
            this.socialRepository.retrieveGroups("publicGuilds").subscribe(groups -> {}, RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_public_guild, menu);

        MenuItem searchItem = menu.findItem(R.id.action_guild_search);
        SearchView guildSearchView = (SearchView) searchItem.getActionView();
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) guildSearchView.findViewById(R.id.search_src_text);
        theTextArea.setHintTextColor(ContextCompat.getColor(getContext(), R.color.white));
        guildSearchView.setQueryHint(getString(R.string.guild_search_hint));
        guildSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        viewAdapter.getFilter().filter(s);
        if (this.activity != null) {
            UiUtils.dismissKeyboard(this.activity);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        viewAdapter.getFilter().filter(s);
        return true;
    }

    @Override
    public String customTitle() {
        if (isAdded()) {
            return getString(R.string.public_guilds);
        } else {
            return "";
        }
    }
}
