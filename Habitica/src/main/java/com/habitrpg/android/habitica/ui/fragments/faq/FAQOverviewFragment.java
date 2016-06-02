package com.habitrpg.android.habitica.ui.fragments.faq;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.menu.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.FAQOverviewRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
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

public class FAQOverviewFragment extends BaseMainFragment {
        @BindView(R.id.recyclerView)
        RecyclerView recyclerView;

        FAQOverviewRecyclerAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            unbinder = ButterKnife.bind(this, view);
            adapter = new FAQOverviewRecyclerAdapter();
            adapter.activity = activity;
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setAdapter(adapter);
            this.loadArticles();

            return view;
        }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void loadArticles() {
            if(user == null || adapter == null){
                return;
            }

            List<FAQArticle> articles = new Select()
                    .from(FAQArticle.class).queryList();

            adapter.setArticles(articles);
        }
}
