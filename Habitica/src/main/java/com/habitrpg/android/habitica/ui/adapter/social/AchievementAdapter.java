package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.Achievement;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AchievementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String itemType;
    public MainActivity activity;
    private List<Object> itemList;

    public <T extends Achievement> void setItemList(List<Object> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.profile_achievement_category, parent, false);

            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.profile_achievement_item, parent, false);

            return new AchievementViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = this.itemList.get(position);
        if (obj.getClass().equals(String.class)) {
            ((SectionViewHolder) holder).bind((String) obj);
        } else {
            ((AchievementViewHolder) holder).bind((Achievement) itemList.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.itemList.get(position).getClass().equals(String.class)) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class AchievementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Achievement achievement;

        @BindView(R.id.achievement_drawee)
        SimpleDraweeView draweeView;

        @BindView(R.id.achievement_text)
        TextView titleView;

        @BindView(R.id.achievement_count_label)
        TextView countText;

        @BindView(R.id.achievement_item_layout)
        LinearLayout item_layout;

        Resources resources;

        public AchievementViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            item_layout = (LinearLayout) itemView;
            resources = itemView.getResources();

            item_layout.setClickable(true);
            item_layout.setOnClickListener(this);
        }

        public void bind(Achievement item) {
              draweeView.setController(Fresco.newDraweeControllerBuilder()
                    .setUri(AvatarView.IMAGE_URI_ROOT  + item.icon.toLowerCase() + ".png")
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFailure(String id, Throwable throwable) {
                            Log.e("Achievemnt", "Couldn't load "+item.icon.toLowerCase());
                        }
                    })
                    .build());



            this.achievement = item;
            titleView.setText(item.title);

            if(item.optionalCount == null) {
                countText.setVisibility(View.GONE);
            } else{
                countText.setVisibility(View.VISIBLE);
                countText.setText(item.optionalCount.toString());
            }
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder b = new AlertDialog.Builder(HabiticaApplication.currentActivity);

            View customView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.dialog_achievement_details, null);
            ImageView achievementImage = (ImageView)customView.findViewById(R.id.achievement_image);
            achievementImage.setImageDrawable(draweeView.getDrawable());

            TextView titleView = (TextView) customView.findViewById(R.id.achievement_title);
            titleView.setText(achievement.title);


            TextView textView = (TextView) customView.findViewById(R.id.achievement_text);
            textView.setText(achievement.text);

            b.setView(customView);
            b.setPositiveButton(R.string.profile_achievement_ok, (dialogInterface, i) -> {});

            b.show();
        }
    }
}
