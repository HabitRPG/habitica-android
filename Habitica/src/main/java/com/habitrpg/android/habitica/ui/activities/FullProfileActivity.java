package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.helpers.UserStatComputer;
import com.habitrpg.android.habitica.models.Achievement;
import com.habitrpg.android.habitica.models.AchievementGroup;
import com.habitrpg.android.habitica.models.AchievementResult;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.user.Buffs;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Profile;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.adapter.social.AchievementAdapter;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;

import net.pherth.android.emoji_library.EmojiEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.realm.RealmResults;
import rx.Observable;

public class FullProfileActivity extends BaseActivity {
    @Inject
    InventoryRepository inventoryRepository;
    @Inject
    ApiClient apiClient;
    @Inject
    SocialRepository socialRepository;
    @BindView(R.id.profile_image)
    SimpleDraweeView profile_image;
    @BindView(R.id.profile_blurb)
    TextView blurbTextView;
    @BindView(R.id.avatarView)
    AvatarView avatarView;
    @BindView(R.id.copy_userid)
    Button copyUserIdButton;
    @BindView(R.id.userid)
    TextView userIdText;
    @BindView(R.id.profile_attributes_card)
    CardView attributesCardView;
    @BindView(R.id.attributes_table)
    TableLayout attributesTableLayout;
    @BindView(R.id.attributes_collapse_icon)
    ImageView attributesCollapseIcon;
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
    @BindView(R.id.profile_costume_card)
    CardView costumeCard;
    @BindView(R.id.avatar_with_bars)
    View avatar_with_bars;
    @BindView(R.id.fullprofile_scrollview)
    ScrollView fullprofile_scrollview;
    @BindView(R.id.profile_pets_found_count)
    TextView petsFoundCount;
    @BindView(R.id.profile_mounts_tamed_count)
    TextView mountsTamedCount;
    @BindView(R.id.profile_achievements_card)
    CardView achievementCard;
    @BindView(R.id.avatar_achievements_progress)
    ProgressBar achievementProgress;
    @BindView(R.id.recyclerView)
    RecyclerView achievementGroupList;
    private String userId;
    private String userName;
    private AvatarWithBarsViewModel avatarWithBars;
    private float attributeStrSum = 0;
    private float attributeIntSum = 0;
    private float attributeConSum = 0;
    private float attributePerSum = 0;
    private boolean attributeDetailsHidden = true;
    private ArrayList<TableRow> attributeRows = new ArrayList<>();

    // region Utils

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.userId = bundle.getString("userId");

        setTitle(R.string.profile_loading_data);

        socialRepository.getMember(this.userId)
                .subscribe(this::updateView,
                        throwable -> {
                        });

        avatarWithBars = new AvatarWithBarsViewModel(this, avatar_with_bars);
        avatarWithBars.hideGems();
        avatarWithBars.valueBarLabelsToBlack();

