package com.habitrpg.android.habitica.events;

import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

/**
 * Created by viirus on 26/01/16.
 */
public class DisplayTutorialEvent {

    public TutorialStep step;
    public String tutorialText;

    public List<String> tutorialTexts;
    public boolean canBeDeferred;
}
