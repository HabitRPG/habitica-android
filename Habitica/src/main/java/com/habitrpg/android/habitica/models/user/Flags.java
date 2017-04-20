package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Flags extends RealmObject {

    User user;
    RealmList<TutorialStep> tutorial;
    private boolean showTour, dropsEnabled, itemsEnabled, newStuff, classSelected, rebirthEnabled, welcomed, armoireEnabled, armoireOpened, armoireEmpty;

    public List<TutorialStep> getTutorial() {
        return tutorial;
    }

    public void setTutorial(RealmList<TutorialStep> tutorial) {
        this.tutorial = tutorial;
    }

    public boolean getShowTour() {
        return showTour;
    }

    public void setShowTour(boolean showTour) {
        this.showTour = showTour;
    }

    public boolean getDropsEnabled() {
        return dropsEnabled;
    }

    public void setDropsEnabled(boolean dropsEnabled) {
        this.dropsEnabled = dropsEnabled;
    }

    public boolean getItemsEnabled() {
        return itemsEnabled;
    }

    public void setItemsEnabled(boolean itemsEnabled) {
        this.itemsEnabled = itemsEnabled;
    }

    public boolean getNewStuff() {
        return newStuff;
    }

    public void setNewStuff(boolean newStuff) {
        this.newStuff = newStuff;
    }

    public boolean getClassSelected() {
        return classSelected;
    }

    public void setClassSelected(boolean classSelected) {
        this.classSelected = classSelected;
    }

    public boolean getRebirthEnabled() {
        return rebirthEnabled;
    }

    public void setRebirthEnabled(boolean rebirthEnabled) {
        this.rebirthEnabled = rebirthEnabled;
    }

    public boolean getWelcomed() {
        return welcomed;
    }

    public void setWelcomed(boolean welcomed) {
        this.welcomed = welcomed;
    }

    public boolean getArmoireEnabled() {
        return armoireEnabled;
    }

    public void setArmoireEnabled(boolean armoireEnabled) {
        this.armoireEnabled = armoireEnabled;
    }

    public boolean getArmoireOpened() {
        return armoireOpened;
    }

    public void setArmoireOpened(boolean armoireOpened) {
        this.armoireOpened = armoireOpened;
    }

    public boolean getArmoireEmpty() {
        return armoireEmpty;
    }

    public void setArmoireEmpty(boolean armoireEmpty) {
        this.armoireEmpty = armoireEmpty;
    }

}
