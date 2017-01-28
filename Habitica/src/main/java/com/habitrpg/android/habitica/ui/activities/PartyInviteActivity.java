package com.habitrpg.android.habitica.ui.activities;

import com.magicmicky.habitrpgwrapper.lib.api.IApiClient;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.prefs.scanner.IntentIntegrator;
import com.habitrpg.android.habitica.prefs.scanner.IntentResult;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;

public class PartyInviteActivity extends BaseActivity {

    public static final int RESULT_SEND_INVITES = 100;
    public static final String USER_IDS_KEY = "userIDs";
    public static final String IS_EMAIL_KEY = "isEmail";
    public static final String EMAILS_KEY = "emails";

    private HabitRPGUser user;
    private String userIdToInvite;

    @Inject
    IApiClient apiClient;

    @Inject
    protected HostConfig hostConfig;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    List<PartyInviteFragment> fragments = new ArrayList<>();

    private TransactionListener<HabitRPGUser> userTransactionListener = new TransactionListener<HabitRPGUser>() {
        @Override
        public void onResultReceived(HabitRPGUser habitRPGUser) {
            handleUserRecieved(habitRPGUser);
        }

        @Override
        public boolean onReady(BaseTransaction<HabitRPGUser> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<HabitRPGUser> baseTransaction, HabitRPGUser habitRPGUser) {
            return true;
        }
    };

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
    protected void injectActivity(AppComponent component) {
        component.inject(this);
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
            setResult(Activity.RESULT_OK, createResultIntent());
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createResultIntent() {
        Intent intent = new Intent();
        PartyInviteFragment fragment = fragments.get(viewPager.getCurrentItem());
        if (viewPager.getCurrentItem() == 0) {
            intent.putExtra(PartyInviteActivity.IS_EMAIL_KEY, true);
            intent.putExtra(PartyInviteActivity.EMAILS_KEY, fragment.getValues());
        } else {
            intent.putExtra(PartyInviteActivity.IS_EMAIL_KEY, false);
            intent.putExtra(PartyInviteActivity.USER_IDS_KEY, fragment.getValues());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanningResult != null && scanningResult.getContents() != null) {
            String qrCodeUrl = scanningResult.getContents();
            Uri uri = Uri.parse(qrCodeUrl);
            if (uri == null || uri.getPathSegments().size() < 3) {
                return;
            }
            userIdToInvite = uri.getPathSegments().get(2);

            //@TODO: Move to user helper/model
            new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);
        }
    }

    public void handleUserRecieved(HabitRPGUser user) {
        this.user = user;

        if (this.userIdToInvite == null) {
            return;
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                "Invited: " + userIdToInvite, Toast.LENGTH_LONG);
        toast.show();

        Map<String, Object> inviteData = new HashMap<>();
        List<String> invites = new ArrayList<>();
        invites.add(userIdToInvite);
        inviteData.put("uuids", invites);

        this.apiClient.inviteToGroup(this.user.getParty().getId(), inviteData)
                .subscribe(aVoid -> {}, throwable -> {});
    }
}
