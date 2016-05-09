package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.StableFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.StableRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PartyInviteActivity extends BaseActivity {

    public static final int RESULT_SEND_INVITES = 100;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    List<PartyInviteFragment> fragments = new ArrayList<>();

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_party_invite;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPager.setCurrentItem(0);

        setViewPagerAdapter();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_party_invite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_invites) {
            setResult(RESULT_SEND_INVITES, createResultIntent());
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createResultIntent() {
        Intent intent = new Intent();
        PartyInviteFragment fragment = fragments.get(viewPager.getCurrentItem());
        if (viewPager.getCurrentItem() == 0) {
            intent.putExtra("isEmail", true);
            intent.putExtra("emails", fragment.getValues());
        } else {
            intent.putExtra("isEmail", false);
            intent.putExtra("userIds", fragment.getValues());
        }
        return intent;
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                PartyInviteFragment fragment = new PartyInviteFragment();
                fragment.isEmailInvite = position == 0;
                if (fragments.size() > position) {
                    fragments.set(position, fragment);
                } else {
                    fragments.add(fragment);
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.by_email);
                    case 1:
                        return getString(R.string.invite_existing_users);
                }
                return "";
            }
        });

        if (tabLayout != null && viewPager != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }
}
