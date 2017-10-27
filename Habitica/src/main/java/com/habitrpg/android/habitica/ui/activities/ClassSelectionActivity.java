package com.habitrpg.android.habitica.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.Gear;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Hair;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.ui.AvatarView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.functions.Action1;

public class ClassSelectionActivity extends BaseActivity implements Action1<User> {

    String currentClass;
    Boolean isInitialSelection;
    Boolean classWasUnset = false;
    Boolean shouldFinish = false;

    @BindView(R.id.healerAvatarView)
    AvatarView healerAvatarView;
    @BindView(R.id.mageAvatarView)
    AvatarView mageAvatarView;
    @BindView(R.id.rogueAvatarView)
    AvatarView rogueAvatarView;
    @BindView(R.id.warriorAvatarView)
    AvatarView warriorAvatarView;

    @Inject
    UserRepository userRepository;

    ProgressDialog progressDialog;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_class_selection;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isInitialSelection = bundle.getBoolean("isInitialSelection");
        currentClass = bundle.getString("currentClass");

        Preferences preferences = new Preferences();
        preferences.setHair(new Hair());
        preferences.setCostume(false);
        preferences.setSize(bundle.getString("size"));
        preferences.setSkin(bundle.getString("skin"));
        preferences.setShirt(bundle.getString("shirt"));
        preferences.getHair().setBangs(bundle.getInt("hairBangs"));
        preferences.getHair().setBase(bundle.getInt("hairBase"));
        preferences.getHair().setColor(bundle.getString("hairColor"));
        preferences.getHair().setMustache(bundle.getInt("hairMustache"));
        preferences.getHair().setBeard(bundle.getInt("hairBeard"));


        Outfit healerOutfit = new Outfit();
        healerOutfit.setArmor("armor_healer_5");
        healerOutfit.setHead("head_healer_5");
        healerOutfit.setShield("shield_healer_5");
        healerOutfit.setWeapon("weapon_healer_6");
        User healer = this.makeUser(preferences, healerOutfit);
        healerAvatarView.setAvatar(healer);

        Outfit mageOutfit = new Outfit();
        mageOutfit.setArmor("armor_wizard_5");
        mageOutfit.setHead("head_wizard_5");
        mageOutfit.setWeapon("weapon_wizard_6");
        User mage = this.makeUser(preferences, mageOutfit);
        mageAvatarView.setAvatar(mage);

        Outfit rogueOutfit = new Outfit();
        rogueOutfit.setArmor("armor_rogue_5");
        rogueOutfit.setHead("head_rogue_5");
        rogueOutfit.setShield("shield_rogue_6");
        rogueOutfit.setWeapon("weapon_rogue_6");
        User rogue = this.makeUser(preferences, rogueOutfit);
        rogueAvatarView.setAvatar(rogue);

        Outfit warriorOutfit = new Outfit();
        warriorOutfit.setArmor("armor_warrior_5");
        warriorOutfit.setHead("head_warrior_5");
        warriorOutfit.setShield("shield_warrior_5");
        warriorOutfit.setWeapon("weapon_warrior_6");
        User warrior = this.makeUser(preferences, warriorOutfit);
        warriorAvatarView.setAvatar(warrior);

        if (!isInitialSelection) {
            userRepository.changeClass()
                    .subscribe(user -> classWasUnset = true, RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    public User makeUser(Preferences preferences, Outfit outfit) {
        User user = new User();
        user.setPreferences(preferences);
        user.setItems(new Items());
        user.getItems().setGear(new Gear());
        user.getItems().getGear().setEquipped(outfit);
        return user;
    }

    @OnClick(R.id.healerWrapper)
    public void healerSelected() {
        displayConfirmationDialogForClass(getString(R.string.healer), Stats.HEALER);
    }

    @OnClick(R.id.mageWrapper)
    public void mageSelected() {
        displayConfirmationDialogForClass(getString(R.string.mage), Stats.MAGE);
    }

    @OnClick(R.id.rogueWrapper)
    public void rogueSelected() {
        displayConfirmationDialogForClass(getString(R.string.rogue), Stats.ROGUE);
    }

    @OnClick(R.id.warriorWrapper)
    public void warriorSelected() {
        displayConfirmationDialogForClass(getString(R.string.warrior), Stats.WARRIOR);
    }

    @OnClick(R.id.optOutWrapper)
    public void optOutSelected() {
        if (!this.isInitialSelection && !this.classWasUnset) {
            return;
        }
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.opt_out_confirmation))
                .setNegativeButton(getString(R.string.dialog_go_back), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.opt_out_class), (dialog, which) -> {
                    optOutOfClasses();
                }).create();
        alert.show();
    }

    private void displayConfirmationDialogForClass(String className, String classIdentifier) {

        if (!this.isInitialSelection && !this.classWasUnset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.change_class_confirmation))
                    .setMessage(getString(R.string.change_class_equipment_warning, currentClass))
                    .setNegativeButton(getString(R.string.dialog_go_back), (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(getString(R.string.choose_class), (dialog, which) -> {
                        selectClass(classIdentifier);
                        displayClassChanged(className);
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.class_confirmation, className))
                    .setNegativeButton(getString(R.string.dialog_go_back), (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(getString(R.string.choose_class), (dialog, which) -> {
                        selectClass(classIdentifier);
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void displayClassChanged(String newClassName) {
        AlertDialog.Builder changeConfirmedBuilder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.class_changed, newClassName))
                .setMessage(getString(R.string.class_changed_description))
                .setPositiveButton(getString(R.string.complete_tutorial), (dialog, which) -> {
                    dialog.dismiss();
                });
        AlertDialog changeDoneAlert = changeConfirmedBuilder.create();
        changeDoneAlert.show();
    }

    private void optOutOfClasses() {
        shouldFinish = true;
        this.displayProgressDialog();
        userRepository.disableClasses().subscribe(this, RxErrorHandler.handleEmptyError());
    }

    private void selectClass(String selectedClass) {
        shouldFinish = true;
        this.displayProgressDialog();
        userRepository.changeClass(selectedClass).subscribe(this, RxErrorHandler.handleEmptyError());
    }

    private void displayProgressDialog() {
        progressDialog = ProgressDialog.show(this, getString(R.string.changing_class_progress), null, true);
    }

    @Override
    public void call(User user) {
        if (shouldFinish) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            setResult(MainActivity.SELECT_CLASS_RESULT);
            finish();
        }
    }
}
