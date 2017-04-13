package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TutorialView extends FrameLayout {

    public TutorialStep step;
    @Nullable
    public OnTutorialReaction onReaction;
    @BindView(R.id.speech_bubble)
    SpeechBubbleView speechBubbleView;
    @BindView(R.id.background)
    RelativeLayout background;
    @BindView(R.id.dismissButton)
    Button dismissButton;
    @BindView(R.id.completeButton)
    Button completeButton;

    private List<String> tutorialTexts;
    private int currentTextIndex;

    public TutorialView(Context context, TutorialStep step, @Nullable OnTutorialReaction onReaction) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.overlay_tutorial, this, true);
        ButterKnife.bind(this);
        speechBubbleView.setConfirmationButtonVisibility(View.GONE);
        speechBubbleView.setShowNextListener(this::displayNextTutorialText);
        this.step = step;
        this.onReaction = onReaction;
    }

    public void setTutorialText(String text) {
        this.speechBubbleView.animateText(text);
        speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE);
    }

    public void setTutorialTexts(List<String> texts) {
        tutorialTexts = texts;
        currentTextIndex = -1;
        displayNextTutorialText();
    }

    public void setCanBeDeferred(boolean canBeDeferred) {
        dismissButton.setVisibility(canBeDeferred ? View.VISIBLE : View.GONE);
    }

    private void displayNextTutorialText() {
        currentTextIndex++;
        if (currentTextIndex < tutorialTexts.size()) {
            speechBubbleView.animateText(tutorialTexts.get(currentTextIndex));
            if (isDisplayingLastStep()) {
                speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE);
                speechBubbleView.setHasMoreContent(false);
            } else {
                speechBubbleView.setHasMoreContent(true);
            }
        } else {
            if (this.onReaction != null) {
                this.onReaction.onTutorialCompleted(this.step);
            }
        }
    }

    @OnClick(R.id.completeButton)
    public void completeButtonClicked() {
        if (this.onReaction != null) {
            this.onReaction.onTutorialCompleted(this.step);
        }
    }

    @OnClick(R.id.dismissButton)
    public void dismissButtonClicked() {
        if (this.onReaction != null) {
            this.onReaction.onTutorialDeferred(this.step);
        }
    }

    @OnClick(R.id.background)
    public void backgroundClicked() {
        speechBubbleView.onClick(speechBubbleView);
    }

    private boolean isDisplayingLastStep() {
        return currentTextIndex == (tutorialTexts.size()-1);
    }

    public interface OnTutorialReaction {
        void onTutorialCompleted(TutorialStep step);

        void onTutorialDeferred(TutorialStep step);
    }
}
