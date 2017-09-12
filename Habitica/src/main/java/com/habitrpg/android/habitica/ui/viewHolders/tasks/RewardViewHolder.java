package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
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

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);

        this.priceLabel.setText(this.priceFormat.format(this.task.value));
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
    public void setDisabled(boolean openTaskDisabled, boolean taskActionsDisabled) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled);

        this.buyButton.setEnabled(!taskActionsDisabled);
    }

}
