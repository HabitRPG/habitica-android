package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.magicmicky.habitrpgwrapper.lib.models.Buffs;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Outfit;
import com.magicmicky.habitrpgwrapper.lib.models.Profile;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class FullProfileActivity extends BaseActivity {
    private String userId;
    private ContentCache contentCache;

    @Inject
    APIHelper apiHelper;

    @BindView(R.id.profile_image)
    ImageView profile_image;

    @BindView(R.id.profile_blurb)
    TextView blurbTextView;

    @BindView(R.id.avatarView)
    AvatarView avatarView;

    @BindView(R.id.attributes_table)
    TableLayout attributesTableLayout;

    @BindView(R.id.equipment_table)
    TableLayout equipmentTableLayout;

    @BindView(R.id.avatar_attributes_progress)
    ProgressBar attributesProgress;

    @BindView(R.id.avatar_equip_progress)
    ProgressBar equipmentProgress;

    @BindView(R.id.avatar_with_bars)
    View avatar_with_bars;
    private AvatarWithBarsViewModel avatarWithBars;


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
                        throwable -> {});

        this.contentCache = new ContentCache(apiHelper.apiService, apiHelper.languageCode);

        avatarWithBars = new AvatarWithBarsViewModel(this, avatar_with_bars);
        avatarWithBars.hideGems();
        avatarWithBars.valueBarLabelsToBlack();

        avatar_with_bars.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    private void updateView(HabitRPGUser user) {
        Profile profile = user.getProfile();
        Stats stats = user.getStats();

        setTitle(profile.getName());

        String imageUrl = profile.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            profile_image.setVisibility(View.INVISIBLE);
        } else {
            profile_image.setImageURI(Uri.parse(imageUrl));
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

    private String upperCaseFirstLetter(String s){
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void stopAndHideProgress(ProgressBar bar){
        bar.setIndeterminate(false);
        bar.setVisibility(View.GONE);
    }

    private String getCeiledValue(float val){
        return ((int)Math.ceil(val)) + "";
    }

    private void addAttributeRow(TableLayout table, String key, float val){
        int intVal = (int) Math.ceil(val);

        if(intVal == 0)
            return;

        addTableRow(table, key, intVal+"", R.layout.fullprofile_attributetablerow_two_textviews);
    }

    private void addTableRow(TableLayout table, String key, float val){
        int intVal = (int) Math.ceil(val);

        if(intVal == 0)
            return;

        addTableRow(table, key, intVal+"", R.layout.fullprofile_tablerow_two_textviews);
    }

    private void addTableRow(TableLayout table, String key, String value){
        addTableRow(table, key, value, R.layout.fullprofile_tablerow_two_textviews);
    }

    private void addTableRow(TableLayout table, String key, String value, int resourceId){
        TableRow tableRow = (TableRow)getLayoutInflater().inflate(resourceId, null);

        TextView keyTextView = (TextView)tableRow.findViewById(R.id.tableRowTextView1);
        keyTextView.setText(key);

        TextView valueTextView = (TextView)tableRow.findViewById(R.id.tableRowTextView2);
        valueTextView.setText(value);

        table.addView(tableRow);
    }

    // endregion

    // region Stats

    StatHelper strHelper;
    StatHelper intHelper;
    StatHelper conHelper;
    StatHelper perHelper;

    private void addLevelAttributes(Stats stats, HabitRPGUser user)
    {
        float byLevelStat = stats.getLvl()/2.0f;

        strHelper = getStatHelper("Strength: ");
        strHelper.add("Level: ", byLevelStat);

        intHelper = getStatHelper("Intelligence:");
        intHelper.add("Level: ", byLevelStat);

        conHelper = getStatHelper("Constitution:");
        conHelper.add("Level: ", byLevelStat);

        perHelper = getStatHelper("Perception:");
        perHelper.add("Level: ", byLevelStat);

        Outfit outfit = user.getItems().getGear().getEquipped();

        ArrayList<String> outfitList = new ArrayList<>();
        outfitList.add(outfit.getArmor());
        outfitList.add(outfit.getBack());
        outfitList.add(outfit.getBody());
        outfitList.add(outfit.getEyeWear());
        outfitList.add(outfit.getHead());
        outfitList.add(outfit.getHeadAccessory());
        outfitList.add(outfit.getShield());
        outfitList.add(outfit.getWeapon());

        contentCache.GetItemDataList(outfitList, obj ->{
            GotGear(obj, user);
            addNormalAddBuffAttributes(stats);

            stopAndHideProgress(attributesProgress);
            attributesTableLayout.setVisibility(View.VISIBLE);
        });
    }

    public void GotGear(List<ItemData> obj, HabitRPGUser user) {
        float strAttributes = 0;
        float intAttributes = 0;
        float conAttributes = 0;
        float perAttributes = 0;

        // Summarize stats and fill equipment table
        for (ItemData i : obj){
            int str_ = (int)i.getStr();
            int int_ = (int)i.get_int();
            int con_ = (int)i.getCon();
            int per_ = (int) i.getPer();

            strAttributes+= str_;
            intAttributes+= int_;
            conAttributes+= con_;
            perAttributes+= per_;

            StringBuilder sb = new StringBuilder();

            if(str_ != 0){
                sb.append("STR "+str_+", ");
            }
            if(int_ != 0){
                sb.append("INT "+int_+", ");
            }
            if(con_ != 0){
                sb.append("CON "+con_+", ");
            }
            if(per_ != 0){
                sb.append("PER "+per_+", ");
            }

            // remove the last comma
            if(sb.length() > 2)
            {
                sb.delete(sb.length()-2, sb.length());
            }

            addTableRow(equipmentTableLayout,i.getText(),  sb.toString(), R.layout.fullprofile_equipment_tablerow);
        }

        stopAndHideProgress(equipmentProgress);
        equipmentTableLayout.setVisibility(View.VISIBLE);

        strHelper.add("Equipment: ", strAttributes);
        intHelper.add("Equipment: ", intAttributes);
        conHelper.add("Equipment: ", conAttributes);
        perHelper.add("Equipment: ", perAttributes);

        if(!user.getPreferences().isDisableClasses()){
            float strClassBonus = 0;
            float intClassBonus = 0;
            float conClassBonus = 0;
            float perClassBonus = 0;

            switch(user.getStats().get_class()){
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

            strHelper.add("Class-Bonus: ", strClassBonus);
            intHelper.add("Class-Bonus: ", intClassBonus);
            conHelper.add("Class-Bonus: ", conClassBonus);
            perHelper.add("Class-Bonus: ", perClassBonus);
        }
    }


    private void addNormalAddBuffAttributes(Stats stats){
        Buffs buffs = stats.getBuffs();

        strHelper.add("Allocated: ", stats.getStr());
        strHelper.add("Boosts: ", buffs.getStr());

        intHelper.add("Allocated: ", stats.get_int());
        intHelper.add("Boosts: ", buffs.get_int());

        conHelper.add("Allocated: ", stats.getCon());
        conHelper.add("Boosts: ", buffs.getCon());

        perHelper.add("Allocated: ", stats.getPer());
        perHelper.add("Boosts: ", buffs.getPer());
    }

    private StatHelper getStatHelper(String label){
        TableRow tableRow = (TableRow)getLayoutInflater().inflate(R.layout.fullprofile_attributetablerow, null);
        TextView keyTextView = (TextView)tableRow.findViewById(R.id.tableRowTextView1);

        attributesTableLayout.addView(tableRow);

        TableLayout layout = (TableLayout)tableRow.findViewById(R.id.tableRowAttributesTable);

        return new StatHelper(layout, keyTextView, label);
    }

    public class StatHelper
    {
        private TableLayout layout;
        private TextView textViewLabel;
        private String attributeName;

        public float currentStatCount = 0;

        public StatHelper(TableLayout layout, TextView label, String attributeName){
            this.layout = layout;
            this.textViewLabel = label;
            this.attributeName = attributeName;
        }

        public void add(String label, float value){
            addAttributeRow(layout, label, value);

            currentStatCount += value;

            textViewLabel.setText(attributeName+" "+ getCeiledValue(currentStatCount));
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

    // endregion
}
