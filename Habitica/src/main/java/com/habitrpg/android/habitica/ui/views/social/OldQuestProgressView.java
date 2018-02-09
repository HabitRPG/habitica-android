package com.habitrpg.android.habitica.ui.views.social;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.QuestProgress;
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.views.ValueBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OldQuestProgressView extends LinearLayout {

    @BindView(R.id.boss_name_view)
    TextView bossNameView;
    @BindView(R.id.boss_health_view)
    ValueBar bossHealthView;
    @BindView(R.id.collection_container)
    ViewGroup collectionContainer;

    public OldQuestProgressView(@NonNull Context context) {
        super(context);
        setupView(context);
    }

    public OldQuestProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupView(context);
    }

    private void setupView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.quest_progress_old, this);

        ButterKnife.bind(this, this);
    }

    public void setData(QuestContent quest, QuestProgress progress) {
        collectionContainer.removeAllViews();
        if (quest.isBossQuest()) {
            bossNameView.setText(quest.getBoss().getName());
            if (progress != null) {
                bossHealthView.set(progress.getHp(), quest.getBoss().getHp());
            }
            bossNameView.setVisibility(View.VISIBLE);
            bossHealthView.setVisibility(View.VISIBLE);
        } else {
            bossNameView.setVisibility(View.GONE);
            bossHealthView.setVisibility(View.GONE);

            if (progress != null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                if (progress.getCollect() != null) {
                    for (QuestProgressCollect collect : progress.getCollect()) {
                        QuestCollect contentCollect = quest.getCollectWithKey(collect.key);
                        if (contentCollect == null) {
                            continue;
                        }
                        View view = inflater.inflate(R.layout.quest_collect, collectionContainer, false);
                        SimpleDraweeView iconView = (SimpleDraweeView) view.findViewById(R.id.icon_view);
                        TextView nameView = (TextView) view.findViewById(R.id.name_view);
                        ValueBar valueView = (ValueBar) view.findViewById(R.id.value_view);
                        DataBindingUtils.INSTANCE.loadImage(iconView, "quest_" + quest.getKey() + "_" + collect.key);
                        nameView.setText(contentCollect.text);
                        valueView.set(collect.count, contentCollect.count);

                        collectionContainer.addView(view);
                    }
                }
            }
        }
    }

}
