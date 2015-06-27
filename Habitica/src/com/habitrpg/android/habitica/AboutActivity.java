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

import com.github.porokoro.paperboy.PaperboyFragment;
import com.github.porokoro.paperboy.Themes;
import com.github.porokoro.paperboy.ViewTypes;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AboutActivity extends AppCompatActivity   {

    @InjectView(R.id.pager)
    ViewPager pager;

    @InjectView(R.id.tab_layout)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.inject(this);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.about_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), 2);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

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
                    Fragment tab1 =    new LibsBuilder()
                            //Pass the fields of your application to the lib so it can find all external lib information
                            .withFields(R.string.class.getFields())
                            //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                            .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                            .withAboutAppName(getString(R.string.app_name))
                            .withAboutDescription("<h2>Used Libraries</h2>")
                            .withAboutIconShown(true)
                            .withAboutVersionShown(true)
                            .withAboutVersionShownCode(true)
                            .withAboutVersionShownName(true)
                            .withAnimations(true)
                            .fragment();

                    return tab1;
                case 1:
                    PaperboyFragment tab2 = new PaperboyFragment.Builder(AboutActivity.this)
                        .setTheme(Themes.DARK)
                        .setViewType(ViewTypes.HEADER)
                        .setFile("paperboy/changelog.json")
                        .build();

                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return getString(R.string.about_title);

            return getString(R.string.about_changelog);
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}
