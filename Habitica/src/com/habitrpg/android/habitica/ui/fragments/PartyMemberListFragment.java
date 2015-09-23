package com.habitrpg.android.habitica.ui.fragments;

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
import com.habitrpg.android.habitica.ui.adapter.PartyMemberRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.models.Group;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Negue on 15.09.2015.
 */
public class PartyMemberListFragment extends Fragment {

    private Context ctx;
    private ApiService apiService;

    private PartyMemberRecyclerViewAdapter viewAdapter;

    public PartyMemberListFragment(Context ctx, ApiService apiService){
        this.ctx = ctx;

        this.apiService = apiService;

        viewAdapter = new PartyMemberRecyclerViewAdapter();
    }

    private View view;

    @InjectView(R.id.recyclerView)
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

        ButterKnife.inject(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        mRecyclerView.setAdapter(viewAdapter);

        // Get the full group data
        apiService.getGroup("party", new Callback<Group>() {
            @Override
            public void success(Group group, Response response) {
                viewAdapter.setMemberList(group.members);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


    }
}
