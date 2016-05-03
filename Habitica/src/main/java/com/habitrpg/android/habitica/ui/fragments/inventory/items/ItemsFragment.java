package com.habitrpg.android.habitica.ui.fragments.inventory.items;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.events.commands.HatchingCommand;
import com.habitrpg.android.habitica.events.commands.InvitePartyToQuestCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.PetDetailRecyclerFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.UserParty;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ItemsFragment extends BaseMainFragment {

    public ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        viewPager.setCurrentItem(0);

        setViewPagerAdapter();

        return v;
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                ItemRecyclerFragment fragment = new ItemRecyclerFragment();

                switch (position) {
                    case 0: {
                        fragment.itemType = "eggs";
                        break;
                    }
                    case 1: {
                        fragment.itemType = "hatchingPotions";
                        break;
                    }
                    case 2: {
                        fragment.itemType = "food";
                        break;
                    }
                    case 3: {
                        fragment.itemType = "quests";
                        break;
                    }
                }
                fragment.isHatching = false;
                fragment.isFeeding = false;
                fragment.itemTypeText = this.getPageTitle(position).toString();

                return fragment;
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return activity.getString(R.string.eggs);
                    case 1:
                        return activity.getString(R.string.hatching_potions);
                    case 2:
                        return activity.getString(R.string.food);
                    case 3:
                        return activity.getString(R.string.quests);
                }
                return "";
            }
        });
        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    @Subscribe
    public void onEvent(InvitePartyToQuestCommand event) {
        this.mAPIHelper.apiService.inviteToQuest("party", event.questKey, new Callback<Group>() {
            @Override
            public void success(Group group, Response response) {
                OpenMenuItemCommand event = new OpenMenuItemCommand();
                event.identifier = MainDrawerBuilder.SIDEBAR_PARTY;
                EventBus.getDefault().post(event);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Subscribe
    public void showHatchingDialog(HatchingCommand event) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            ItemRecyclerFragment fragment = new ItemRecyclerFragment();
            if (event.usingEgg != null) {
                fragment.itemType = "hatchingPotions";
                fragment.hatchingItem= event.usingEgg;
            } else {
                fragment.itemType = "eggs";
                fragment.hatchingItem = event.usingHatchingPotion;
            }
            fragment.isHatching = true;
            fragment.isFeeding = false;
            fragment.ownedPets = this.user.getItems().getPets();
            fragment.show(getFragmentManager(), "hatchingDialog");
        }
    }
}
