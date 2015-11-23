package com.habitrpg.android.habitica.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.habitrpg.android.habitica.AboutActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.PartyFragment;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.TasksFragment;
import com.habitrpg.android.habitica.ui.fragments.TavernFragment;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;


/**
 * Created by Negue on 18.08.2015.
 */
public class MainDrawerBuilder {

    // Change the identificationIDs to the position IDs so that its easier to set the selected entry
    static final int SIDEBAR_TASKS = 0;
    static final int SIDEBAR_TAVERN = 2;
    static final int SIDEBAR_PARTY = 3;
    static final int SIDEBAR_PURCHASE = 4;
    static final int SIDEBAR_SETTINGS = 6;
    static final int SIDEBAR_ABOUT = 7;



    public static AccountHeaderBuilder CreateDefaultAccountHeader(final Activity activity) {
        AccountHeaderBuilder builder = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.sidebar_background)
                .addProfiles(
                        new ProfileDrawerItem()
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .withSelectionListEnabledForSingleProfile(false);
        return builder;
    }


    public static DrawerBuilder CreateDefaultBuilderSettings(final MainActivity activity, Toolbar toolbar, AccountHeader accountHeader) {
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(activity);

        if (toolbar != null) {
            builder.withToolbar(toolbar);
        }

        builder.withHeaderDivider(false)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tasks)).withIdentifier(SIDEBAR_TASKS),

                        new SectionDrawerItem().withName(activity.getString(R.string.sidebar_section_social)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tavern)).withIdentifier(SIDEBAR_TAVERN),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_party)).withIdentifier(SIDEBAR_PARTY),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_purchaseGems)).withIdentifier(SIDEBAR_PURCHASE),
                        /*new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_guilds)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_challenges)),

                        new SectionDrawerItem().withName(activity.getString(R.string.sidebar_section_inventory)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_avatar)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_equipment)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_stable)),*/

                        new DividerDrawerItem(),
                        //new SecondaryDrawerItem().withName(activity.getString(R.string.sidebar_news)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_settings)).withIdentifier(SIDEBAR_SETTINGS),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_about)).withIdentifier(SIDEBAR_ABOUT)

                )
                .withStickyFooterDivider(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        BaseFragment fragment = null;
                        Class newActivityClass = null;

                        switch (drawerItem.getIdentifier()) {
                            case SIDEBAR_TASKS: {
                                fragment = new TasksFragment();
                                break;
                            }
                            case SIDEBAR_PARTY: {
                                fragment = new PartyFragment();
                                break;
                            }
                            case SIDEBAR_TAVERN: {
                                fragment = new TavernFragment();
                                break;
                            }
                            case SIDEBAR_SETTINGS: {
                                newActivityClass = PrefsActivity.class;
                                break;
                            }
                            case SIDEBAR_ABOUT: {
                                newActivityClass = AboutActivity.class;
                                break;
                            }
                        }

                        if (fragment != null) {
                            fragment.fragmentSidebarPosition = position;
                            activity.displayFragment(fragment);
                            return false;
                        }
                        if (newActivityClass != null) {
                            activity.startActivity(new Intent(activity, newActivityClass));
                            return false;
                        }


                        return true;
                    }
                });

        return builder;
    }
}
