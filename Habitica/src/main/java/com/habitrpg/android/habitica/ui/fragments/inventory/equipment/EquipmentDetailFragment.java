package com.habitrpg.android.habitica.ui.fragments.inventory.equipment;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.adapter.inventory.EquipmentRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EquipmentDetailFragment extends BaseMainFragment {

    @Inject
    InventoryRepository inventoryRepository;

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

        setUnbinder(ButterKnife.bind(this, v));


        this.adapter = new EquipmentRecyclerViewAdapter(null, true);
        this.adapter.equippedGear = this.equippedGear;
        this.adapter.isCostume = this.isCostume;
        this.adapter.type = this.type;
        this.adapter.getEquipEvents()
                .flatMap(key -> inventoryRepository.equipGear(user, key, isCostume))
                .subscribe(items -> {}, RxErrorHandler.handleEmptyError());


        this.recyclerView.setAdapter(this.adapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new SafeDefaultItemAnimator());

        inventoryRepository.getOwnedEquipment(type).first().subscribe(this.adapter::updateData, RxErrorHandler.handleEmptyError());
        return v;
    }

    @Override
    public void onDestroy() {
        inventoryRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }
}
