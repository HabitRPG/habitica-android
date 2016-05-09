package com.habitrpg.android.habitica.ui.fragments.inventory.equipment;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.inventory.EquipmentRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EquipmentDetailFragment extends BaseMainFragment {

    public String type;
    public String equippedGear;
    public Boolean isCostume;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    EquipmentRecyclerViewAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        ButterKnife.bind(this, v);

        this.adapter = new EquipmentRecyclerViewAdapter();
        this.adapter.equippedGear = this.equippedGear;
        this.adapter.isCostume = this.isCostume;
        this.adapter.type = this.type;
        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        this.recyclerView.setAdapter(this.adapter);

        this.loadGear();

        return v;
    }

    private void loadGear() {
        if(user == null || adapter == null){
            return;
        }

        List<ItemData> gear = new Select()
                .from(ItemData.class)
                .where(Condition.CombinedCondition.begin(Condition.column("type").eq(this.type))
                                .and(Condition.column("owned").eq(true))
                ).queryList();

        adapter.setGearList(gear);
    }
}