        avatar_with_bars.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        attributeRows.clear();
        attributesCardView.setOnClickListener(view -> toggleAttributeDetails());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.private_message) {
            showSendMessageToUserDialog();

            return true;
        }

        if (id == android.R.id.home) {
            // app icon in action bar clicked; goto parent activity.
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSendMessageToUserDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View newMessageView = factory.inflate(
                R.layout.profile_new_message_dialog, null);

        EmojiEditText emojiEditText = (EmojiEditText) newMessageView.findViewById(R.id.edit_new_message_text);

        TextView newMessageTitle = (TextView) newMessageView.findViewById(R.id.new_message_title);
        newMessageTitle.setText(String.format(getString(R.string.profile_send_message_to), userName));

        final AlertDialog addMessageDialog = new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    socialRepository.postPrivateMessage(emojiEditText.getText().toString(), userId)
                            .subscribe(postChatMessageResult -> UiUtils.showSnackbar(FullProfileActivity.this, FullProfileActivity.this.fullprofile_scrollview,
                                    String.format(getString(R.string.profile_message_sent_to), userName), UiUtils.SnackbarDisplayType.NORMAL), throwable -> {
                            });

                    UiUtils.dismissKeyboard(HabiticaApplication.currentActivity);
                })
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> UiUtils.dismissKeyboard(HabiticaApplication.currentActivity))

                .create();

        addMessageDialog.setView(newMessageView);

        addMessageDialog.show();
    }

    private void updateView(User user) {
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
            blurbTextView.setText(MarkdownParser.parseMarkdown(blurbText));
        }
        userIdText.setText(userId);
        copyUserIdButton.setVisibility(View.VISIBLE);
        copyUserIdButton.setOnClickListener(view -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) view.getContext()
                    .getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText(
                            userId, userId);
            clipboard.setPrimaryClip(clip);
        });


        avatarView.setUser(user);
        avatarWithBars.updateData(user);

        addLevelAttributes(stats, user);

        petsFoundCount.setText(String.valueOf(user.getPetsFoundCount()));
        mountsTamedCount.setText(String.valueOf(user.getMountsTamedCount()));

        // Load the members achievements now
        apiClient.getMemberAchievements(this.userId)

                .subscribe(this::fillAchievements,
                        throwable -> {
                        });
    }

    // endregion

    // region Attributes

    private void fillAchievements(AchievementResult achievements) {
        List<Object> items = new ArrayList<>();

        fillAchievements(achievements.basic, items);
        fillAchievements(achievements.seasonal, items);
        fillAchievements(achievements.special, items);

        AchievementAdapter adapter = new AchievementAdapter();
        adapter.setItemList(items);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == 0) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        achievementGroupList.setLayoutManager(layoutManager);
        achievementGroupList.setAdapter(adapter);

        stopAndHideProgress(achievementProgress);
    }

    private void fillAchievements(AchievementGroup achievementGroup, List<Object> targetList) {
        // Order by ID first
        ArrayList<Achievement> achievementList = new ArrayList<>(achievementGroup.achievements.values());
        Collections.sort(achievementList, (achievement, t1) -> Double.compare(achievement.index, t1.index));

        targetList.add(achievementGroup.label);
        targetList.addAll(achievementList);
    }

    private void stopAndHideProgress(ProgressBar bar) {
        bar.setIndeterminate(false);
        bar.setVisibility(View.GONE);
    }

    private String getFloorValueString(float val, boolean roundDown) {
        return roundDown
                ? ((int) Math.floor(val)) + ""
                : (val == 0.0 ? "0" : val + "");
    }

    private float getFloorValue(float val, boolean roundDown) {
        return roundDown
                ? ((int) Math.floor(val))
                : val;
    }

    private TableRow addEquipmentRow(TableLayout table, String gearKey, String text, String stats) {
        TableRow gearRow = (TableRow) getLayoutInflater().inflate(R.layout.profile_gear_tablerow, table);

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

        TextView valueTextView = (TextView) gearRow.findViewById(R.id.tableRowTextView2);

        if (!stats.isEmpty()) {
            valueTextView.setText(stats);
        } else {
            valueTextView.setVisibility(View.GONE);
        }

        return gearRow;
    }

    private void addLevelAttributes(Stats stats, User user) {
        float byLevelStat = Math.min(stats.getLvl() / 2.0f, 50f);

        addAttributeRow(getString(R.string.profile_level), byLevelStat, byLevelStat, byLevelStat, byLevelStat, true, false);

        loadItemDataByOutfit(user.getItems().getGear().getEquipped()).subscribe(gear -> this.gotGear(gear, user), throwable -> {});

        if (user.getPreferences().getCostume()) {
            loadItemDataByOutfit(user.getItems().getGear().getCostume()).subscribe(this::gotCostume, throwable -> {});
        } else {
            costumeCard.setVisibility(View.GONE);
        }
    }

    private Observable<RealmResults<Equipment>> loadItemDataByOutfit(Outfit outfit) {
        ArrayList<String> outfitList = new ArrayList<>();
        outfitList.add(outfit.getArmor());
        outfitList.add(outfit.getBack());
        outfitList.add(outfit.getBody());
        outfitList.add(outfit.getEyeWear());
        outfitList.add(outfit.getHead());
        outfitList.add(outfit.getHeadAccessory());
        outfitList.add(outfit.getShield());
        outfitList.add(outfit.getWeapon());

        return inventoryRepository.getItems(outfitList);
    }

    public void gotGear(List<Equipment> equipmentList, User user) {
        UserStatComputer userStatComputer = new UserStatComputer();
        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);

        for (UserStatComputer.StatsRow row : statsRows) {
            if (row.getClass().equals(UserStatComputer.EquipmentRow.class)) {
                UserStatComputer.EquipmentRow equipmentRow = (UserStatComputer.EquipmentRow) row;
                addEquipmentRow(equipmentTableLayout, equipmentRow.gearKey, equipmentRow.text, equipmentRow.stats);
            } else if (row.getClass().equals(UserStatComputer.AttributeRow.class)) {
                UserStatComputer.AttributeRow attributeRow2 = (UserStatComputer.AttributeRow) row;
                addAttributeRow(getString(attributeRow2.labelId), attributeRow2.strVal, attributeRow2.intVal, attributeRow2.conVal, attributeRow2.perVal, attributeRow2.roundDown, attributeRow2.isSummary);
            }
        }

        stopAndHideProgress(equipmentProgress);
        equipmentTableLayout.setVisibility(View.VISIBLE);

        stopAndHideProgress(attributesProgress);
        attributesTableLayout.setVisibility(View.VISIBLE);
    }

    public void gotCostume(List<Equipment> obj) {
        // fill costume table
        for (Equipment i : obj) {
            addEquipmentRow(costumeTableLayout, i.getKey(), i.getText(), "");
        }

        stopAndHideProgress(costumeProgress);
    }

    private void addNormalAddBuffAttributes(Stats stats) {
        Buffs buffs = stats.getBuffs();

        addAttributeRow(getString(R.string.profile_allocated), stats.getStr(), stats.get_int(), stats.getCon(), stats.getPer(), true, false);
        addAttributeRow(getString(R.string.profile_boosts), buffs.getStr(), buffs.get_int(), buffs.getCon(), buffs.getPer(), true, false);

        // Summary row
        addAttributeRow("", attributeStrSum, attributeIntSum, attributeConSum, attributePerSum, false, true);
    }

    private TableRow addAttributeRow(String label, float strVal, float intVal, float conVal, float perVal, boolean roundDown, boolean isSummary) {
        TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.profile_attributetablerow, attributesTableLayout);
        TextView keyTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_type);
        keyTextView.setText(label);

        TextView strTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_str);
        strTextView.setText(getFloorValueString(strVal, roundDown));

        TextView intTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_int);
        intTextView.setText(getFloorValueString(intVal, roundDown));

        TextView conTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_con);
        conTextView.setText(getFloorValueString(conVal, roundDown));

        TextView perTextView = (TextView) tableRow.findViewById(R.id.tv_attribute_per);
        perTextView.setText(getFloorValueString(perVal, roundDown));


        if (isSummary) {
            strTextView.setTypeface(null, Typeface.BOLD);
            intTextView.setTypeface(null, Typeface.BOLD);
            conTextView.setTypeface(null, Typeface.BOLD);
            perTextView.setTypeface(null, Typeface.BOLD);
        } else {
            attributeStrSum += getFloorValue(strVal, roundDown);
            attributeIntSum += getFloorValue(intVal, roundDown);
            attributeConSum += getFloorValue(conVal, roundDown);
            attributePerSum += getFloorValue(perVal, roundDown);

            attributeRows.add(tableRow);
            tableRow.setVisibility(attributeDetailsHidden ? View.GONE : View.VISIBLE);
        }

        return tableRow;
    }

    private void toggleAttributeDetails() {
        attributeDetailsHidden = !attributeDetailsHidden;

        attributesCollapseIcon.setImageDrawable(ContextCompat.getDrawable(this, attributeDetailsHidden
                ? R.drawable.ic_keyboard_arrow_right_black_24dp
                : R.drawable.ic_keyboard_arrow_down_black_24dp));

        for (TableRow row : attributeRows) {
            row.setVisibility(attributeDetailsHidden ? View.GONE : View.VISIBLE);
        }
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
