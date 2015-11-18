package com.habitrpg.android.habitica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.habitrpg.android.habitica.userpicture.UserPictureRunnable;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GemPurchaseActivity extends AppCompatActivity {

    // region View Elements

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    Drawer drawer;
    AccountHeader accountHeader;
    // endregion

    // region IAP

    @NonNull
    private final ActivityCheckout checkout = Checkout.forActivity(this, HabiticaApplication.Instance.getCheckout());

    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gem_purchase);

        // Inject Controls
        ButterKnife.inject(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {

                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }

            actionBar.setTitle("Purchase Gems");
        }

        accountHeader = MainDrawerBuilder.CreateDefaultAccountHeader(this).build();
        Drawer drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar, accountHeader)
                .withSelectedItem(2)
                .build();

        HostConfig hostConfig = PrefsActivity.fromContext(this);
        HabitRPGUser user = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).querySingle();

        final IProfile profile = accountHeader.getProfiles().get(0);
        profile.withName(user.getProfile().getName());
        new UserPicture(user, this, true, false).setPictureWithRunnable(new UserPictureRunnable() {
            public void run(Bitmap avatar) {
                profile.withIcon(avatar);
                accountHeader.updateProfile(profile);
            }
        });
        accountHeader.updateProfile(profile);

        checkout.start();
        // you only need this if this activity starts purchase process
        checkout.createPurchaseFlow(new RequestListener<Purchase>() {
            @Override
            public void onSuccess(Purchase purchase) {

            }

            @Override
            public void onError(int i, Exception e) {

            }
        });


        checkout.whenReady(new Checkout.Listener() {
            @Override
            public void onReady(BillingRequests billingRequests) {
                billingRequests.purchase(ProductTypes.IN_APP, HabiticaApplication.Purchase20Gems, null, checkout.getPurchaseFlow());
            }

            @Override
            public void onReady(BillingRequests billingRequests, String s, boolean b) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        checkout.stop();
        super.onDestroy();
    }
}
