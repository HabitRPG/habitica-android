package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.ItemsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.BaseItem;
import com.magicmicky.habitrpgwrapper.lib.models.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.Food;
import com.magicmicky.habitrpgwrapper.lib.models.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.QuestItem;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ItemsRecyclerFragment extends BaseFragment implements TransactionListener<List<BaseItem>> {

    public String itemType;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    private View view;
    private ItemsRecyclerViewAdapter viewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
            android.support.v4.app.FragmentActivity context = getActivity();

            ButterKnife.bind(this, view);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            viewAdapter = new ItemsRecyclerViewAdapter();
            recyclerView.setAdapter(viewAdapter);

            this.loadItems();
        }
        return view;
    }

    private void loadItems() {
        Select select = new Select();
        From from = null;
        switch (this.itemType) {
            case "eggs":
                from = select.from(Egg.class);
                break;
            case "hatchingpotions":
                from = select.from(HatchingPotion.class);
                break;
            case "food":
                from = select.from(Food.class);
                break;
            case "quests":
                from = select.from(QuestItem.class);
                break;
        }
        if (from != null) {
            from.async().queryList(this);
        }
    }

    @Override
    public void onResultReceived(List<BaseItem> result) {
        this.viewAdapter.setItemsArrayList(result);
    }

    @Override
    public boolean onReady(BaseTransaction<List<BaseItem>> transaction) {
        return false;
    }

    @Override
    public boolean hasResult(BaseTransaction<List<BaseItem>> transaction, List<BaseItem> result) {
        return false;
    }
}
