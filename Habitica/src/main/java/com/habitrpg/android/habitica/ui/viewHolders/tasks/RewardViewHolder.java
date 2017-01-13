package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.ui.ItemDetailDialog;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.view.View;
import android.widget.Button;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;

public class RewardViewHolder extends BaseTaskViewHolder {

    private final DecimalFormat priceFormat;
    @BindView(R.id.rewardImageView)
    SimpleDraweeView rewardImageView;

    @BindView(R.id.btnReward)
    Button rewardButton;

    public RewardViewHolder(View itemView) {
        super(itemView);
        priceFormat = new DecimalFormat("0.##");

    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);

        this.rewardButton.setText(this.priceFormat.format(this.task.value));

        if (this.isItem()) {
            this.rewardImageView.setVisibility(View.VISIBLE);
            DataBindingUtils.loadImage(this.rewardImageView, "shop_" + this.task.getId());
        } else {
            this.rewardImageView.setVisibility(View.GONE);
        }
    }

    private boolean isItem() {
        return this.task.specialTag != null && this.task.specialTag.equals("item");
    }

    @Override
    public boolean canContainMarkdown() {
        return !isItem();
    }

    @OnClick(R.id.btnReward)
    public void buyReward() {
        BuyRewardCommand event = new BuyRewardCommand();
        event.Reward = task;
        EventBus.getDefault().post(event);
    }

    @Override
    public void onClick(View v) {
        if (task.specialTag != null && task.specialTag.equals("item")) {
            ItemDetailDialog dialog = new ItemDetailDialog(context);
            dialog.setTitle(task.getText());
            dialog.setDescription(task.getNotes());
            dialog.setImage("shop_" + this.task.getId());
            dialog.setCurrency("gold");
            dialog.setValue(task.getValue());
            dialog.setBuyListener((clickedDialog, which) -> {
                this.buyReward();
            });
            dialog.show();
        } else {
            TaskTappedEvent event = new TaskTappedEvent();
            event.Task = task;

            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void setDisabled(boolean disabled) {
        super.setDisabled(disabled);

        this.rewardButton.setEnabled(!disabled);
    }

}
