package com.habitrpg.android.habitica.ui.menu;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.AboutActivity;
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.PrefsActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.NewsFragment;
import com.habitrpg.android.habitica.ui.fragments.faq.FAQOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.customization.AvatarOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.equipment.EquipmentOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemsFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.shops.ShopsFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragment;
import com.habitrpg.android.habitica.ui.fragments.skills.SkillsFragment;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GuildsOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.social.InboxFragment;
import com.habitrpg.android.habitica.ui.fragments.social.TavernFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyFragment;
import com.habitrpg.android.habitica.ui.fragments.tasks.TasksFragment;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.Toolbar;

import java.util.Locale;

import static com.habitrpg.android.habitica.ui.activities.MainActivity.GEM_PURCHASE_REQUEST;

public class MainDrawerBuilder {

    // Change the identificationIDs to the position IDs so that its easier to set the selected entry
    public static final int SIDEBAR_TASKS = 0;
    public static final int SIDEBAR_SKILLS = 1;
    public static final int SIDEBAR_INBOX = 2;
    public static final int SIDEBAR_TAVERN = 3;
    public static final int SIDEBAR_PARTY = 4;
    public static final int SIDEBAR_GUILDS = 5;
    public static final int SIDEBAR_CHALLENGES = 6;
    public static final int SIDEBAR_SHOPS = 7;
    public static final int SIDEBAR_AVATAR = 8;
    public static final int SIDEBAR_EQUIPMENT = 9;
    public static final int SIDEBAR_ITEMS = 10;
    public static final int SIDEBAR_STABLE = 11;
    public static final int SIDEBAR_PURCHASE = 12;
    public static final int SIDEBAR_NEWS = 13;
    public static final int SIDEBAR_SETTINGS = 14;
    public static final int SIDEBAR_HELP = 15;
    public static final int SIDEBAR_ABOUT = 16;

    public static AccountHeaderBuilder CreateDefaultAccountHeader(final Activity activity) {
        return new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.sidebar_background)
                .addProfiles(
                        new ProfileDrawerItem()
                )
                .withOnAccountHeaderListener((view, profile, currentProfile) -> false)
                .withSelectionListEnabledForSingleProfile(false);
    }


    public static DrawerBuilder CreateDefaultBuilderSettings(final MainActivity activity, SharedPreferences sharedPreferences, Toolbar toolbar, final AccountHeader accountHeader) {
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(activity);

        if (toolbar != null) {
            builder.withToolbar(toolbar);
        }

        builder.withHeaderDivider(false)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tasks)).withIdentifier(SIDEBAR_TASKS),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_skills)).withIdentifier(SIDEBAR_SKILLS),

                        new SectionIconDrawerItem().withName(activity.getString(R.string.sidebar_section_social).toUpperCase(Locale.getDefault())),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_inbox)).withIdentifier(SIDEBAR_INBOX),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tavern)).withIdentifier(SIDEBAR_TAVERN),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_party)).withIdentifier(SIDEBAR_PARTY),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_guilds)).withIdentifier(SIDEBAR_GUILDS),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_challenges)).withIdentifier(SIDEBAR_CHALLENGES),

                        new SectionIconDrawerItem().withName(activity.getString(R.string.sidebar_section_inventory).toUpperCase(Locale.getDefault())),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_shops)).withIdentifier(SIDEBAR_SHOPS),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_avatar)).withIdentifier(SIDEBAR_AVATAR),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_equipment)).withIdentifier(SIDEBAR_EQUIPMENT),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_items)).withIdentifier(SIDEBAR_ITEMS),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_stable)).withIdentifier(SIDEBAR_STABLE),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_purchaseGems)).withIdentifier(SIDEBAR_PURCHASE),

                        new SectionIconDrawerItem().withName(activity.getString(R.string.sidebar_about).toUpperCase(Locale.getDefault())),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_news)).withIdentifier(SIDEBAR_NEWS).withSelectable(false),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_settings)).withIdentifier(SIDEBAR_SETTINGS).withSelectable(false),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_help)).withIdentifier(SIDEBAR_HELP),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_about)).withIdentifier(SIDEBAR_ABOUT).withSelectable(false)

                )
                .withStickyFooterDivider(false)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    BaseMainFragment fragment = null;
                    Class newActivityClass = null;

                    int identifier = (int) drawerItem.getIdentifier();
                    switch (identifier) {
                        case SIDEBAR_TASKS: {
                            fragment = new TasksFragment();
                            break;
                        }
                        case SIDEBAR_SKILLS: {
                            fragment = new SkillsFragment();
                            break;
                        }
                        case SIDEBAR_INBOX: {
                            fragment = new InboxFragment();
                            break;
                        }
                        case SIDEBAR_PARTY: {
                            fragment = new PartyFragment();
                            break;
                        }
                        case SIDEBAR_GUILDS: {
                            fragment = new GuildsOverviewFragment();
                            break;
                        }
                        case SIDEBAR_TAVERN: {
                            fragment = new TavernFragment();
                            break;
                        }
                        case SIDEBAR_CHALLENGES: {
                            fragment = new ChallengesOverviewFragment();

                            break;
                        }
                        case SIDEBAR_SHOPS: {
                            fragment = new ShopsFragment();
                            break;
                        }
                        case SIDEBAR_AVATAR: {
                            fragment = new AvatarOverviewFragment();
                            break;
                        }
                        case SIDEBAR_EQUIPMENT: {
                            fragment = new EquipmentOverviewFragment();
                            break;
                        }
                        case SIDEBAR_ITEMS: {
                            fragment = new ItemsFragment();
                            break;
                        }
                        case SIDEBAR_STABLE: {
                            fragment = new StableFragment();
                            break;
                        }
                        case SIDEBAR_PURCHASE: {
                            newActivityClass = GemPurchaseActivity.class;
                            break;
                        }
                        case SIDEBAR_NEWS: {
                            fragment = new NewsFragment();
                            break;
                        }
                        case SIDEBAR_SETTINGS: {
                            newActivityClass = PrefsActivity.class;
                            break;
                        }
                        case SIDEBAR_HELP: {
                            fragment = new FAQOverviewFragment();
                            break;
                        }
                        case SIDEBAR_ABOUT: {
                            newActivityClass = AboutActivity.class;
                            break;
                        }
                    }

                    sharedPreferences.edit().putInt("lastActivePosition", position).apply();

                    if (fragment != null) {
                        fragment.fragmentSidebarPosition = position;
                        activity.displayFragment(fragment);
                        return false;
                    }
                    if (newActivityClass != null) {
                        Intent passUserId = new Intent(activity, newActivityClass);
                        passUserId.putExtra("userId", activity.getUserID());
                        if (identifier == SIDEBAR_PURCHASE) {
                            activity.startActivityForResult(passUserId, GEM_PURCHASE_REQUEST);
                        } else {
                            activity.startActivity(passUserId);
                        }
                        return false;
                    }


                    return true;
                });

        return builder;
    }
}
