package com.habitrpg.android.habitica.ui.fragments.social;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;

public class QuestDetailFragment extends BaseMainFragment {

    @Inject
    SocialRepository socialRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    InventoryRepository inventoryRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.quest_title_view)
    TextView questTitleView;
    @BindView(R.id.quest_scroll_image_view)
    SimpleDraweeView questScrollImageView;
    @BindView(R.id.quest_leader_view)
    TextView questLeaderView;
    @BindView(R.id.description_view)
    TextView questDescriptionView;
    @BindView(R.id.quest_participant_list)
    LinearLayout questParticipantList;
    @BindView(R.id.participants_header)
    TextView participantHeader;
    @BindView(R.id.participants_header_count)
    TextView participantHeaderCount;
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
    @BindView(R.id.quest_abort_button)
    Button questAbortButton;

    public String partyId;
    public String questKey;
    private Group party;
    private Quest quest;
    private String begin_quest_message;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_quest_detail, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getCompositeSubscription().add(socialRepository.getGroup(partyId).subscribe(this::updateParty, RxErrorHandler.handleEmptyError()));
        getCompositeSubscription().add(inventoryRepository.getQuestContent(questKey).subscribe(this::updateQuestContent, RxErrorHandler.handleEmptyError()));
    }


    private void updateParty(Group group) {
        if (questTitleView == null || group.quest == null) {
            return;
        }
        party = group;
        quest = group.quest;
        setQuestParticipants(group.quest.participants);
        socialRepository.getMember(quest.leader).first().subscribe(member -> {
            if (getContext() != null && questLeaderView != null && member != null) {
                questLeaderView.setText(getContext().getString(R.string.quest_leader_header, member.getDisplayName()));
            }
        }, RxErrorHandler.handleEmptyError());

        if (questLeaderResponseWrapper != null) {
            if (showParticipatantButtons()) {
                questLeaderResponseWrapper.setVisibility(View.GONE);
                questParticipantResponseWrapper.setVisibility(View.VISIBLE);
            } else if (showLeaderButtons()) {
                questParticipantResponseWrapper.setVisibility(View.GONE);
                questLeaderResponseWrapper.setVisibility(View.VISIBLE);
                if (isQuestActive()) {
                    questBeginButton.setVisibility(View.GONE);
                    questCancelButton.setVisibility(View.GONE);
                    questAbortButton.setVisibility(View.VISIBLE);
                } else {
                    questBeginButton.setVisibility(View.VISIBLE);
                    questCancelButton.setVisibility(View.VISIBLE);
                    questAbortButton.setVisibility(View.GONE);
                }
            } else {
                questLeaderResponseWrapper.setVisibility(View.GONE);
                questParticipantResponseWrapper.setVisibility(View.GONE);
            }
        }
    }

    private boolean showLeaderButtons() {
        return party != null && party.quest != null && userId != null && userId.equals(party.quest.leader);
    }

    private boolean showParticipatantButtons() {
        if (user == null || user.getParty() == null || user.getParty().getQuest() == null) {
            return false;
        }
        return !isQuestActive() && user.getParty().getQuest().RSVPNeeded;
    }

    private boolean isQuestActive() {
        return quest != null && quest.active;
    }


    private void updateQuestContent(QuestContent questContent) {
        if (questTitleView == null || !questContent.isManaged()) {
            return;
        }
        questTitleView.setText(questContent.getText());
        questDescriptionView.setText(MarkdownParser.parseMarkdown(questContent.getNotes()));
        DataBindingUtils.INSTANCE.loadImage(questScrollImageView, "inventory_quest_scroll_"+questContent.getKey());
    }

    private void setQuestParticipants(List<Member> participants) {
        if (questParticipantList == null) {
            return;
        }
        questParticipantList.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int participantCount = 0;
        for (Member participant : participants) {
            if (quest.active && (participant.getParticipatesInQuest() == null || !participant.getParticipatesInQuest())) {
                continue;
            }
            View participantView = inflater.inflate(R.layout.quest_participant, questParticipantList, false);
            TextView textView = (TextView) participantView.findViewById(R.id.participant_name);
            textView.setText(participant.getDisplayName());
            TextView statusTextView = (TextView) participantView.findViewById(R.id.status_view);
            if (!quest.active) {
                if (participant.getParticipatesInQuest() == null) {
                    statusTextView.setText(R.string.pending);
                    statusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_200));
                } else if (participant.getParticipatesInQuest()) {
                    statusTextView.setText(R.string.accepted);
                    statusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.green_100));
                } else {
                    statusTextView.setText(R.string.declined);
                    statusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red_100));
                }
            } else {
                statusTextView.setVisibility(View.GONE);
            }
            questParticipantList.addView(participantView);
            if (quest.active || (participant.getParticipatesInQuest() != null && participant.getParticipatesInQuest())) {
                participantCount += 1;
            }
        }
        if (quest.active) {
            participantHeader.setText(R.string.participants);
            participantHeaderCount.setText(String.valueOf(participantCount));
        } else {
            participantHeader.setText(R.string.invitations);
            participantHeaderCount.setText(participantCount + "/" + quest.participants.size());
            begin_quest_message = getString(R.string.quest_begin_message, participantCount, quest.participants.size());
        }
    }

    @Override
    public void onDestroyView() {
        socialRepository.close();
        userRepository.close();
        inventoryRepository.close();
        super.onDestroyView();
    }

    @OnClick(R.id.quest_accept_button)
    public void onQuestAccept() {
        socialRepository.acceptQuest(user, partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
    }


    @OnClick(R.id.quest_reject_button)
    public void onQuestReject() {
        socialRepository.rejectQuest(user, partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
    }

    @OnClick(R.id.quest_begin_button)
    public void onQuestBegin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(begin_quest_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> socialRepository.forceStartQuest(party)
                        .subscribe(quest -> {}, RxErrorHandler.handleEmptyError()))
                .setNegativeButton(R.string.no, (dialog, which) -> {});
        builder.show();
    }

    @OnClick(R.id.quest_cancel_button)
    public void onQuestCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_cancel_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> socialRepository.cancelQuest(partyId)
                        .subscribe(aVoid -> getActivity().getFragmentManager().popBackStack(), RxErrorHandler.handleEmptyError())).setNegativeButton(R.string.no, (dialog, which) -> {});
        builder.show();
    }

    @OnClick(R.id.quest_abort_button)
    public void onQuestAbort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_abort_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> socialRepository.abortQuest(partyId)
                        .subscribe(aVoid -> getActivity().getFragmentManager().popBackStack(), RxErrorHandler.handleEmptyError())).setNegativeButton(R.string.no, (dialog, which) -> {});
        builder.show();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }
}
