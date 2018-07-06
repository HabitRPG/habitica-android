package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.social.QuestDetailFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.views.social.OldQuestProgressView;

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

    @BindView(R.id.refreshLayout)
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
    @BindView(R.id.quest_accept_button)
    Button questAcceptButton;
    @BindView(R.id.quest_reject_button)
    Button questRejectButton;
    @BindView(R.id.quest_progress_view)
    OldQuestProgressView questProgressView;
    @BindView(R.id.quest_participant_list)
    LinearLayout questParticipantList;


    public String partyId;
    private Group party;
    private Quest quest;
    private User user;

    @Override
    public void injectFragment(@NonNull AppComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setOnRefreshListener(this::refreshParty);

        getCompositeSubscription().add(socialRepository.getGroup(partyId).subscribe(this::updateParty, RxErrorHandler.handleEmptyError()));
        getCompositeSubscription().add(userRepository.getUser(userId).subscribe(this::updateUser, RxErrorHandler.handleEmptyError()));
    }

    private void refreshParty() {
        socialRepository.retrieveGroup("party")
                .flatMap(group1 -> socialRepository.retrieveGroupMembers(group1.getId(), true))
                .subscribe(members -> {}, RxErrorHandler.handleEmptyError(), () -> {
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }
                });
    }

    private void updateParty(Group party) {
        if (party == null) {
            return;
        }
        this.party = party;
        this.quest = party.getQuest();
        if (titleView == null) {
            return;
        }
        titleView.setText(party.getName());
        //TODO: FIX
        //descriptionView.setText(MarkdownParser.parseMarkdown(party.getDescription()));

        if (quest != null && !quest.getKey().isEmpty()) {
            newQuestButton.setVisibility(View.GONE);
            questDetailButton.setVisibility(View.VISIBLE);
            questImageWrapper.setVisibility(View.VISIBLE);
            Handler mainHandler = new Handler(getContext().getMainLooper());
            mainHandler.postDelayed(() -> inventoryRepository.getQuestContent(quest.getKey())
                    .firstElement()
                    .subscribe(PartyDetailFragment.this::updateQuestContent, RxErrorHandler.handleEmptyError()), 500);
        } else {
            newQuestButton.setVisibility(View.VISIBLE);
            questDetailButton.setVisibility(View.GONE);
            questImageWrapper.setVisibility(View.GONE);
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

        if (questParticipantResponseWrapper != null) {
            if (showParticipantButtons()) {
                questParticipantResponseWrapper.setVisibility(View.VISIBLE);
            } else {
                questParticipantResponseWrapper.setVisibility(View.GONE);
            }
        }

        questProgressView.configure(user);
    }

    private boolean showParticipantButtons() {
        return !(user == null || user.getParty() == null || user.getParty().getQuest() == null) && !isQuestActive() && user.getParty().getQuest().getRSVPNeeded();
    }

    private void updateQuestContent(QuestContent questContent) {
        if (questTitleView == null || !questContent.isValid()) {
            return;
        }
        questTitleView.setText(questContent.getText());
        DataBindingUtils.INSTANCE.loadImage(questScrollImageView, "inventory_quest_scroll_"+questContent.getKey());
        DataBindingUtils.INSTANCE.loadImage(questImageView, "quest_"+questContent.getKey());
        if (isQuestActive()) {
            questProgressView.setVisibility(View.VISIBLE);
            questProgressView.setData(questContent, quest.getProgress());

            questParticipationView.setText(getString(R.string.number_participants, quest.getMembers().size()));
        } else {
            questProgressView.setVisibility(View.GONE);
        }
    }

    private boolean isQuestActive() {
        return quest != null && quest.getActive();
    }

    @OnClick(R.id.new_quest_button)
    public void inviteNewQuest() {
        ItemRecyclerFragment fragment = new ItemRecyclerFragment();
        fragment.setItemType("quests");
        fragment.setItemTypeText(getString(R.string.quest));
        fragment.show(getFragmentManager(), "questDialog");
    }

    @OnClick(R.id.leave_button)
    public void leaveParty() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.leave_party_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> socialRepository.leaveGroup(partyId)
                        .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError())).setNegativeButton(R.string.no, (dialog, which) -> {});
        builder.show();    }

    @OnClick(R.id.quest_accept_button)
    public void onQuestAccept() {
        socialRepository.acceptQuest(user, partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
    }


    @OnClick(R.id.quest_reject_button)
    public void onQuestReject() {
        socialRepository.rejectQuest(user, partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
    }

    @OnClick(R.id.party_invite_accept_button)
    public void onPartyInviteAccepted() {
        if (user != null) {
            socialRepository.joinGroup(user.getInvitations().getParty().getId())
                    .subscribe(group -> {}, RxErrorHandler.handleEmptyError());
        }
    }

    @OnClick(R.id.party_invite_reject_button)
    public void onPartyInviteRejected() {
        if (user != null) {
            socialRepository.rejectGroupInvite(user.getInvitations().getParty().getId())
                    .subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        }
    }

    @OnClick(R.id.quest_detail_button)
    public void questDetailButtonClicked() {
        QuestDetailFragment fragment = new QuestDetailFragment();
        fragment.partyId = partyId;
        if (party != null && party.getQuest() != null) {
            fragment.questKey = party.getQuest().getKey();
        }
        if (getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.displayFragment(fragment);
        }
    }
}
