package com.habitrpg.android.habitica.ui;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TutorialView extends FrameLayout implements View.OnClickListener {

    public TutorialStep step;
    public OnTutorialReaction onReaction;
    @BindView(R.id.textView)
    TextView tutorialTextView;
    @BindView(R.id.background)
    RelativeLayout background;
    @BindView(R.id.dismissButton)
    Button dismissButton;
    @BindView(R.id.completeButton)
    Button completeButton;
    @BindView(R.id.confirmation_buttons)
    ViewGroup confirmationButtons;

    public TutorialView(Context context, TutorialStep step, OnTutorialReaction onReaction) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.overlay_tutorial, this, true);
        ButterKnife.bind(this);
        background.setOnClickListener(this);
        dismissButton.setOnClickListener(this);
        completeButton.setOnClickListener(this);
        confirmationButtons.setVisibility(View.VISIBLE);
        this.step = step;
        this.onReaction = onReaction;
    }

    public void setTutorialText(String text) {
        this.tutorialTextView.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (Objects.equals(v, background) || Objects.equals(v, completeButton)) {
            this.onReaction.onTutorialCompleted(this.step);
        } else if (Objects.equals(v, dismissButton)) {
            this.onReaction.onTutorialDeferred(this.step);
        }
    }

    public interface OnTutorialReaction {
        void onTutorialCompleted(TutorialStep step);

        void onTutorialDeferred(TutorialStep step);
    }
}
