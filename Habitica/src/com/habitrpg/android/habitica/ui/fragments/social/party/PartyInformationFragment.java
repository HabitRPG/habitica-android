package com.habitrpg.android.habitica.ui.fragments.social.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.FragmentPartyInfoBinding;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Negue on 16.09.2015.
 */
public class PartyInformationFragment extends Fragment {


    private View view;
    FragmentPartyInfoBinding viewBinding;
    APIHelper mAPIHelper;
    @Bind(R.id.questMemberView)
    LinearLayout questMemberView;
    private Group group;
    private HabitRPGUser user;


    public static PartyInformationFragment newInstance(Group group, HabitRPGUser user, APIHelper mAPIHelper) {

        Bundle args = new Bundle();

        PartyInformationFragment fragment = new PartyInformationFragment();
        fragment.setArguments(args);
        fragment.group = group;
        fragment.user = user;
        fragment.mAPIHelper = mAPIHelper;
        return fragment;
    }

    public PartyInformationFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_party_info, container, false);

        viewBinding = DataBindingUtil.bind(view);
        if (user != null) {
            viewBinding.setUser(user);
        }

        if (group != null) {
            setGroup(group);
        }

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setGroup(Group group) {
        if (viewBinding != null) {
            viewBinding.setParty(group);
        }

        if (questMemberView != null) {
            updateQuestMember(group);
        }

        this.group = group;
    }

    public void setQuestContent(QuestContent quest) {
        if (viewBinding != null) {
            viewBinding.setQuest(quest);
        }
    }

    private void updateQuestMember(Group group) {

        questMemberView.removeAllViewsInLayout();
        if (group.quest.key == null) return;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (HabitRPGUser member : group.members) {
            final LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.party_member_quest, null);
            TextView questResponse = (TextView) itemView.findViewById(R.id.rsvpneeded);
            TextView userName = (TextView) itemView.findViewById(R.id.username);
            if (group.quest.leader.equals(member.getId()))
                userName.setText("* " + member.getProfile().getName());
            else
                userName.setText(member.getProfile().getName());

            if (!group.quest.members.containsKey(member.getId()))
                continue;
            Boolean questresponse = group.quest.members.get(member.getId());
            if (group.quest.active) {
                questResponse.setText("");
            } else if (questresponse == null) {
                questResponse.setText("Pending");
            } else if (questresponse.booleanValue() == true) {
                questResponse.setText("Accepted");
                questResponse.setTextColor(Color.parseColor("#2db200"));
            } else if (questresponse.booleanValue() == false) {
                questResponse.setText("Rejected");
                questResponse.setTextColor(Color.parseColor("#b30409"));
            }
            questMemberView.post(new Runnable() {
                @Override
                public void run() {
                    questMemberView.addView(itemView);
                }
            });
        }
    }


    @OnClick(R.id.btnQuestAccept)
    public void onQuestAccept() {
        mAPIHelper.apiService.acceptQuest(group.id, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                user.getParty().getQuest().RSVPNeeded = false;
                group.quest.members.put(user.getId(), true);
                setGroup(group);
                viewBinding.setUser(user);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    @OnClick(R.id.btnQuestReject)
    public void onQuestReject() {
        mAPIHelper.apiService.rejectQuest(group.id, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                user.getParty().getQuest().RSVPNeeded = false;
                group.quest.members.put(user.getId(), false);
                setGroup(group);
                viewBinding.setUser(user);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    @OnClick(R.id.btnQuestLeave)
    public void onQuestLeave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to leave the active quest? All your quest progress will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAPIHelper.apiService.leaveQuest(group.id, new Callback<Void>() {
                            @Override
                            public void success(Void aVoid, Response response) {
                                group.quest.members.remove(user.getId());
                                setGroup(group);
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @OnClick(R.id.btnQuestBegin)
    public void onQuestBegin() {
        mAPIHelper.apiService.forceStartQuest(group.id, group, new Callback<Group>() {
            @Override
            public void success(Group group, Response response) {
                setGroup(group);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @OnClick(R.id.btnQuestCancel)
    public void onQuestCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to cancel this quest? All invitation acceptances will be lost. The quest owner will retain possession of the quest scroll.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAPIHelper.apiService.cancelQuest(group.id, new Callback<Void>() {
                            @Override
                            public void success(Void aVoid, Response response) {
                                setGroup(group);
                                setQuestContent(null);
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @OnClick(R.id.btnQuestAbort)
    public void onQuestAbort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to abort this mission? It will abort it for everyone in your party and all progress will be lost. The quest scroll will be returned to the quest owner.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAPIHelper.apiService.abortQuest(group.id, new Callback<Group>() {
                            @Override
                            public void success(Group group, Response response) {
                                setGroup(group);
                                setQuestContent(null);
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

}
