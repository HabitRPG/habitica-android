package com.habitrpg.android.habitica.ui;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.views.Typewriter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpeechBubbleView extends FrameLayout implements View.OnClickListener {

    @BindView(R.id.name_plate)
    TextView namePlate;

    @BindView(R.id.textView)
    Typewriter textView;

    @BindView(R.id.npc_image_view)
    ImageView npcImageView;

    @BindView(R.id.confirmation_buttons)
    ViewGroup confirmationButtons;

    @BindView(R.id.continue_indicator)
    View continueIndicator;
    private ShowNextListener showNextListener;

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

        confirmationButtons.setVisibility(View.GONE);

        this.setOnClickListener(this);
    }


    public void setConfirmationButtonVisibility(int visibility) {
        confirmationButtons.setVisibility(visibility);
    }

    public void animateText(String text) {
        this.textView.animateText(text);
    }

    public void setText(String text) {
        this.textView.setText(text);
    }

    public void setHasMoreContent(Boolean hasMoreContent) {
        continueIndicator.setVisibility(hasMoreContent ? View.VISIBLE : View.GONE);
    }

    public void setShowNextListener(ShowNextListener listener) {
        showNextListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (textView.isAnimating()) {
            textView.stopTextAnimation();
        } else {
            if (showNextListener != null) {
                showNextListener.showNextStep();
            }
        }
    }

    public interface ShowNextListener {
        void showNextStep();
    }
}
