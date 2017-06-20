package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.views.social.QuestProgressView;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;


public class PartyDetailFragment extends BaseFragment {

    @Inject
    SocialRepository socialRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    InventoryRepository inventoryRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.party_invitation_wrapper)
    ViewGroup partyInvitationWrapper;

    @BindView(R.id.title_view)
    TextView titleView;
    @BindView(R.id.description_view)
    TextView descriptionView;

    @BindView(R.id.new_quest_button)
    Button newQuestButton;
    @BindView(R.id.quest_detail_button)
    ViewGroup questDetailButton;
    @BindView(R.id.quest_scroll_image_view)
    SimpleDraweeView questScrollImageView;
    @BindView(R.id.quest_title_view)
    TextView questTitleView;
    @BindView(R.id.quest_participation_view)
    TextView questParticipationView;
    @BindView(R.id.quest_image_wrapper)
    ViewGroup questImageWrapper;
    @BindView(R.id.quest_image_view)
    SimpleDraweeView questImageView;
    @BindView(R.id.quest_participant_response_wrapper)
    ViewGroup questParticipantResponseWrapper;
    @BindView(R.id.quest_leader_response_wrapper)
    ViewGroup questLeaderResponseWrapper;
    @BindView(R.id.quest_accept_button)
    Button questAcceptButton;
    @BindView(R.id.quest_reject_button)
    Button questRejectButton;
    @BindView(R.id.quest_begin_button)
    Button questBeginButton;
    @BindView(R.id.quest_cancel_button)
    Button questCancelButton;
    @BindView(R.id.quest_progress_view)
    QuestProgressView questProgressView;


    public String partyId;
    private Group party;
    private Quest quest;
    private User user;

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_party_detail, container, false);
    }

    @Override
    public void onDestroyView() {
        socialRepository.close();
        userRepository.close();
        inventoryRepository.close();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setOnRefreshListener(this::refreshParty);

        compositeSubscription.add(socialRepository.getGroup(partyId).subscribe(this::updateParty, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(userRepository.getUser(userId).subscribe(this::updateUser, RxErrorHandler.handleEmptyError()));
    }

    private void refreshParty() {
        socialRepository.retrieveGroup("party")
                .flatMap(group1 -> socialRepository.retrieveGroupMembers(group1.id, true))
                .subscribe(members -> {
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }
                }, throwable -> refreshLayout.setRefreshing(false));
    }

    private void updateParty(Group party) {
        this.party = party;
        this.quest = party.quest;
        if (titleView == null) {
            return;
        }
        if (party.quest != null && user != null && user.getId().equals(party.quest.leader)) {
            questLeaderResponseWrapper.setVisibility(View.VISIBLE);
            questParticipantResponseWrapper.setVisibility(View.GONE);
        }
        titleView.setText(party.name);
        descriptionView.setText(party.description);

        if (quest.key != null) {
            newQuestButton.setVisibility(View.GONE);
            questDetailButton.setVisibility(View.VISIBLE);
            questImageWrapper.setVisibility(View.VISIBLE);

            getActivity().runOnUiThread(() -> inventoryRepository.getQuestContent(quest.getKey())
                    .first()
                    .subscribe(this::updateQuestContent, RxErrorHandler.handleEmptyError()));
        } else {
            newQuestButton.setVisibility(View.VISIBLE);
            questDetailButton.setVisibility(View.GONE);
            questImageWrapper.setVisibility(View.GONE);
            questLeaderResponseWrapper.setVisibility(View.GONE);
            questParticipantResponseWrapper.setVisibility(View.GONE);
            questProgressView.setVisibility(View.GONE);
        }
    }

    private void updateUser(User user) {
        if (user == null || user.getParty() == null || user.getParty().getQuest() == null) {
            return;
        }
        this.user = user;

        int invitationVisibility = View.GONE;
        if (user.getInvitations() != null && user.getInvitations().getParty() != null && user.getInvitations().getParty().getId() != null) {
            invitationVisibility = View.VISIBLE;
        }

        if (partyInvitationWrapper != null) {
            partyInvitationWrapper.setVisibility(invitationVisibility);
        }

        if (questLeaderResponseWrapper != null) {
            if (isQuestActive() || !user.getParty().getQuest().RSVPNeeded) {
                questLeaderResponseWrapper.setVisibility(View.GONE);
                questParticipantResponseWrapper.setVisibility(View.GONE);
            } else {
                questLeaderResponseWrapper.setVisibility(View.GONE);
                questParticipantResponseWrapper.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateQuestContent(QuestContent questContent) {
        if (questTitleView != null) {
            questTitleView.setText(questContent.getText());
        }
        DataBindingUtils.loadImage(questScrollImageView, "inventory_quest_scroll_"+questContent.getKey());
        DataBindingUtils.loadImage(questImageView, "quest_"+questContent.getKey());
        if (isQuestActive()) {
            questProgressView.setVisibility(View.VISIBLE);
            questProgressView.setData(questContent, quest.getProgress());

            questParticipationView.setText(getString(R.string.number_participants, quest.members.size()));
        } else {
            questProgressView.setVisibility(View.GONE);
        }
    }

    private boolean isQuestActive() {
        return quest != null && quest.active;
    }

    @OnClick(R.id.new_quest_button)
    public void inviteNewQuest() {

    }

    @OnClick(R.id.leave_button)
    public void leaveParty() {

    }

    @OnClick(R.id.quest_accept_button)
    public void onQuestAccept() {
        socialRepository.acceptQuest(user, partyId).subscribe(aVoid -> {}, throwable -> {});
    }


    @OnClick(R.id.quest_reject_button)
    public void onQuestReject() {
        socialRepository.rejectQuest(user, partyId).subscribe(aVoid -> {}, throwable -> {});
    }

    @OnClick(R.id.quest_begin_button)
    public void onQuestBegin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_begin_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> socialRepository.forceStartQuest(party)
                        .subscribe(quest -> {}, throwable -> {}))
                .setNegativeButton(R.string.no, (dialog, which) -> {});
        builder.show();
    }

    @OnClick(R.id.quest_cancel_button)
    public void onQuestCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_cancel_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> socialRepository.cancelQuest(partyId)
                        .subscribe(aVoid -> {}, throwable -> {})).setNegativeButton(R.string.no, (dialog, which) -> {});
        builder.show();
    }

    @OnClick(R.id.party_invite_accept_button)
    public void onPartyInviteAccepted() {
        if (user != null) {
            socialRepository.joinGroup(user.getInvitations().getParty().getId())
                    .subscribe(group -> {}, throwable -> {});
        }
    }

    @OnClick(R.id.party_invite_reject_button)
    public void onPartyInviteRejected() {
        if (user != null) {
            socialRepository.rejectGroupInvite(user.getInvitations().getParty().getId())
                    .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        }
    }
}
