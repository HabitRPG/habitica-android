package com.habitrpg.android.habitica.ui.fragments.setup;

import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SetupCustomizationRepository;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.activities.SetupActivity;
import com.habitrpg.android.habitica.ui.adapter.setup.CustomizationSetupAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.views.setup.AvatarCategoryView;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.SetupCustomization;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AvatarSetupFragment extends BaseFragment {

    public SetupActivity activity;
    public int width;
    View view;
    @BindView(R.id.avatarView)
    AvatarView avatarView;
    @BindView(R.id.customization_list)
    RecyclerView customizationList;
    @BindView(R.id.subcategory_tabs)
    TabLayout subCategoryTabs;
    @BindView(R.id.body_button)
    AvatarCategoryView bodyButton;
    @BindView(R.id.skin_button)
    AvatarCategoryView skinButton;
    @BindView(R.id.hair_button)
    AvatarCategoryView hairButton;
    @BindView(R.id.extras_button)
    AvatarCategoryView extrasButton;
    @BindView(R.id.caret_view)
    ImageView caretView;

    CustomizationSetupAdapter adapter;
    LinearLayoutManager layoutManager;
    @Inject
    SetupCustomizationRepository customizationRepository;
    @Inject
    ApiClient apiClient;

    private HabitRPGUser user;
    private List<String> subcategories;
    private AvatarCategoryView activeButton;
    private String activeCategory;
    private String activeSubCategory;
    private Random random;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_setup_avatar, container, false);
        }

        unbinder = ButterKnife.bind(this, view);
        this.adapter = new CustomizationSetupAdapter();
        if (this.user != null) {
            this.adapter.userSize = this.user.getPreferences().getSize();
        } else {
            this.adapter.userSize = "slim";
        }
        this.adapter.user = this.user;
        this.layoutManager = new LinearLayoutManager(activity);
        this.layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        this.customizationList.setLayoutManager(this.layoutManager);

        this.customizationList.setAdapter(this.adapter);

        this.subCategoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (subcategories != null && position < subcategories.size()) {
                    activeSubCategory = subcategories.get(position);
                }
                loadCustomizations();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        this.selectedBodyCategory();

        this.random = new Random();

        if (this.user != null) {
            this.updateAvatar();
        }
        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void loadCustomizations() {
        if (this.user == null || this.adapter == null || this.activeCategory == null) {
            return;
        }

        this.adapter.setCustomizationList(customizationRepository.getCustomizations(activeCategory, activeSubCategory, user));
    }

    public void setUser(HabitRPGUser user) {
        this.user = user;
        if (avatarView != null) {
            updateAvatar();
        }
        if (this.adapter != null) {
            this.adapter.user = user;
            this.adapter.notifyDataSetChanged();
        }
    }

    private void updateAvatar() {
        avatarView.setUser(user);
    }


    @OnClick(R.id.body_button)
    protected void selectedBodyCategory() {
        activateButton(bodyButton);
        this.activeCategory = "body";
        this.subCategoryTabs.removeAllTabs();
        this.subcategories = Arrays.asList("size", "shirt");
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_size));
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_shirt));
        loadCustomizations();
    }

    @OnClick(R.id.skin_button)
    protected void selectedSkinCategory() {
        activateButton(skinButton);
        this.activeCategory = "skin";
        this.subCategoryTabs.removeAllTabs();
        this.subcategories = Collections.singletonList("color");
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_skin_color));
        loadCustomizations();
    }

    @OnClick(R.id.hair_button)
    protected void selectedHairCategory() {
        activateButton(hairButton);
        this.activeCategory = "hair";
        this.subCategoryTabs.removeAllTabs();
        this.subcategories = Arrays.asList("bangs", "color", "ponytail");
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_hair_bangs));
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_hair_color));
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_hair_ponytail));
        loadCustomizations();
    }

    @OnClick(R.id.extras_button)
    protected void selectedExtrasCategory() {
        activateButton(extrasButton);
        this.activeCategory = "extras";
        this.subCategoryTabs.removeAllTabs();
        this.subcategories = Arrays.asList("glasses", "flower", "wheelchair");
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_glasses));
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_flower));
        this.subCategoryTabs.addTab(subCategoryTabs.newTab().setText(R.string.avatar_wheelchair));
        loadCustomizations();
    }

    @OnClick(R.id.randomize_button)
    protected  void randomizeCharacter() {
        if (user == null) {
            return;
        }
        UpdateUserCommand command = new UpdateUserCommand();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("preferences.size", chooseRandomKey(customizationRepository.getCustomizations("body", "size", user), false));
        updateData.put("preferences.shirt", chooseRandomKey(customizationRepository.getCustomizations("body", "shirt", user), false));
        updateData.put("preferences.skin", chooseRandomKey(customizationRepository.getCustomizations("skin", "color", user), false));
        updateData.put("preferences.hair.color", chooseRandomKey(customizationRepository.getCustomizations("hair", "color", user), false));
        updateData.put("preferences.hair.base", chooseRandomKey(customizationRepository.getCustomizations("hair", "ponytail", user), false));
        updateData.put("preferences.hair.bangs", chooseRandomKey(customizationRepository.getCustomizations("hair", "bangs", user), false));
        updateData.put("preferences.hair.flower", chooseRandomKey(customizationRepository.getCustomizations("extras", "flower", user), true));
        updateData.put("preferences.chair", chooseRandomKey(customizationRepository.getCustomizations("extras", "wheelchair", user), true));
        command.updateData = updateData;

        EventBus.getDefault().post(command);
    }

    @Nullable
    private String chooseRandomKey(List<SetupCustomization> customizations, boolean weighFirstOption) {
        if (customizations.size() == 0) {
            return null;
        }
        if (weighFirstOption) {
            if (random.nextInt(10) > 3) {
                return customizations.get(0).key;
            }
        }
        return customizations.get(random.nextInt(customizations.size())).key;
    }

    private void activateButton(AvatarCategoryView button) {
        if (this.activeButton != null) {
            this.activeButton.setActive(false);
        }
        this.activeButton = button;
        this.activeButton.setActive(true);
        int[] location = new int[2];
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.caretView.getLayoutParams();
        this.activeButton.getLocationOnScreen(location);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
            params.setMarginStart(location[0]+px);
            this.caretView.setLayoutParams(params);
        } else {
            caretView.setVisibility(View.GONE);
        }

    }
}
