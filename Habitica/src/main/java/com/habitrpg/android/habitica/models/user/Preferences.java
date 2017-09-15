package com.habitrpg.android.habitica.models.user;


import com.habitrpg.android.habitica.models.AvatarPreferences;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Preferences extends RealmObject implements AvatarPreferences {

    @PrimaryKey
    private String userId;

    public Hair hair;
    SuppressedModals suppressModals;
    User user;
    private boolean costume, toolbarCollapsed, advancedCollapsed, tagsCollapsed, newTaskEdit, disableClasses, stickyHeader, sleep, hideHeader, dailyDueDefaultView;
    private String allocationMode, shirt, skin, size, background, chair, language, sound;
    private int dayStart, timezoneOffset;

    public Preferences() {
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public int getDayStart() {
        return dayStart;
    }

    public void setDayStart(int dayStart) {
        this.dayStart = dayStart;
    }

    public boolean getCostume() {
        return costume;
    }

    public void setCostume(boolean costume) {
        this.costume = costume;
    }

    public boolean getToolbarCollapsed() {
        return toolbarCollapsed;
    }

    public void setToolbarCollapsed(boolean toolbarCollapsed) {
        this.toolbarCollapsed = toolbarCollapsed;
    }

    public boolean getAdvancedCollapsed() {
        return advancedCollapsed;
    }

    public void setAdvancedCollapsed(boolean advancedCollapsed) {
        this.advancedCollapsed = advancedCollapsed;
    }

    public boolean getTagsCollapsed() {
        return tagsCollapsed;
    }

    public void setTagsCollapsed(boolean tagsCollapsed) {
        this.tagsCollapsed = tagsCollapsed;
    }

    public boolean getNewTaskEdit() {
        return newTaskEdit;
    }

    public void setNewTaskEdit(boolean newTaskEdit) {
        this.newTaskEdit = newTaskEdit;
    }

    public boolean getDisableClasses() {
        return disableClasses;
    }

    public boolean getStickyHeader() {
        return stickyHeader;
    }

    public void setStickyHeader(boolean stickyHeader) {
        this.stickyHeader = stickyHeader;
    }

    public boolean getSleep() {
        return sleep;
    }

    public boolean getHideHeader() {
        return hideHeader;
    }

    public void setHideHeader(boolean hideHeader) {
        this.hideHeader = hideHeader;
    }

    public String getAllocationMode() {
        return allocationMode;
    }

    public void setAllocationMode(String allocationMode) {
        this.allocationMode = allocationMode;
    }

    public String getShirt() {
        return shirt;
    }

    public void setShirt(String shirt) {
        this.shirt = shirt;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(int timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public Hair getHair() {
        return hair;
    }

    public void setHair(Hair hair) {
        this.hair = hair;
    }

    public SuppressedModals getSuppressModals() {
        return suppressModals;
    }

    public void setSuppressModals(SuppressedModals suppressModals) {
        this.suppressModals = suppressModals;
    }

    public String getChair() {
        if (chair != null && !chair.equals("none")) {
            if (chair.length() > 5 && !chair.substring(0, 6).equals("chair_")) {
                return chair;
            } else {
                return "chair_" +chair;
            }
        }
        return null;
    }

    public void setChair(String chair) {
        this.chair = chair;
    }

    public boolean isSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public boolean isDisableClasses() {
        return disableClasses;
    }

    public void setDisableClasses(boolean disableClasses) {
        this.disableClasses = disableClasses;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean getDailyDueDefaultView() {
        return dailyDueDefaultView;
    }

    public void setDailyDueDefaultView(boolean dailyDueDefaultView) {
        this.dailyDueDefaultView = dailyDueDefaultView;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (hair != null && !hair.isManaged()) {
            hair.setUserId(userId);
        }
        if (suppressModals != null && !suppressModals.isManaged()) {
            suppressModals.setUserId(userId);
        }
    }
}
