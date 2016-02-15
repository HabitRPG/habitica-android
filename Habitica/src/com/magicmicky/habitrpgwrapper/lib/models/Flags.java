package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viirus on 22/01/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Flags extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String user_id;

    @Column
    private boolean showTour, dropsEnabled, itemsEnabled, newStuff, classSelected, rebirthEnabled, welcomed, armoireEnabled, armoireOpened, armoireEmpty;

    List<TutorialStep> tutorial;


    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tutorial")
    public List<TutorialStep> getTutorial() {
        if (tutorial == null) {
            tutorial = new Select()
                    .from(TutorialStep.class)
                    .where(Condition.column("user_id").eq(this.user_id))
                    .queryList();
        }
        return tutorial;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setTutorial(List<TutorialStep> tutorial) {
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

    @Override
    public void save() {

        for (TutorialStep step : this.getTutorial()) {
            step.user_id = user_id;
        }

        super.save();
    }

}
