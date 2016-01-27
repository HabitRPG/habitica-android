package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TutorialView extends FrameLayout implements View.OnClickListener {

    @Bind(R.id.tutorialTextView)
    TextView tutorialTextView;

    @Bind(R.id.background)
    RelativeLayout background;

    @Bind(R.id.dismissButton)
    Button dismissButton;

    @Bind(R.id.completeButton)
    Button completeButton;

    public TutorialStep step;
    public OnTutorialReaction onReaction;

    public TutorialView(Context context, TutorialStep step, OnTutorialReaction onReaction) {
        super(context);
        LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.overlay_tutorial, this, true);
        ButterKnife.bind(this);
        background.setOnClickListener(this);
        dismissButton.setOnClickListener(this);
        completeButton.setOnClickListener(this);
        this.step = step;
        this.onReaction = onReaction;
    }

    public void setTutorialText(String text) {
        this.tutorialTextView.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (v == background || v == completeButton) {
            this.onReaction.onTutorialCompleted(this.step);
        } else if (v == dismissButton){
            this.onReaction.onTutorialDeferred(this.step);
        }
    }

    public interface OnTutorialReaction {
        void onTutorialCompleted(TutorialStep step);

        void onTutorialDeferred(TutorialStep step);
    }
}
