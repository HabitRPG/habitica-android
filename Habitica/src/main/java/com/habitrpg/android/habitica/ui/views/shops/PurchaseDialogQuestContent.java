package com.habitrpg.android.habitica.ui.views.shops;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.QuestDropItem;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by phillip on 24.07.17.
 */

public class PurchaseDialogQuestContent extends PurchaseDialogContent {

    @BindView(R.id.questDetailView)
    View questDetailView;
    @BindView(R.id.questTypeTextView)
    TextView questTypeTextView;
    @BindView(R.id.boss_health_view)
    View bossHealthView;
    @BindView(R.id.boss_health_text)
    TextView bossHealthTextView;
    @BindView(R.id.quest_collect_view)
    View questCollectView;
    @BindView(R.id.quest_collect_text)
    TextView questCollectTextView;
    @BindView(R.id.quest_difficulty_view)
    RatingBar questDifficultyView;
    @BindView(R.id.rage_meter_view)
    View rageMeterView;
    @BindView(R.id.rewardsList)
    ViewGroup rewardsList;
    @BindView(R.id.ownerRewardsTitle)
    View ownerRewardsTitle;
    @BindView(R.id.ownerRewardsList)
    ViewGroup ownerRewardsList;

    public PurchaseDialogQuestContent(Context context) {
        super(context);
    }

    public PurchaseDialogQuestContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getViewId() {
        return R.layout.dialog_purchase_content_quest;
    }

    @Override
    public void setItem(ShopItem item) {
        super.setItem(item);
    }

    public void setQuestContent(QuestContent questContent) {

        if (questContent.isBossQuest()) {
            questTypeTextView.setText(R.string.boss_quest);
            questCollectView.setVisibility(View.GONE);
            bossHealthTextView.setText(String.valueOf(questContent.getBoss().hp));
            if (questContent.getBoss().hasRage()) {
                rageMeterView.setVisibility(View.VISIBLE);
            }
            questDifficultyView.setRating(questContent.getBoss().str);
        } else{
            questTypeTextView.setText(R.string.collection_quest);
            List<String> collectionList = new ArrayList<>();
            for (QuestCollect collect : questContent.getCollect()) {
                collectionList.add(collect.count + " " + collect.text);
            }
            questCollectTextView.setText(TextUtils.join(", ", collectionList));

            bossHealthView.setVisibility(View.GONE);

            questDifficultyView.setRating(1);
        }

        questDetailView.setVisibility(View.VISIBLE);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (questContent.getDrop().getItems() != null) {
            for (QuestDropItem item : questContent.getDrop().getItems()) {
                if (!item.isOnlyOwner()) {
                    addRewardsRow(inflater, item, rewardsList);
                }
            }

            boolean hasOwnerRewards = false;
            for (QuestDropItem item : questContent.getDrop().getItems()) {
                if (item.isOnlyOwner()) {
                    addRewardsRow(inflater, item, ownerRewardsList);
                    hasOwnerRewards = true;
                }
            }
            if (!hasOwnerRewards) {
                ownerRewardsTitle.setVisibility(View.GONE);
                ownerRewardsList.setVisibility(View.GONE);
            }

            if (questContent.getDrop().exp > 0) {
                ViewGroup view = (ViewGroup) inflater.inflate(R.layout.row_quest_reward, rewardsList, false);
                SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.imageView);
                TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
                titleTextView.setText(getContext().getString(R.string.experience_reward, questContent.getDrop().exp));
                rewardsList.addView(view);
            }

            if (questContent.getDrop().gp > 0) {
                ViewGroup view = (ViewGroup) inflater.inflate(R.layout.row_quest_reward, rewardsList, false);
                SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.imageView);
                TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
                titleTextView.setText(getContext().getString(R.string.gold_reward, questContent.getDrop().gp));
                rewardsList.addView(view);
            }
        }
    }

    private void addRewardsRow(LayoutInflater inflater, QuestDropItem item, ViewGroup containerView) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.row_quest_reward, containerView, false);
        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.imageView);
        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        DataBindingUtils.loadImage(imageView, item.getImageName());
        if (item.getCount() > 1) {
            titleTextView.setText(getContext().getString(R.string.quest_reward_count, item.getText(), item.getCount()));
        } else {
            titleTextView.setText(item.getText());
        }
        containerView.addView(view);
    }
}
