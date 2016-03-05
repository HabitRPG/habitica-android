package com.habitrpg.android.habitica.ui.fragments.social;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.FragmentGroupInfoBinding;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;

/**
 * Created by Negue on 16.09.2015.
 */
public class GroupInformationFragment extends Fragment {


    private View view;
    FragmentGroupInfoBinding viewBinding;
    private Group group;

    public static GroupInformationFragment newInstance(Group group) {

        Bundle args = new Bundle();

        GroupInformationFragment fragment = new GroupInformationFragment();
        fragment.setArguments(args);
        fragment.group = group;
        return fragment;
    }

    public GroupInformationFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_group_info, container, false);

        viewBinding = DataBindingUtil.bind(view);

        if (group != null) {
            setGroup(group);
        }

        return view;
    }

    public void setGroup(Group group) {
        if (viewBinding != null) {
            viewBinding.setGroup(group);
        }
    }

    public void setQuestContent(QuestContent quest) {
        if (viewBinding != null) {
            viewBinding.setQuest(quest);
        }
    }
}
