package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.magicmicky.habitrpgwrapper.lib.models.Achievement;

import java.util.Collection;

public class AchievementAdapter extends BaseAdapter {
    private Context context;
    private Achievement[] achievements;

    public AchievementAdapter(Context context, Collection<Achievement> achievements) {
        this.context = context;
        this.achievements =new Achievement[achievements.size()];
        achievements.toArray(this.achievements);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

            gridView = inflater.inflate(R.layout.profile_achievement_item, null);


            TextView counterText = (TextView) gridView.findViewById(R.id.achievement_text);
            SimpleDraweeView draweeView = (SimpleDraweeView) gridView.findViewById(R.id.achievement_drawee);

            Achievement achiev = achievements[position];

        draweeView.setController(Fresco.newDraweeControllerBuilder()
                    .setUri(AvatarView.IMAGE_URI_ROOT  + achiev.icon.toLowerCase() + ".png")
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFailure(String id, Throwable throwable) {
                            Log.e("Achievemnt", "Couldn't load "+achiev.icon.toLowerCase());
                        }
                    })
                    .build());


        return gridView;
    }

    @Override
    public int getCount() {
        return achievements.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        TextView counterText;
        SimpleDraweeView draweeView;
    }
}