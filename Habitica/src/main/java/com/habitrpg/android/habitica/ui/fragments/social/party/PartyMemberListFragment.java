package com.habitrpg.android.habitica.ui.fragments.social.party;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Negue on 15.09.2015.
 */
public class PartyMemberListFragment extends BaseFragment {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private Context ctx;
    private List<HabitRPGUser> members;
    private PartyMemberRecyclerViewAdapter viewAdapter;
    private View view;

    public void configure(Context ctx, List<HabitRPGUser> members) {
        this.ctx = ctx;
        this.members = members;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_party_memberlist, container, false);

        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        viewAdapter = new PartyMemberRecyclerViewAdapter();
        viewAdapter.context = this.ctx;
        mRecyclerView.setAdapter(viewAdapter);

        if (members != null) {
            setMemberList(members);
        }
    }

    public void setMemberList(List<HabitRPGUser> members) {
        this.members = members;
        viewAdapter.setMemberList(members);
    }

}
