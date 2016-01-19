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
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
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

    @Bind(R.id.avatar_shirt)
    View avatarShirtView;

    @Bind(R.id.avatar_skin)
    View avatarSkinView;

    @Bind(R.id.avatar_hair_color)
    View avatarHairColorView;

    @Bind(R.id.avatar_hair_base)
    View avatarHairBaseView;

    @Bind(R.id.avatar_hair_bangs)
    View avatarHairBangsView;

    @Bind(R.id.avatar_hair_flower)
    View avatarHairFlowerView;

    @Bind(R.id.avatar_hair_beard)
    View avatarHairBeardView;

    @Bind(R.id.avatar_hair_mustache)
    View avatarHairMustacheView;

    @Bind(R.id.avatar_background)
    View avatarBackgroundView;

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

        avatarShirtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("shirt", null);
            }
        });

        avatarSkinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("skin", null);
            }
        });

        avatarHairColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("hair", "color");
            }
        });
        avatarHairBangsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("hair", "bangs");
            }
        });
        avatarHairBaseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("hair", "base");
            }
        });
        avatarHairFlowerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("hair", "flower");
            }
        });
        avatarHairBeardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("hair", "beard");
            }
        });
        avatarHairMustacheView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("hair", "mustache");
            }
        });
        avatarBackgroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomizationFragment("background", null);
            }
        });

        return v;
    }

    private void displayCustomizationFragment(String type, String category) {
                AvatarCustomizationFragment fragment = new AvatarCustomizationFragment();
                fragment.type = type;
                fragment.category = category;
                activity.displayFragment(fragment);
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
