package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME)
public class Preferences extends BaseModel {

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "hair_user_id",
            columnType = String.class,
            foreignColumnName = "userId")})
    public Hair hair;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "suppressedModals_user_id",
            columnType = String.class,
            foreignColumnName = "userId")})
    public SuppressedModals suppressModals;
    @Column
    @PrimaryKey
    @NotNull
    String user_id;
    @Column
    private boolean costume, toolbarCollapsed, advancedCollapsed, tagsCollapsed, newTaskEdit, disableClasses, stickyHeader, sleep, hideHeader;
    @Column
    private String allocationMode, shirt, skin, size, background, chair, language, sound;
    @Column
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
        if (chair != null && !chair.equals("none") && !chair.substring(0, 6).equals("chair_")) {
            return "chair_" + chair;
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

    public String getLanguage(){
        return language;
    }

    public void setLanguage(String language){
        this.language = language;
    }

    @Override
    public void save() {
        if (user_id == null) {
            return;
        }
        hair.userId = user_id;

        if (suppressModals != null)
            suppressModals.userId = user_id;

        super.save();
    }
}
