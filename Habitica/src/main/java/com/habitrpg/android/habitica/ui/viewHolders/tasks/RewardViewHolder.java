package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;

public class RewardViewHolder extends BaseTaskViewHolder {

    private final DecimalFormat priceFormat;
    @BindView(R.id.rewardImageView)
    ImageView rewardImageView;

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
        if(task.specialTag != null && task.specialTag.equals("item")) {
            LinearLayout contentViewForDialog = createContentViewForGearDialog();
            AlertDialog dialog = createGearDialog(contentViewForDialog);
            dialog.show();
        } else {
            TaskTappedEvent event = new TaskTappedEvent();
            event.Task = task;

            EventBus.getDefault().post(event);
        }
    }

    private AlertDialog createGearDialog(LinearLayout contentViewForDialog) {
        return new AlertDialog.Builder(context)
                .setPositiveButton(R.string.reward_dialog_buy, (dialog, which) -> {
                   this.buyReward();
                })
                .setTitle(this.task.getText())
                .setView(contentViewForDialog)
                .setNegativeButton(R.string.reward_dialog_dismiss, (dialog, which) -> {
                    dialog.dismiss();
                }).create();
    }

    @NonNull
    private LinearLayout createContentViewForGearDialog() {
        String price = this.priceFormat.format(this.task.value);
        String content = this.task.getNotes();

        // External ContentView
        LinearLayout contentViewLayout = new LinearLayout(context);
        contentViewLayout.setOrientation(LinearLayout.VERTICAL);

        // Gear Image
        ImageView gearImageView = new ImageView(context);
        LinearLayout.LayoutParams gearImageLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        gearImageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        gearImageLayoutParams.setMargins(0,0,0,20);
        gearImageView.setMinimumWidth(200);
        gearImageView.setMinimumHeight(200);
        gearImageView.setLayoutParams(gearImageLayoutParams);
        DataBindingUtils.loadImage(gearImageView, "shop_" + this.task.getId());

        // Gear Description
        TextView contentTextView = new TextView(context, null);
        if(!content.isEmpty()){
            contentTextView.setText(content);
        }

        // GoldPrice View
        LinearLayout goldPriceLayout = new LinearLayout(context);
        goldPriceLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams goldPriceLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        goldPriceLayoutParams.setMargins(0, 0, 0, 16);
        goldPriceLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        goldPriceLayout.setOrientation(LinearLayout.HORIZONTAL);
        goldPriceLayout.setLayoutParams(goldPriceLayoutParams);
        goldPriceLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        // Price View
        TextView priceTextView = new TextView(context);
        priceTextView.setText(price);
        priceTextView.setPadding(10, 0, 0, 0);

        ImageView gold = new ImageView(context);
        gold.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_header_gold));
        gold.setMinimumHeight(50);
        gold.setMinimumWidth(50);
        gold.setPadding(0, 0, 5, 0);

        goldPriceLayout.addView(gold);
        goldPriceLayout.addView(priceTextView);

        if(gearImageView.getDrawable()!= null){
            contentViewLayout.addView(gearImageView);
        }
        contentViewLayout.setGravity(Gravity.CENTER_VERTICAL);

        contentViewLayout.addView(goldPriceLayout);

        if(!content.isEmpty()){
            contentViewLayout.addView(contentTextView);
        }

        return contentViewLayout;
    }

}
