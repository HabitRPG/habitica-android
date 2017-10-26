package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.helpers.NumberAbbreviator;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.ItemDetailDialog;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;

public class RewardViewHolder extends BaseTaskViewHolder {

    private final DecimalFormat priceFormat;

    @BindView(R.id.buyButton)
    View buyButton;
    @BindView(R.id.priceLabel)
    TextView priceLabel;
    @BindView(R.id.gold_icon)
    ImageView goldIconView;

    public RewardViewHolder(View itemView) {
        super(itemView);
        priceFormat = new DecimalFormat("0.##");

        goldIconView.setImageBitmap(HabiticaIconsHelper.imageOfGold());
    }


    private boolean isItem() {
        return this.task.specialTag != null && this.task.specialTag.equals("item");
    }

    @Override
    public boolean canContainMarkdown() {
        return !isItem();
    }

    @OnClick(R.id.buyButton)
    void buyReward() {
        BuyRewardCommand event = new BuyRewardCommand();
        event.Reward = task;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onClick(View v) {
        if (!task.isValid()) {
            return;
        }
        if (task.specialTag != null && task.specialTag.equals("item")) {
            ItemDetailDialog dialog = new ItemDetailDialog(context);
            dialog.setTitle(task.getText());
            dialog.setDescription(task.getNotes());
            dialog.setImage("shop_" + this.task.getId());
            dialog.setCurrency("gold");
            dialog.setValue(task.getValue());
            dialog.setBuyListener((clickedDialog, which) -> this.buyReward());
            dialog.show();
        } else {
            TaskTappedEvent event = new TaskTappedEvent();
            event.Task = task;

            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void setDisabled(boolean openTaskDisabled, boolean taskActionsDisabled) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled);

        this.buyButton.setEnabled(!taskActionsDisabled);
    }

    public void bindHolder(Task reward, int position, boolean canBuy) {
        this.task = reward;
        super.bindHolder(reward, position);
        this.priceLabel.setText(NumberAbbreviator.INSTANCE.abbreviate(itemView.getContext(), this.task.value));

        if (canBuy) {
            goldIconView.setAlpha(1.0f);
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.yellow_50));
        } else {
            goldIconView.setAlpha(0.4f);
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.gray_500));
        }
    }
}
