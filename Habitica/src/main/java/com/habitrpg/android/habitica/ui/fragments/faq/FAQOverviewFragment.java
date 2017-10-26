package com.habitrpg.android.habitica.ui.fragments.faq;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.FAQRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.adapter.FAQOverviewRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FAQOverviewFragment extends BaseMainFragment {
    @Inject
    FAQRepository faqRepository;

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
        adapter.getResetWalkthroughEvents().subscribe(aVoid -> this.userRepository.resetTutorial(user), RxErrorHandler.handleEmptyError());
        adapter.activity = activity;
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new SafeDefaultItemAnimator());
        this.loadArticles();

        return view;
    }

    @Override
    public void onDestroy() {
        faqRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void loadArticles() {
        if (user == null || adapter == null) {
            return;
        }
        faqRepository.getArticles().subscribe(adapter::setArticles, RxErrorHandler.handleEmptyError());
    }

    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.FAQ);
    }
}
