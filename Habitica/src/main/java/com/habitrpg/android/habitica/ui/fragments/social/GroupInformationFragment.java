package com.habitrpg.android.habitica.ui.fragments.social;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.databinding.FragmentGroupInfoBinding;
import com.habitrpg.android.habitica.helpers.QrCodeManager;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupInformationFragment extends BaseFragment {


    FragmentGroupInfoBinding viewBinding;

    @Inject
    ApiClient apiClient;
    @Inject
    SocialRepository socialRepository;
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
            setUser(user);
        } else {
            compositeSubscription.add(userRepository.getUser().subscribe(user1 -> {
                user = user1;
                setUser(user);
            }, RxErrorHandler.handleEmptyError()));
        }

        if (group != null) {
            setGroup(group);
        }

        unbinder = ButterKnife.bind(this, view);

        if (this.group == null) {
            QrCodeManager qrCodeManager = new QrCodeManager(userRepository, this.getContext());
            qrCodeManager.setUpView(qrLayout);
        }

        return view;
    }

    private void setUser(User user) {
        viewBinding.setUser(user);
        if (user.getInvitations() != null) {
            viewBinding.setInvitation(user.getInvitations().getParty());
        }
    }

    @Override
    public void onDestroy() {
        userRepository.close();
        socialRepository.close();
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

        if (group.quest.members == null) {
            viewBinding.setHideParticipantCard(true);
        }
    }


    @OnClick(R.id.btnPartyInviteAccept)
    public void onPartyInviteAccepted() {
        if (user != null) {
            socialRepository.joinGroup(user.getInvitations().getParty().getId())
                    .doOnNext(group1 -> viewBinding.setInvitation(null))
                    .flatMap(group1 -> userRepository.retrieveUser(false))
                    .flatMap(user -> socialRepository.retrieveGroup("party"))
                    .flatMap(group1 -> socialRepository.retrieveGroupMembers(group1.id, true))
                    .subscribe(members -> {}, RxErrorHandler.handleEmptyError());
        }
    }

    @OnClick(R.id.btnPartyInviteReject)
    public void onPartyInviteRejected() {
        if (user != null) {
            socialRepository.rejectGroupInvite(user.getInvitations().getParty().getId())
                    .subscribe(aVoid -> {
                        viewBinding.setInvitation(null);
                    }, RxErrorHandler.handleEmptyError());
        }
    }

}
