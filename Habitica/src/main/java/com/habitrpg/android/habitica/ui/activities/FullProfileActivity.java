package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.SendNewInboxMessageCommand;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.fragments.social.InboxMessageListFragment;
import com.habitrpg.android.habitica.ui.helpers.EmojiKeyboard;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Buffs;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Outfit;
import com.magicmicky.habitrpgwrapper.lib.models.Profile;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;

import net.pherth.android.emoji_library.EmojiEditText;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class FullProfileActivity extends BaseActivity {
    private String userId;
    private String userName;
    private ContentCache contentCache;

    @Inject
    APIHelper apiHelper;

    @BindView(R.id.profile_image)
    SimpleDraweeView profile_image;

    @BindView(R.id.profile_blurb)
    TextView blurbTextView;

    @BindView(R.id.avatarView)
    AvatarView avatarView;

    @BindView(R.id.attributes_table)
    TableLayout attributesTableLayout;

    @BindView(R.id.equipment_table)
    TableLayout equipmentTableLayout;

    @BindView(R.id.costume_table)
    TableLayout costumeTableLayout;

    @BindView(R.id.avatar_attributes_progress)
    ProgressBar attributesProgress;

    @BindView(R.id.avatar_equip_progress)
    ProgressBar equipmentProgress;

    @BindView(R.id.avatar_costume_progress)
    ProgressBar costumeProgress;

    @BindView(R.id.avatar_with_bars)
    View avatar_with_bars;
    private AvatarWithBarsViewModel avatarWithBars;

    @BindView(R.id.fullprofile_scrollview)
    ScrollView fullprofile_scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.userId = bundle.getString("userId");

        setTitle("loading member data..");

        apiHelper.apiService.GetMember(this.userId)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(habitRPGUser -> updateView(habitRPGUser),
                        throwable -> {
                        });

        this.contentCache = new ContentCache(apiHelper.apiService, apiHelper.languageCode);

        avatarWithBars = new AvatarWithBarsViewModel(this, avatar_with_bars);
        avatarWithBars.hideGems();
        avatarWithBars.valueBarLabelsToBlack();

        avatar_with_bars.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();

        if (id == R.id.private_message) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View newMessageView = factory.inflate(
                    R.layout.fullprofile_new_message_dialog, null);

            EmojiEditText emojiEditText = (EmojiEditText)newMessageView.findViewById(R.id.edit_new_message_text);

            TextView newMessageTitle = (TextView)newMessageView.findViewById(R.id.new_message_title);
            newMessageTitle.setText("Send message to "+userName);

            final AlertDialog addMessageDialog = new AlertDialog.Builder(this)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        HashMap<String, String> messageObject = new HashMap<>();
                        messageObject.put("message", emojiEditText.getText().toString());
                        messageObject.put("toUserId", userId);

                        apiHelper.apiService.postPrivateMessage(messageObject)
                                .compose(apiHelper.configureApiCallObserver())
                                .subscribe(postChatMessageResult -> {
                                    UiUtils.showSnackbar(FullProfileActivity.this, FullProfileActivity.this.fullprofile_scrollview,
                                            "Message sent to "+userName, UiUtils.SnackbarDisplayType.NORMAL);
                                }, throwable -> {
                                });

                        UiUtils.dismissKeyboard(HabiticaApplication.currentActivity);
                    })
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {

                        UiUtils.dismissKeyboard(HabiticaApplication.currentActivity);
                    })

                    .create();

            addMessageDialog.setView(newMessageView);

            addMessageDialog.show();

            return true;
        }

        if (id == android.R.id.home) {
            // app icon in action bar clicked; goto parent activity.
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateView(HabitRPGUser user) {
        Profile profile = user.getProfile();
        Stats stats = user.getStats();

        userName = profile.getName();

        setTitle(profile.getName());

        String imageUrl = profile.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            profile_image.setVisibility(View.GONE);
        } else {
            profile_image.setController(Fresco.newDraweeControllerBuilder()
                    .setUri(imageUrl)
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFailure(String id, Throwable throwable) {
                            profile_image.setVisibility(View.GONE);
                        }
                    })
                    .build());
        }

        String blurbText = profile.getBlurb();
        if (blurbText != null && !blurbText.isEmpty()) {
            blurbTextView.setText(blurbText);
        }

        avatarView.setUser(user);
        avatarWithBars.updateData(user);

        addLevelAttributes(stats, user);
    }

    // region Utils

    private void stopAndHideProgress(ProgressBar bar) {
        bar.setIndeterminate(false);
        bar.setVisibility(View.GONE);
    }

    private String getCeiledValue(float val) {
        return ((int) Math.ceil(val)) + "";
    }

    private TableRow addEquipmentRow(TableLayout table, String gearKey, String text, String stats) {
        TableRow gearRow = (TableRow) getLayoutInflater().inflate(R.layout.fullprofile_gear_tablerow, null);

        SimpleDraweeView draweeView = (SimpleDraweeView) gearRow.findViewById(R.id.gear_drawee);

        draweeView.setController(Fresco.newDraweeControllerBuilder()
                .setUri(AvatarView.IMAGE_URI_ROOT + "shop_" + gearKey + ".png")
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        draweeView.setVisibility(View.GONE);
                    }
                })
                .build());

        TextView keyTextView = (TextView) gearRow.findViewById(R.id.tableRowTextView1);
        keyTextView.setText(text);

        table.addView(gearRow);

        TextView valueTextView = (TextView) gearRow.findViewById(R.id.tableRowTextView2);

        if (!stats.isEmpty()) {
            valueTextView.setText(stats);
        } else {
            valueTextView.setVisibility(View.GONE);
        }

        return gearRow;
    }

    // endregion

    // region Stats

    private float attributeStrSum = 0;
    private float attributeIntSum = 0;
    private float attributeConSum = 0;
    private float attributePerSum = 0;

    private void addLevelAttributes(Stats stats, HabitRPGUser user) {
        float byLevelStat = stats.getLvl() / 2.0f;

        addAttributeRow("Level: ", byLevelStat, byLevelStat, byLevelStat, byLevelStat, false);

        LoadItemDataByOutfit(user.getItems().getGear().getEquipped(), obj -> {
            GotGear(obj, user);
            addNormalAddBuffAttributes(stats);

            stopAndHideProgress(attributesProgress);
        });

        LoadItemDataByOutfit(user.getItems().getGear().getCostume(), obj -> {
            GotCostume(obj);
        });
    }

    private void LoadItemDataByOutfit(Outfit outfit, ContentCache.GotContentEntryCallback<List<ItemData>> gotEntries) {
        ArrayList<String> outfitList = new ArrayList<>();
        outfitList.add(outfit.getArmor());
        outfitList.add(outfit.getBack());
        outfitList.add(outfit.getBody());
        outfitList.add(outfit.getEyeWear());
        outfitList.add(outfit.getHead());
        outfitList.add(outfit.getHeadAccessory());
        outfitList.add(outfit.getShield());
        outfitList.add(outfit.getWeapon());

        contentCache.GetItemDataList(outfitList, gotEntries);
    }

    public void GotGear(List<ItemData> obj, HabitRPGUser user) {
        float strAttributes = 0;
        float intAttributes = 0;
        float conAttributes = 0;
        float perAttributes = 0;

        // Summarize stats and fill equipment table
        for (ItemData i : obj) {
            int str_ = (int) i.getStr();
            int int_ = (int) i.get_int();
            int con_ = (int) i.getCon();
            int per_ = (int) i.getPer();

            strAttributes += str_;
            intAttributes += int_;
            conAttributes += con_;
            perAttributes += per_;

            StringBuilder sb = new StringBuilder();

            if (str_ != 0) {
                sb.append("STR " + str_ + ", ");
            }
            if (int_ != 0) {
                sb.append("INT " + int_ + ", ");
            }
            if (con_ != 0) {
                sb.append("CON " + con_ + ", ");
            }
            if (per_ != 0) {
                sb.append("PER " + per_ + ", ");
            }

            // remove the last comma
            if (sb.length() > 2) {
                sb.delete(sb.length() - 2, sb.length());
            }

            addEquipmentRow(equipmentTableLayout, i.getKey(), i.getText(), sb.toString());
        }

        stopAndHideProgress(equipmentProgress);
        equipmentTableLayout.setVisibility(View.VISIBLE);

        addAttributeRow("Equipment: ", strAttributes, intAttributes, conAttributes, perAttributes, false);

        if (!user.getPreferences().isDisableClasses()) {
            float strClassBonus = 0;
            float intClassBonus = 0;
            float conClassBonus = 0;
            float perClassBonus = 0;

            switch (user.getStats().get_class()) {
                case rogue:
                    strClassBonus = strAttributes * 0.5f;
                    perClassBonus = perAttributes * 0.5f;
                    break;
                case healer:
                    conClassBonus = conAttributes * 0.5f;
                    intClassBonus = intClassBonus * 0.5f;
                    break;
                case warrior:
                    strClassBonus = strAttributes * 0.5f;
                    conClassBonus = conAttributes * 0.5f;
                    break;
                case wizard:
                    intClassBonus = intClassBonus * 0.5f;
                    perClassBonus = perAttributes * 0.5f;
                    break;
            }

            addAttributeRow("Class-Bonus: ", strClassBonus, intClassBonus, conClassBonus, perClassBonus, false);
        }
    }

    public void GotCostume(List<ItemData> obj) {
        // fill costume table
        for (ItemData i : obj) {
            addEquipmentRow(costumeTableLayout, i.getKey(), i.getText(), "");
        }

        stopAndHideProgress(costumeProgress);
    }


    private void addNormalAddBuffAttributes(Stats stats) {
        Buffs buffs = stats.getBuffs();

        addAttributeRow("Allocated: ", stats.getStr(), stats.get_int(), stats.getCon(), stats.getPer(), false);
        addAttributeRow("Boosts: ", buffs.getStr(), buffs.get_int(), buffs.getCon(), buffs.getPer(), false);

        addAttributeRow("", attributeStrSum, attributeIntSum, attributeConSum, attributePerSum, true);
    }

    private TableRow addAttributeRow(String label, float strVal, float intVal, float conVal, float perVal, boolean isSummary) {
        TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.fullprofile_attributetablerow, null);
        TextView keyTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_type);
        keyTextView.setText(label);

        TextView strTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_str);
        strTextView.setText(getCeiledValue(strVal));

        TextView intTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_int);
        intTextView.setText(getCeiledValue(intVal));

        TextView conTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_con);
        conTextView.setText(getCeiledValue(conVal));

        TextView perTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_per);
        perTextView.setText(getCeiledValue(perVal));


        if (isSummary) {
            strTextView.setTypeface(null, Typeface.BOLD);
            intTextView.setTypeface(null, Typeface.BOLD);
            conTextView.setTypeface(null, Typeface.BOLD);
            perTextView.setTypeface(null, Typeface.BOLD);
        } else {
            attributeStrSum += strVal;
            attributeIntSum += intVal;
            attributeConSum += conVal;
            attributePerSum += perVal;
        }

        attributesTableLayout.addView(tableRow);

        return tableRow;
    }

    // endregion

    // region Navigation

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // endregion

    // region BaseActivity-Overrides

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_full_profile;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_profile, menu);
        return true;
    }

    // endregion
}
