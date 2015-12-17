package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.porokoro.paperboy.ItemTypeBuilder;
import com.github.porokoro.paperboy.PaperboyBuilder;
import com.github.porokoro.paperboy.ViewTypes;
import com.habitrpg.android.habitica.ui.fragments.AboutFragment;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.pager)
    ViewPager pager;

    @Bind(R.id.tab_layout)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.about_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setElevation(0);
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), 3);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        pager.setOffscreenPageLimit(1);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.setTabsFromPagerAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:

                    return new AboutFragment();
                case 1:
                    return new LibsBuilder()
                            //Pass the fields of your application to the lib so it can find all external lib information
                            .withFields(R.string.class.getFields())
                            .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                            .withAboutAppName(getString(R.string.app_name))
                            .withAboutDescription("<h2>Used Libraries</h2>")
                            .withAboutIconShown(true)
                            .withAboutVersionShown(true)
                            .withAboutVersionShownCode(true)
                            .withAboutVersionShownName(true)
                            .withSlideInAnimation(true)
                            .supportFragment();
                case 2:
                    PaperboyBuilder builder = new PaperboyBuilder(AboutActivity.this)
                            .setViewType(ViewTypes.HEADER)
                            .setFile("paperboy/changelog.json");


                    builder.addItemType(new ItemTypeBuilder(AboutActivity.this, 1000, "Note", "n")
                            .setColorRes(R.color.changelog_note)
                            .setTitleSingular("Note")
                            .setTitlePlural("Notes")
                            .setSortOrder(0)
                            .build());

                    return builder.buildFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.about_title);
            } else if (position == 1) {
                return getString(R.string.about_libraries);
            }


            return getString(R.string.about_versionhistory);
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}
