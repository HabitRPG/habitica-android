package com.habitrpg.android.habitica.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.habitrpg.android.habitica.AboutActivity;
import com.habitrpg.android.habitica.MainActivity;
import com.habitrpg.android.habitica.PartyActivity;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.TavernActivity;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


/**
 * Created by Negue on 18.08.2015.
 */
public class MainDrawerBuilder {

    static final int SIDEBAR_TASKS = 1;
    static final int SIDEBAR_TAVERN = 2;
    static final int SIDEBAR_PARTY = 3;
    static final int SIDEBAR_SETTINGS = 11;
    static final int SIDEBAR_ABOUT = 12;

    public static DrawerBuilder CreateDefaultBuilderSettings(final Activity activity, Toolbar toolbar) {
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(activity);

        if (toolbar != null) {
            builder.withToolbar(toolbar);
        }

        builder.withHeaderDivider(false)
                .withAnimateDrawerItems(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tasks)).withIdentifier(SIDEBAR_TASKS),

                        new SectionDrawerItem().withName(activity.getString(R.string.sidebar_section_social)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tavern)).withIdentifier(SIDEBAR_TAVERN),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_party)).withIdentifier(SIDEBAR_PARTY),
                        /*new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_guilds)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_challenges)),

                        new SectionDrawerItem().withName(activity.getString(R.string.sidebar_section_inventory)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_avatar)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_equipment)),
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_stable)),*/

                        new DividerDrawerItem(),
                        //new SecondaryDrawerItem().withName(activity.getString(R.string.sidebar_news)),
                        new SecondaryDrawerItem().withName(activity.getString(R.string.sidebar_settings)).withIdentifier(SIDEBAR_SETTINGS),
                        new SecondaryDrawerItem().withName(activity.getString(R.string.sidebar_about)).withIdentifier(SIDEBAR_ABOUT)

                )
                .withStickyFooterDivider(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        Class selectedClass = null;

                        switch (drawerItem.getIdentifier()) {
                            case SIDEBAR_TASKS: {
                                selectedClass = MainActivity.class;
                                break;
                            }
                            case SIDEBAR_TAVERN: {
                                selectedClass = TavernActivity.class;
                                break;
                            }
                            case SIDEBAR_PARTY: {
                                selectedClass = PartyActivity.class;
                                break;
                            }
                            case SIDEBAR_SETTINGS: {
                                selectedClass = PrefsActivity.class;
                                break;
                            }
                            case SIDEBAR_ABOUT: {
                                selectedClass = AboutActivity.class;
                                break;
                            }
                        }

                        if (selectedClass != null && activity.getClass() != selectedClass) {
                            activity.startActivity(new Intent(activity, selectedClass));
                            return false;
                        } else if (selectedClass != null) {
                            //same item was clicked again
                            return false;
                        }


                        return true;
                    }
                });

        return builder;
    }
}
