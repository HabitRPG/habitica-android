package com.habitrpg.android.habitica.ui.fragments.inventory.customization;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.MergeUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.databinding.FragmentAvatarOverviewBinding;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvatarOverviewFragment extends BaseMainFragment implements AdapterView.OnItemSelectedListener {

    FragmentAvatarOverviewBinding viewBinding;

    @BindView(R.id.avatar_size_spinner)
    Spinner avatarSizeSpinner;

    @BindView(R.id.avatar_shirt)
    View avatarShirtView;

    @BindView(R.id.avatar_skin)
    View avatarSkinView;

    @BindView(R.id.avatar_hair_color)
    View avatarHairColorView;

    @BindView(R.id.avatar_hair_base)
    View avatarHairBaseView;

    @BindView(R.id.avatar_hair_bangs)
    View avatarHairBangsView;

    @BindView(R.id.avatar_hair_flower)
    View avatarHairFlowerView;

    @BindView(R.id.avatar_hair_beard)
    View avatarHairBeardView;

    @BindView(R.id.avatar_hair_mustache)
    View avatarHairMustacheView;

    @BindView(R.id.avatar_background)
    View avatarBackgroundView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (apiClient != null) {
            apiClient.getContent()
                    .subscribe(contentResult -> {}, throwable -> {});
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_avatar_overview, container, false);

        if (this.user == null) {
            return view;
        }

        viewBinding = DataBindingUtil.bind(view);
        viewBinding.setPreferences(this.user.getPreferences());

        unbinder = ButterKnife.bind(this, view);

        this.setSize(this.user.getPreferences().getSize());
        avatarSizeSpinner.setOnItemSelectedListener(this);

        avatarShirtView.setOnClickListener(v1 -> displayCustomizationFragment("shirt", null));

        avatarSkinView.setOnClickListener(v1 -> displayCustomizationFragment("skin", null));

        avatarHairColorView.setOnClickListener(v1 -> displayCustomizationFragment("hair", "color"));
        avatarHairBangsView.setOnClickListener(v1 -> displayCustomizationFragment("hair", "bangs"));
        avatarHairBaseView.setOnClickListener(v1 -> displayCustomizationFragment("hair", "base"));
        avatarHairFlowerView.setOnClickListener(v1 -> displayCustomizationFragment("hair", "flower"));
        avatarHairBeardView.setOnClickListener(v1 -> displayCustomizationFragment("hair", "beard"));
        avatarHairMustacheView.setOnClickListener(v1 -> displayCustomizationFragment("hair", "mustache"));
        avatarBackgroundView.setOnClickListener(v1 -> displayCustomizationFragment("background", null));

        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void displayCustomizationFragment(String type, @Nullable String category) {
        AvatarCustomizationFragment fragment = new AvatarCustomizationFragment();
        fragment.type = type;
        fragment.category = category;
        if (activity != null) {
            activity.displayFragment(fragment);
        }
    }

    @Override
    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        if (user != null) {
            viewBinding.setPreferences(user.getPreferences());
            this.setSize(user.getPreferences().getSize());
        }
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

        if (this.user != null && !this.user.getPreferences().getSize().equals(newSize)) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("preferences.size", newSize);
            apiClient.updateUser(updateData)
                    .subscribe(new MergeUserCallback(activity, user), throwable -> {
                    });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    @Override
    public String customTitle() {
        return getString(R.string.sidebar_avatar);
    }
}
