package com.habitrpg.android.habitica.ui.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.databinding.FragmentAvatarOverviewBinding;
import com.habitrpg.android.habitica.databinding.FragmentPartyInfoBinding;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viirus on 12/01/16.
 */
public class AvatarOverviewFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    FragmentAvatarOverviewBinding viewBinding;

    @Bind(R.id.avatar_size_spinner)
    Spinner avatarSizeSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_avatar_overview, container, false);

        viewBinding = DataBindingUtil.bind(v);
        viewBinding.setPreferences(this.user.getPreferences());

        ButterKnife.bind(this, v);

        this.setSize(this.user.getPreferences().getSize());
        avatarSizeSpinner.setOnItemSelectedListener(this);

        return v;
    }

    @Override
    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        viewBinding.setPreferences(user.getPreferences());
        this.setSize(user.getPreferences().getSize());
    }

    private void setSize(String size) {
        if (size.equals("slim")) {
            avatarSizeSpinner.setSelection(0, false);
        } else {
            avatarSizeSpinner.setSelection(1, false);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String newSize;
        if (position == 0) {
            newSize = "slim";
        } else {
            newSize = "broad";
        }

        if (!this.user.getPreferences().getSize().equals(newSize)) {
            Map<String, String> updateData = new HashMap<String, String>();
            updateData.put("preferences.size", newSize);
            mAPIHelper.apiService.updateUser(updateData, new HabitRPGUserCallback(activity));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
