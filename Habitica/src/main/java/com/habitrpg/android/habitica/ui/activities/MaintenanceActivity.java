package com.habitrpg.android.habitica.ui.activities;

import com.github.data5tream.emojilib.EmojiTextView;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.magicmicky.habitrpgwrapper.lib.models.responses.MaintenanceResponse;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MaintenanceActivity extends BaseActivity {

    @Bind(R.id.titleTextView)
    TextView titleTextView;

    @Bind(R.id.imageView)
    ImageView imageView;

    @Bind(R.id.descriptionTextView)
    EmojiTextView descriptionTextView;

    @Bind(R.id.playStoreButton)
    Button playStoreButton;
    private APIHelper apiHelper;
    private Boolean isDeprecationNotice;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_maintenance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle data = getIntent().getExtras();

        this.titleTextView.setText(data.getString("title"));
        Picasso.with(this).load(data.getString("imageUrl")).into(this.imageView);
        this.descriptionTextView.setText(MarkdownParser.parseMarkdown(data.getString("description")));
        this.descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());

        isDeprecationNotice = data.getBoolean("deprecationNotice");
        if (isDeprecationNotice) {
            this.playStoreButton.setVisibility(View.VISIBLE);
        } else {
            this.playStoreButton.setVisibility(View.GONE);
        }
        HostConfig hostConfig = PrefsActivity.fromContext(this);
        apiHelper = new APIHelper(hostConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isDeprecationNotice) {
            this.apiHelper.maintenanceService.getMaintenanceStatus(new Callback<MaintenanceResponse>() {
                @Override
                public void success(MaintenanceResponse maintenanceResponse, Response response) {
                    if (!maintenanceResponse.activeMaintenance) {
                        finish();
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    @OnClick(R.id.playStoreButton)
    public void openInPlayStore() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
