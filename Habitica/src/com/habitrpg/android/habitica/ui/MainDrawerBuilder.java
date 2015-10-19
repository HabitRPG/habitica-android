package com.habitrpg.android.habitica.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.habitrpg.android.habitica.AboutActivity;
import com.habitrpg.android.habitica.GemPurchaseActivity;
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

import java.util.HashMap;


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

    private static HashMap<Integer, Class> ClassMap = new HashMap<>();

    static {
        ClassMap.put(SIDEBAR_TASKS, MainActivity.class);
        ClassMap.put(SIDEBAR_TAVERN, TavernActivity.class);
        ClassMap.put(SIDEBAR_PARTY, PartyActivity.class);
        ClassMap.put(SIDEBAR_PURCHASE, GemPurchaseActivity.class);
        ClassMap.put(SIDEBAR_SETTINGS, PrefsActivity.class);
        ClassMap.put(SIDEBAR_ABOUT, AboutActivity.class);
    }

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
                        new PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_purchaseGems)).withIdentifier(SIDEBAR_PURCHASE),
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

                        Class selectedClass = ClassMap.get(drawerItem.getIdentifier());

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

        Class activityClass = activity.getClass();
        for (java.util.Map.Entry<Integer,Class> entry: ClassMap.entrySet()) {
            if(entry.getValue() == activityClass){
                builder.withSelectedItem(entry.getKey());
            }
        }


        return builder;
    }
}
