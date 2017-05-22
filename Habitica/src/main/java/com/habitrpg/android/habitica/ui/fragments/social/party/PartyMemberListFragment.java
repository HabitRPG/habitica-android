package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by Negue on 15.09.2015.
 */
public class PartyMemberListFragment extends BaseFragment {

    @Inject
    SocialRepository socialRepository;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private PartyMemberRecyclerViewAdapter adapter;
    private View view;
    private String partyId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        }
        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PartyMemberRecyclerViewAdapter(null, true, getContext(), true);
        recyclerView.setAdapter(adapter);

        getUsers();
    }

    public void setPartyId(String id) {
        this.partyId = id;
        getUsers();
    }

    private void getUsers() {
        if (partyId == null) {
            return;
        }
        socialRepository.getGroupMembers(partyId).first().subscribe(users -> {
            if (adapter != null) {
                adapter.updateData(users);
            }
        }, RxErrorHandler.handleEmptyError());
    }
}
