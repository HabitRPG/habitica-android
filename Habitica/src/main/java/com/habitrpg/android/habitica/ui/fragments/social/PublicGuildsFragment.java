package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublicGuildsFragment extends BaseMainFragment {

    List<String> memberGuildIDs;
    List <Group> guilds;

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

            ButterKnife.bind(this, view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            viewAdapter = new PublicGuildsRecyclerViewAdapter();
            viewAdapter.setMemberGuildIDs(this.memberGuildIDs);
            viewAdapter.apiHelper = this.apiHelper;
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
        if (this.apiHelper != null) {
            this.apiHelper.apiService.listGroups("publicGuilds")
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(groups -> {
                PublicGuildsFragment.this.guilds = groups;
                if (PublicGuildsFragment.this.viewAdapter!= null) {
                    PublicGuildsFragment.this.viewAdapter.setPublicGuildList(groups);
                }
            }, throwable -> {});
        }
    }
}
