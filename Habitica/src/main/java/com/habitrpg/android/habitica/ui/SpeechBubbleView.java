package com.habitrpg.android.habitica.ui;


import com.habitrpg.android.habitica.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpeechBubbleView extends FrameLayout {

    @BindView(R.id.name_plate)
    TextView namePlate;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.npc_image_view)
    ImageView npcImageView;

    public SpeechBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.speechbubble, this);
        ButterKnife.bind(this);


        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SpeechBubbleView,
                0, 0);

        namePlate.setText(attributes.getString(R.styleable.SpeechBubbleView_namePlate));
        textView.setText(attributes.getString(R.styleable.SpeechBubbleView_text));

        Drawable iconRes = attributes.getDrawable(R.styleable.SpeechBubbleView_npcDrawable);
        if (iconRes != null) {
            npcImageView.setImageDrawable(iconRes);
        }
    }


}
