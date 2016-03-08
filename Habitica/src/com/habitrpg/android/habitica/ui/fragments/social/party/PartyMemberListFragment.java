package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Negue on 15.09.2015.
 */
public class PartyMemberListFragment extends Fragment {

    private Context ctx;
    private ArrayList<HabitRPGUser> members;

    private PartyMemberRecyclerViewAdapter viewAdapter;

    public void configure(Context ctx, ArrayList<HabitRPGUser> members) {
        this.ctx = ctx;
        this.members = members;

    }

    private View view;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_party_memberlist, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        viewAdapter = new PartyMemberRecyclerViewAdapter();
        viewAdapter.context = this.ctx;
        mRecyclerView.setAdapter(viewAdapter);

        if (members != null) {
            setMemberList(members);
        }
    }

    public void setMemberList(ArrayList<HabitRPGUser> members) {
        this.members = members;
        viewAdapter.setMemberList(members);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);

    }

}
