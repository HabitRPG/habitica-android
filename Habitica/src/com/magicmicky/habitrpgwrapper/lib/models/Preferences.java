package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by MagicMicky on 16/03/14.
 */
public class Preferences {
    private boolean costume, toolbarCollapsed, advancedCollapsed, tagsCollapsed, newTaskEdit, disableClasses, stickyHeader, sleep, hideHeader;
    private String allocationMode, shirt, skin, size;
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

    public boolean isCostume() {
        return costume;
    }

    public void setCostume(boolean costume) {
        this.costume = costume;
    }

    public boolean isToolbarCollapsed() {
        return toolbarCollapsed;
    }

    public void setToolbarCollapsed(boolean toolbarCollapsed) {
        this.toolbarCollapsed = toolbarCollapsed;
    }

    public boolean isAdvancedCollapsed() {
        return advancedCollapsed;
    }

    public void setAdvancedCollapsed(boolean advancedCollapsed) {
        this.advancedCollapsed = advancedCollapsed;
    }

    public boolean isTagsCollapsed() {
        return tagsCollapsed;
    }

    public void setTagsCollapsed(boolean tagsCollapsed) {
        this.tagsCollapsed = tagsCollapsed;
    }

    public boolean isNewTaskEdit() {
        return newTaskEdit;
    }

    public void setNewTaskEdit(boolean newTaskEdit) {
        this.newTaskEdit = newTaskEdit;
    }

    public boolean isDisableClasses() {
        return disableClasses;
    }

    public void setDisableClasses(boolean disableClasses) {
        this.disableClasses = disableClasses;
    }

    public boolean isStickyHeader() {
        return stickyHeader;
    }

    public void setStickyHeader(boolean stickyHeader) {
        this.stickyHeader = stickyHeader;
    }

    public boolean isSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public boolean isHideHeader() {
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
