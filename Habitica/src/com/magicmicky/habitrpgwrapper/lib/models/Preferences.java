package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME)
public class Preferences extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    private boolean costume, toolbarCollapsed, advancedCollapsed, tagsCollapsed, newTaskEdit, disableClasses, stickyHeader, sleep, hideHeader;

    @Column
    private String allocationMode, shirt, skin, size;

    @Column
    private int dayStart, timezoneOffset;
    private Hair hair;
    public Preferences() {
    }

    public Preferences(boolean costume, boolean toolbarCollapsed, boolean advancedCollapsed, boolean tagsCollapsed, boolean newTaskEdit, boolean disableClasses, boolean stickyHeader, boolean sleep, boolean hideHeader, String allocationMode, String shirt, String skin, String size, int dayStart, int timezoneOffset, Hair hair) {
        this.costume = costume;
        this.toolbarCollapsed = toolbarCollapsed;
        this.advancedCollapsed = advancedCollapsed;
        this.tagsCollapsed = tagsCollapsed;
        this.newTaskEdit = newTaskEdit;
        this.disableClasses = disableClasses;
        this.stickyHeader = stickyHeader;
        this.sleep = sleep;
        this.hideHeader = hideHeader;
        this.allocationMode = allocationMode;
        this.shirt = shirt;
        this.skin = skin;
        this.size = size;
        this.dayStart = dayStart;
        this.timezoneOffset = timezoneOffset;
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

    public void setDisableClasses(boolean disableClasses) {
        this.disableClasses = disableClasses;
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

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
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

    public class Hair{
        private int mustache,beard, bangs,base;
        private String color;
        private Hair() {
        }
        private Hair(int mustache, int beard, int bangs, int base, String color) {
            this.mustache = mustache;
            this.beard = beard;
            this.bangs = bangs;
            this.base = base;
            this.color = color;
        }

        public int getMustache() {
            return mustache;
        }

        public void setMustache(int mustache) {
            this.mustache = mustache;
        }

        public int getBeard() {
            return beard;
        }

        public void setBeard(int beard) {
            this.beard = beard;
        }

        public int getBangs() {
            return bangs;
        }

        public void setBangs(int bangs) {
            this.bangs = bangs;
        }

        public int getBase() {
            return base;
        }

        public void setBase(int base) {
            this.base = base;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

}
