package com.habitrpg.android.habitica.ui.activities;

import com.github.data5tream.emojilib.EmojiTextView;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;

public class MaintenanceActivity extends BaseActivity {

    @BindView(R.id.titleTextView)
    TextView titleTextView;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.descriptionTextView)
    EmojiTextView descriptionTextView;

    @BindView(R.id.playStoreButton)
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
            this.apiHelper.maintenanceService.getMaintenanceStatus()
                    .compose(this.apiHelper.configureApiCallObserver())
                    .subscribe(maintenanceResponse -> {
                        if (!maintenanceResponse.activeMaintenance) {
                            finish();
                        }
                    }, throwable -> {});
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
