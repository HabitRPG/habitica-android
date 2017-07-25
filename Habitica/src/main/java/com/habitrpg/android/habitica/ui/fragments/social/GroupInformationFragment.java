package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.databinding.FragmentGroupInfoBinding;
import com.habitrpg.android.habitica.helpers.QrCodeManager;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.views.ValueBar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupInformationFragment extends BaseFragment {


    FragmentGroupInfoBinding viewBinding;

    @Inject
    ApiClient apiClient;
    @Inject
    UserRepository userRepository;

    @BindView(R.id.qrLayout)
    @Nullable
    LinearLayout qrLayout;

    @BindView(R.id.qrWrapper)
    @Nullable
    CardView qrWrapper;

    private View view;
    @Nullable
    private Group group;
    @Nullable
    private User user;
    private QuestContent quest;
    private ValueBar bossHpBar;
    private ValueBar bossRageBar;

    public GroupInformationFragment() {

    }

    public static GroupInformationFragment newInstance(@Nullable Group group, @Nullable User user) {
        Bundle args = new Bundle();

        GroupInformationFragment fragment = new GroupInformationFragment();
        fragment.setArguments(args);
        fragment.group = group;
        fragment.user = user;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_group_info, container, false);
        }

        viewBinding = DataBindingUtil.bind(view);
        viewBinding.setHideParticipantCard(false);

        if (user != null) {
            viewBinding.setUser(user);
        }

        if (group != null) {
            setGroup(group);
        }

        unbinder = ButterKnife.bind(this, view);

        bossHpBar = (ValueBar) view.findViewById(R.id.bossHpBar);
        bossRageBar = (ValueBar) view.findViewById(R.id.bossRageBar);

        if (this.group == null) {
            QrCodeManager qrCodeManager = new QrCodeManager(userRepository, this.getContext());
            qrCodeManager.setUpView(qrLayout);
        }

        return view;
    }

    @Override
    public void onDestroy() {
        userRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setGroup(@Nullable Group group) {
        if (group == null) {
            this.group = null;
            return;
        }
        if (viewBinding != null) {
            viewBinding.setGroup(group);
        }

        this.group = group;

        if (isAdded()) {
            updateQuestProgress(group, quest);
        }
    }

    public void setQuestContent(@Nullable QuestContent quest) {
        if (quest == null) {
            return;
        }
        if (viewBinding != null) {
            viewBinding.setQuest(quest);
        }

        updateQuestProgress(group, quest);

        this.quest = quest;
    }

    private void updateQuestProgress(@Nullable Group group, QuestContent quest) {
        if (group == null || quest == null) {
            return;
        }

        boolean showHpBar = (quest.getBoss() != null && quest.getBoss().hp > 0);
        bossHpBar.setVisibility(showHpBar ? View.VISIBLE : View.GONE);
        if (showHpBar) {
            bossHpBar.set(group.quest.getProgress().hp, quest.getBoss().hp);
        }
        boolean showRageBar = (quest.getBoss() != null && quest.getBoss().hasRage());
        bossRageBar.setVisibility(showRageBar ? View.VISIBLE : View.GONE);
        if (showRageBar) {
            bossHpBar.set(group.quest.getProgress().rage, quest.getBoss().rage.value);
        }

        if (group.quest.members == null) {
            viewBinding.setHideParticipantCard(true);
        }
    }
}
