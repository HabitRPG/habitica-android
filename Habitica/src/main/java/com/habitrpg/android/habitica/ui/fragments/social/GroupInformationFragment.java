package com.habitrpg.android.habitica.ui.fragments.social;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.helpers.QrCodeManager;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.ui.adapter.social.QuestCollectRecyclerViewAdapter;
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
    UserRepository userRepository;

    @BindView(R.id.questMemberView)
    LinearLayout questMemberView;

    @BindView(R.id.collectionStats)
    RecyclerView collectionStats;

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
    private HabitRPGUser user;
    private QuestContent quest;
    private ValueBarBinding bossHpBar;
    private ValueBarBinding bossRageBar;

    private QuestCollectRecyclerViewAdapter questCollectViewAdapter;

    public GroupInformationFragment() {

    }

    public static GroupInformationFragment newInstance(@Nullable Group group, @Nullable HabitRPGUser user) {
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
        if (view == null)
            view = inflater.inflate(R.layout.fragment_group_info, container, false);
        questCollectViewAdapter = new QuestCollectRecyclerViewAdapter();

        viewBinding = DataBindingUtil.bind(view);
        viewBinding.setHideParticipantCard(false);

        if (user != null) {
            viewBinding.setUser(user);
        }

        if (group != null) {
            setGroup(group);
        }

        unbinder = ButterKnife.bind(this, view);

        collectionStats.setLayoutManager(new LinearLayoutManager(getContext()));
        collectionStats.setAdapter(questCollectViewAdapter);
        bossHpBar = DataBindingUtil.bind(view.findViewById(R.id.bossHpBar));
        bossRageBar = DataBindingUtil.bind(view.findViewById(R.id.bossRageBar));

        if (this.group == null) {
            QrCodeManager qrCodeManager = new QrCodeManager(userRepository, this.getContext());
            qrCodeManager.setUpView(qrLayout);

            if (user != null && user.getInvitations().getParty() != null && user.getInvitations().getParty().getId() != null) {
                viewBinding.setInvitation(user.getInvitations().getParty());
            }
        }

        return view;
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

        if (questMemberView != null) {
            updateQuestMember(group);
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
        if (questCollectViewAdapter != null) {
            questCollectViewAdapter.setQuestContent(quest);
            if (group.quest.getProgress() != null) {
                questCollectViewAdapter.setQuestProgress(group.quest.getProgress());
            }
        }

        bossHpBar.valueBarLayout.setVisibility((quest.boss != null && quest.boss.hp > 0) ? View.VISIBLE : View.GONE);
        bossRageBar.valueBarLayout.setVisibility((quest.boss != null && quest.boss.rage_value > 0) ? View.VISIBLE : View.GONE);

        if (group.quest.members == null) {
            viewBinding.setHideParticipantCard(true);
        }
    }

    private void updateQuestMember(Group group) {
        questMemberView.removeAllViewsInLayout();
        if (group.quest == null || group.quest.key == null) return;
        Context context = getContext();
        if (context == null && group.quest.members != null) {
            viewBinding.setHideParticipantCard(true);
            return;
        }
        if (group.quest.members == null || group.members == null) {
            viewBinding.setHideParticipantCard(true);
            return;
        }
        viewBinding.setHideParticipantCard(false);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (HabitRPGUser member : group.members) {
            final LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.party_member_quest, questMemberView, false);
            TextView questResponse = (TextView) itemView.findViewById(R.id.rsvpneeded);
            TextView userName = (TextView) itemView.findViewById(R.id.username);
            userName.setText(member.getProfile().getName());

            if (!group.quest.members.containsKey(member.getId()))
                continue;
            Boolean questresponse = group.quest.members.get(member.getId());
            if (group.quest.active) {
                questResponse.setText("");
            } else if (questresponse == null) {
                questResponse.setText(R.string.quest_pending);
            } else if (questresponse) {
                questResponse.setText(R.string.quest_accepted);
                questResponse.setTextColor(ContextCompat.getColor(context, R.color.good_10));
            } else {
                questResponse.setText(R.string.quest_rejected);
                questResponse.setTextColor(ContextCompat.getColor(context, R.color.worse_10));
            }
            questMemberView.post(() -> {
                if (questMemberView != null) {
                    questMemberView.addView(itemView);
                }
            });
        }
    }


    @OnClick(R.id.btnQuestAccept)
    public void onQuestAccept() {
        if (group != null) {
            apiClient.acceptQuest(group.id)
                    .subscribe(aVoid -> {
                        if (user != null) {
                            user.getParty().getQuest().RSVPNeeded = false;
                            group.quest.members.put(user.getId(), true);
                        }
                        setGroup(group);
                        viewBinding.setUser(user);
                    }, throwable -> {
                    });
        }
    }


    @OnClick(R.id.btnQuestReject)
    public void onQuestReject() {
        if (group != null) {
            apiClient.rejectQuest(group.id)
                    .subscribe(aVoid -> {
                        if (user != null) {
                            user.getParty().getQuest().RSVPNeeded = false;
                            group.quest.members.put(user.getId(), false);
                        }
                        setGroup(group);
                        viewBinding.setUser(user);
                    }, throwable -> {
                    });
        }
    }


    @OnClick(R.id.btnQuestLeave)
    public void onQuestLeave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to leave the active quest? All your quest progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (group != null) {
                        apiClient.leaveQuest(group.id)
                                .subscribe(aVoid -> {
                                    if (user != null) {
                                        group.quest.members.remove(user.getId());
                                    }
                                    setGroup(group);
                                }, throwable -> {
                                });
                    }
                }).setNegativeButton("No", (dialog, which) -> {

                });
        builder.show();
    }

    @OnClick(R.id.btnQuestBegin)
    public void onQuestBegin() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_begin_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (group != null) {
                        apiClient.forceStartQuest(group.id, group)
                                .subscribe(quest -> {
                                    group.quest = quest;
                                    setGroup(group);
                                }, throwable -> {
                                });
                    }
                }).setNegativeButton(R.string.no, (dialog, which) -> {

                });
        builder.show();
    }

    @OnClick(R.id.btnQuestCancel)
    public void onQuestCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_cancel_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (group != null) {
                        apiClient.cancelQuest(group.id)
                                .subscribe(aVoid -> {
                                    setGroup(group);
                                    setQuestContent(null);
                                }, throwable -> {
                                });
                    }
                }).setNegativeButton(R.string.no, (dialog, which) -> {

                });
        builder.show();
    }

    @OnClick(R.id.btnQuestAbort)
    public void onQuestAbort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to abort this mission? It will abort it for everyone in your party and all progress will be lost. The quest scroll will be returned to the quest owner.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (group != null) {
                        apiClient.abortQuest(group.id)
                                .subscribe(quest -> {
                                    group.quest = quest;
                                    setGroup(group);
                                    setQuestContent(null);
                                }, throwable -> {
                                });
                    }
                }).setNegativeButton("No", (dialog, which) -> {

                });
        builder.show();
    }

    @OnClick(R.id.btnPartyInviteAccept)
    public void onPartyInviteAccepted() {
        if (user != null) {
            apiClient.joinGroup(user.getInvitations().getParty().getId())
                    .subscribe(group -> {
                        setGroup(group);
                        viewBinding.setInvitation(null);
                    }, throwable -> {
                    });
        }
    }

    @OnClick(R.id.btnPartyInviteReject)
    public void onPartyInviteRejected() {
        if (user != null) {
            apiClient.rejectGroupInvite(user.getInvitations().getParty().getId())
                    .subscribe(aVoid -> viewBinding.setInvitation(null), throwable -> {});
        }
    }
}
