package com.habitrpg.android.habitica.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;

public class SkillMemberActivity extends BaseActivity {
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private PartyMemberRecyclerViewAdapter viewAdapter;

    @Inject
    public SocialRepository socialRepository;
    @Inject
    public UserRepository userRepository;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_skill_members;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadMemberList();
    }

    private void loadMemberList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewAdapter = new PartyMemberRecyclerViewAdapter(null, true, this);
        viewAdapter.getUserClickedEvents().subscribe(userId -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("member_id", userId);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }, RxErrorHandler.handleEmptyError());
        recyclerView.setAdapter(viewAdapter);

        userRepository.getUser()
                .first()
                .flatMap(user -> socialRepository.getGroupMembers(user.getParty().id))
                .subscribe(viewAdapter::updateData, RxErrorHandler.handleEmptyError());
    }
}
