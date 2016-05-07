package com.habitrpg.android.habitica.ui.fragments.inventory.equipment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.FragmentEquipmentOverviewBinding;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EquipmentOverviewFragment extends BaseMainFragment implements TransactionListener<List<ItemData>> {

    FragmentEquipmentOverviewBinding viewBinding;

    @BindView(R.id.battle_gear_group)
    View battleGearGroupView;

    @BindView(R.id.costume_group)
    View costumeGroupView;

    View battleGearHeadView;
    View battleGearHeadAccessoryView;
    View battleGearEyewearView;
    View battleGearArmorView;
    View battleGearBackView;
    View battleGearBodyView;
    View battleGearWeaponView;
    View battleGearShieldView;

    View costumeHeadView;
    View costumeHeadAccessoryView;
    View costumeEyewearView;
    View costumeArmorView;
    View costumeBackView;
    View costumeBodyView;
    View costumeWeaponView;
    View costumeShieldView;

    @BindView(R.id.costume_switch)
    Switch costumeSwitch;

    HashMap<String, String> nameMapping;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_equipment_overview, container, false);

        viewBinding = DataBindingUtil.bind(v);
        viewBinding.setCurrentBattleGear(this.user.getItems().getGear().getEquipped());
        viewBinding.setCurrentCostume(this.user.getItems().getGear().getCostume());
        viewBinding.setUsingCostume(this.user.getPreferences().getCostume());

        ButterKnife.bind(this, v);

        battleGearHeadView = battleGearGroupView.findViewById(R.id.outfit_head);
        battleGearHeadAccessoryView = battleGearGroupView.findViewById(R.id.outfit_head_accessory);
        battleGearEyewearView = battleGearGroupView.findViewById(R.id.outfit_Eyewear);
        battleGearArmorView = battleGearGroupView.findViewById(R.id.outfit_Armor);
        battleGearBackView = battleGearGroupView.findViewById(R.id.outfit_back);
        battleGearBodyView = battleGearGroupView.findViewById(R.id.outfit_Body);
        battleGearWeaponView = battleGearGroupView.findViewById(R.id.outfit_weapon);
        battleGearShieldView = battleGearGroupView.findViewById(R.id.outfit_shield);

        costumeHeadView = costumeGroupView.findViewById(R.id.outfit_head);
        costumeHeadAccessoryView = costumeGroupView.findViewById(R.id.outfit_head_accessory);
        costumeEyewearView = costumeGroupView.findViewById(R.id.outfit_Eyewear);
        costumeArmorView = costumeGroupView.findViewById(R.id.outfit_Armor);
        costumeBackView = costumeGroupView.findViewById(R.id.outfit_back);
        costumeBodyView = costumeGroupView.findViewById(R.id.outfit_Body);
        costumeWeaponView = costumeGroupView.findViewById(R.id.outfit_weapon);
        costumeShieldView = costumeGroupView.findViewById(R.id.outfit_shield);

        battleGearHeadView.setOnClickListener(v1 -> displayEquipmentDetailList("head", user.getItems().getGear().getEquipped().getHead(), false));
        battleGearHeadAccessoryView.setOnClickListener(v1 -> displayEquipmentDetailList("headAccessory", user.getItems().getGear().getEquipped().getHeadAccessory(), false));
        battleGearEyewearView.setOnClickListener(v1 -> displayEquipmentDetailList("eyewear", user.getItems().getGear().getEquipped().getEyeWear(), false));
        battleGearArmorView.setOnClickListener(v1 -> displayEquipmentDetailList("armor", user.getItems().getGear().getEquipped().getArmor(), false));
        battleGearBackView.setOnClickListener(v1 -> displayEquipmentDetailList("back", user.getItems().getGear().getEquipped().getBack(), false));
        battleGearBodyView.setOnClickListener(v1 -> displayEquipmentDetailList("body", user.getItems().getGear().getEquipped().getBody(), false));
        battleGearWeaponView.setOnClickListener(v1 -> displayEquipmentDetailList("weapon", user.getItems().getGear().getEquipped().getWeapon(), false));
        battleGearShieldView.setOnClickListener(v1 -> displayEquipmentDetailList("shield", user.getItems().getGear().getEquipped().getShield(), false));


        costumeHeadView.setOnClickListener(v1 -> displayEquipmentDetailList("head", user.getItems().getGear().getCostume().getHead(), true));
        costumeHeadAccessoryView.setOnClickListener(v1 -> displayEquipmentDetailList("headAccessory", user.getItems().getGear().getCostume().getHeadAccessory(), true));
        costumeEyewearView.setOnClickListener(v1 -> displayEquipmentDetailList("eyewear", user.getItems().getGear().getCostume().getEyeWear(), true));
        costumeArmorView.setOnClickListener(v1 -> displayEquipmentDetailList("armor", user.getItems().getGear().getCostume().getArmor(), true));
        costumeBackView.setOnClickListener(v1 -> displayEquipmentDetailList("back", user.getItems().getGear().getCostume().getBack(), true));
        costumeBodyView.setOnClickListener(v1 -> displayEquipmentDetailList("body", user.getItems().getGear().getCostume().getBody(), true));
        costumeWeaponView.setOnClickListener(v1 -> displayEquipmentDetailList("weapon", user.getItems().getGear().getCostume().getWeapon(), true));
        costumeShieldView.setOnClickListener(v1 -> displayEquipmentDetailList("shield", user.getItems().getGear().getCostume().getShield(), true));

        this.costumeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UpdateUserCommand command = new UpdateUserCommand();
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("preferences.costume", isChecked);
            command.updateData = updateData;

            EventBus.getDefault().post(command);
        });

        if (this.nameMapping == null) {
            new Select().from(ItemData.class).where(Condition.column("owned").eq(true)).async().queryList(this);
        } else {
            this.viewBinding.setEquipmentNames(this.nameMapping);
        }

        return v;
    }

    private void displayEquipmentDetailList(String type, String equipped, Boolean isCostume) {
        EquipmentDetailFragment fragment = new EquipmentDetailFragment();
        fragment.type = type;
        fragment.isCostume = isCostume;
        fragment.equippedGear = equipped;
        activity.displayFragment(fragment);
    }

    @Override
    public void onResultReceived(List<ItemData> result) {
        this.nameMapping = new HashMap<>();

        for (ItemData gear : result) {
            this.nameMapping.put(gear.key, gear.text);
        }

        this.viewBinding.setEquipmentNames(this.nameMapping);
    }

    @Override
    public boolean onReady(BaseTransaction<List<ItemData>> transaction) {
        return true;
    }

    @Override
    public boolean hasResult(BaseTransaction<List<ItemData>> transaction, List<ItemData> result) {
        return true;
    }
}
