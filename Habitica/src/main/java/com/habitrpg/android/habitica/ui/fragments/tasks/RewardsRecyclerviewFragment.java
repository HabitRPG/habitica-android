package com.habitrpg.android.habitica.ui.fragments.tasks;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class RewardsRecyclerviewFragment extends TaskRecyclerViewFragment {

    public static RewardsRecyclerviewFragment newInstance(Context context, @Nullable User user, String classType) {
        RewardsRecyclerviewFragment fragment = new RewardsRecyclerviewFragment();
        fragment.setRetainInstance(true);
        fragment.user = user;
        fragment.classType = classType;
        fragment.tutorialStepIdentifier = "rewards";

        fragment.tutorialTexts = new ArrayList<>(Arrays.asList(context.getString(R.string.tutorial_rewards_1),
                    context.getString(R.string.tutorial_rewards_2)));
        fragment.tutorialCanBeDeferred = false;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (layoutManager != null) {
            ((GridLayoutManager)layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (recyclerAdapter.getItemViewType(position) < 2) {
                        return ((GridLayoutManager)layoutManager).getSpanCount();
                    } else {
                        return 1;
                    }
                }
            });
        }

        inventoryRepository.retrieveInAppRewards().subscribe(shopItems -> {}, RxErrorHandler.handleEmptyError());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View finalView = view;
        finalView.post(() -> setGridSpanCount(finalView.getWidth()));
        recyclerView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

        inventoryRepository.getInAppRewards().subscribe(shopItems -> {
            if (recyclerAdapter != null) {
                ((RewardsRecyclerViewAdapter)recyclerAdapter).updateItemRewards(shopItems);
            }
        }, RxErrorHandler.handleEmptyError());
    }

    @NonNull
    @Override
    protected LinearLayoutManager getLayoutManager(FragmentActivity context) {
        return new GridLayoutManager(context, 4);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        userRepository.retrieveUser(true, true)
                .flatMap(user -> inventoryRepository.retrieveInAppRewards())
                .doOnTerminate(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .subscribe(user1 -> {}, RxErrorHandler.handleEmptyError());
    }

    private void setGridSpanCount(int width) {
        float itemWidth;
        itemWidth = getContext().getResources().getDimension(R.dimen.reward_width);

        int spanCount = (int) (width / itemWidth);
        if (spanCount == 0) {
            spanCount = 1;
        }
        ((GridLayoutManager)layoutManager).setSpanCount(spanCount);
    }
}
