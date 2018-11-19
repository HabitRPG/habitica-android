package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Flags extends RealmObject {

    @PrimaryKey
    private String userId;

    RealmList<TutorialStep> tutorial;
    private boolean showTour;
    private boolean dropsEnabled;
    private boolean itemsEnabled;
    private boolean newStuff;
    private boolean classSelected;
    private boolean rebirthEnabled;
    private boolean welcomed;
    private boolean armoireEnabled;
    private boolean armoireOpened;
    private boolean armoireEmpty;
    private boolean communityGuidelinesAccepted;
    private boolean verifiedUsername;
    private boolean warnedLowHealth;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCommunityGuidelinesAccepted() {
        return communityGuidelinesAccepted;
    }

    public void setCommunityGuidelinesAccepted(boolean communityGuidelinesAccepted) {
        this.communityGuidelinesAccepted = communityGuidelinesAccepted;
    }

    public boolean isVerifiedUsername() {
        return verifiedUsername;
    }

    public void setVerifiedUsername(boolean verifiedUsername) {
        this.verifiedUsername = verifiedUsername;
    }

    public boolean isWarnedLowHealth() {
        return warnedLowHealth;
    }

    public void setWarnedLowHealth(boolean warnedLowHealth) {
        this.warnedLowHealth = warnedLowHealth;
    }
}
