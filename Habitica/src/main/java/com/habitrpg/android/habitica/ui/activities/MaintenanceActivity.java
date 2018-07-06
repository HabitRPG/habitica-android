package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.api.MaintenanceApiService;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import net.pherth.android.emoji_library.EmojiTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class MaintenanceActivity extends BaseActivity {

    @Inject
    public MaintenanceApiService maintenanceService;

    @Inject
    public ApiClient apiClient;

    @BindView(R.id.titleTextView)
    TextView titleTextView;
    @BindView(R.id.imageView)
    SimpleDraweeView imageView;
    @BindView(R.id.descriptionTextView)
    EmojiTextView descriptionTextView;
    @BindView(R.id.playStoreButton)
    Button playStoreButton;
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

        imageView.setImageURI(Uri.parse(data.getString("imageUrl")));
        this.descriptionTextView.setText(MarkdownParser.INSTANCE.parseMarkdown(data.getString("description")));
        this.descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());

        isDeprecationNotice = data.getBoolean("deprecationNotice");
        if (isDeprecationNotice) {
            this.playStoreButton.setVisibility(View.VISIBLE);
        } else {
            this.playStoreButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isDeprecationNotice) {
            this.maintenanceService.getMaintenanceStatus()
                    .compose(apiClient.configureApiCallObserver())
                    .subscribe(maintenanceResponse -> {
                        if (!maintenanceResponse.activeMaintenance) {
                            finish();
                        }
                    }, RxErrorHandler.handleEmptyError());
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
