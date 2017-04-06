package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskDeleteEvent;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.greenrobot.eventbus.EventBus;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by viirus on 10/08/15.
 */
@ModelContainer
@Table(databaseName = HabitDatabase.NAME)
public class Task extends BaseModel {
    public static final String TYPE_HABIT = "habit";
    public static final String TYPE_TODO = "todo";
    public static final String TYPE_DAILY = "daily";
    public static final String TYPE_REWARD = "reward";
    public static final String FILTER_ALL = "all";
    public static final String FILTER_WEAK = "weak";
    public static final String FILTER_STRONG = "strong";
    public static final String FILTER_ACTIVE = "active";
    public static final String FILTER_GRAY = "gray";
    public static final String FILTER_DATED = "dated";
    public static final String FILTER_COMPLETED = "completed";
    public static final String FREQUENCY_WEEKLY = "weekly";
    public static final String FREQUENCY_DAILY = "daily";
    public static final String ATTRIBUTE_STRENGTH = "str";
    public static final String ATTRIBUTE_CONSTITUTION = "con";
    public static final String ATTRIBUTE_INTELLIGENCE = "int";
    public static final String ATTRIBUTE_PERCEPTION = "per";
    @Column
    @SerializedName("userId")
    public String user_id;
    @Column
    public Float priority;
    @Column
    public String text, notes, attribute, type;
    @Column
    public double value;
    public List<TaskTag> tags;
    @Column
    public Date dateCreated;
    @Column
    public int position;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "group_id",
            columnType = String.class,
            foreignColumnName = "task_id")})
    public TaskGroupPlan group;
    //Habits
    @Column
    public Boolean up, down;
    //todos/dailies
    @Column
    public boolean completed;
    public List<ChecklistItem> checklist;
    public List<RemindersItem> reminders;
    //dailies
    @Column
    public String frequency;
    @Column
    public Integer everyX, streak;
    @Column
    public
    Date startDate;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "days_id",
            columnType = String.class,
            foreignColumnName = "task_id")})
    public Days repeat;
    //todos
    @Column
    @SerializedName("date")
    public Date duedate;
    //TODO: private String lastCompleted;
    // used for buyable items
    public String specialTag;
    public CharSequence parsedText;
    public CharSequence parsedNotes;
    @Column
    @PrimaryKey
    @NotNull
    @SerializedName("_id")
    String id;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the priority
     */
    public Float getPriority() {
        return priority;
    }

    /**
     * @param i the priority to set
     */
    public void setPriority(Float i) {
        this.priority = i;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * To be allowed to set int value without problems
     *
     * @param value the value to set
     */
    public void setValue(double value) {
        this.setValue(Double.valueOf(value));
    }

    /**
     * Returns a string of the type of the Task
     *
     * @return the string of the Item type
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tags")
    public List<TaskTag> getTags() {
        if (tags == null) {
            tags = new Select()
                    .from(TaskTag.class)
                    .where(Condition.column("task_id").eq(this.id))
                    .queryList();
        }
        return tags;
    }

    public void setTags(List<TaskTag> tags) {
        for (TaskTag tag : tags) {
            tag.setTask(this);
        }
        this.tags = tags;
    }

    public boolean containsAnyTagId(ArrayList<String> tagIdList) {
        getTags();

        for (TaskTag t : tags) {
            if (tagIdList.contains(t.getTag().getId())) {
                return true;
            }
        }

        return false;
    }

    public boolean containsAllTagIds(List<String> tagIdList) {
        getTags();

        ArrayList<String> allTagIds = new ArrayList<String>();

        for (TaskTag t : tags) {
            allTagIds.add(t.getTag().getId());
        }

        return allTagIds.containsAll(tagIdList);
    }

    /**
     * @return whether or not the habit can be "upped"
     */
    public boolean getUp() {
        if (up == null) {
            return false;
        }
        return up;
    }

    /**
     * Set the Up value
     */
    public void setUp(Boolean up) {
        this.up = up;
    }

    /**
     * @return whether or not the habit can be "down"
     */
    public boolean getDown() {
        if (down == null) {
            return false;
        }
        return down;
    }

    /**
     * Set the Down value
     */
    public void setDown(Boolean down) {
        this.down = down;
    }


    public boolean getCompleted() {
        return completed;
    }

    /**
     * Set whether or not the daily is completed
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }


    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "checklist")
    public List<ChecklistItem> getChecklist() {
        if (this.checklist == null) {
            this.checklist = new Select()
                    .from(ChecklistItem.class)
                    .where(Condition.column("task_id").eq(this.id))
                    .orderBy(true, "position")
                    .queryList();
        }
        return this.checklist;
    }

    public void setChecklist(List<ChecklistItem> checklist) {
        for (ChecklistItem checklistItem : checklist) {
            checklistItem.setTask(this);
        }
        this.checklist = checklist;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "reminders")
    public List<RemindersItem> getReminders() {
        if (this.reminders == null) {
            this.reminders = new Select()
                    .from(RemindersItem.class)
                    .where(Condition.column("task_id").eq(this.id))
                    .orderBy(true, "time")
                    .queryList();
        }
        return this.reminders;
    }

    public void setReminders(List<RemindersItem> reminders) {
        for (RemindersItem remindersItem : reminders) {
            remindersItem.setTask(this);
        }
        this.reminders = reminders;
    }

    public Integer getCompletedChecklistCount() {
        Integer count = 0;
        for (ChecklistItem item : this.getChecklist()) {
            if (item.getCompleted()) {
                count++;
            }
        }
        return count;
    }

    public String getFrequency() {
        if (frequency == null) {
            return FREQUENCY_WEEKLY;
        }
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getEveryX() {
        if (everyX == null) {
            return 1;
        }
        return everyX;
    }

    public void setEveryX(Integer everyX) {
        this.everyX = everyX;
    }

    public Date getStartDate() {
        if (startDate == null) {
            return new Date();
        }
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the repeat array.<br/>
     * This array contains 7 values, one for each days, starting from monday.
     */
    public Days getRepeat() {
        if (repeat == null) {
            repeat = new Days();
        }
        return repeat;
    }

    /**
     * @param repeat the repeat array to set
     */
    public void setRepeat(Days repeat) {
        this.repeat = repeat;
    }

    /**
     * @return the streak
     */
    public int getStreak() {
        if (streak == null) {
            return 0;
        }
        return streak;
    }

    /**
     * @param streak the streak to set
     */
    public void setStreak(Integer streak) {
        this.streak = streak;
    }


    /**
     * @return the due date
     */
    public Date getDueDate() {
        return this.duedate;
    }

    /**
     * Set the due date
     *
     * @param duedate the date to set
     */
    public void setDueDate(@Nullable Date duedate) {
        this.duedate = duedate;
    }

    /**
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }


    @Override
    public void save() {
        if (this.getId() == null || this.getId().length() == 0) {
            return;
        }

        List<TaskTag> tmpTags = tags;
        List<ChecklistItem> tmpChecklist = checklist;
        List<RemindersItem> tmpReminders = reminders;

        // remove them, so that the database don't add empty entries

        tags = null;
        checklist = null;
        reminders = null;

        if (repeat != null) {
            repeat.task_id = this.id;
        }

        if (group != null) {
            group.task_id = this.id;
        }

        super.save();

        tags = tmpTags;
        checklist = tmpChecklist;
        reminders = tmpReminders;

        if (this.tags != null) {
            for (TaskTag tag : this.tags) {
                tag.setTask(this);
                tag.async().save();
            }
        }

        int position = 0;
        if (this.checklist != null) {
            for (ChecklistItem item : this.checklist) {
                if (item.getTask() == null) {
                    item.setTask(this);
                }
                item.setPosition(position);
                item.async().save();
                position++;
            }
        }

        int index = 0;
        if (this.reminders != null) {
            for (RemindersItem item : this.reminders) {
                if (item.getTask() == null) {
                    item.setTask(this);
                }
                if (item.getId() == null) {
                    item.setId(this.id + "task-reminder" + index);
                }
                item.async().save();
                index++;
            }
        }
    }

    @Override
    public void update() {
        if (this.getId() == null || this.getId().length() == 0) {
            return;
        }
        super.update();
    }

    public int getLightTaskColor() {
        if (this.value < -20)
            return R.color.worst_100;
        if (this.value < -10)
            return R.color.worse_100;
        if (this.value < -1)
            return R.color.bad_100;
        if (this.value < 1)
            return R.color.neutral_100;
        if (this.value < 5)
            return R.color.good_100;
        if (this.value < 10)
            return R.color.better_100;
        return R.color.best_100;
    }

    /**
     * Get the button color resources depending on a certain score
     *
     * @return the color resource id
     */
    public int getMediumTaskColor() {
        if (this.value < -20)
            return R.color.worst_50;
        if (this.value < -10)
            return R.color.worse_50;
        if (this.value < -1)
            return R.color.bad_50;
        if (this.value < 1)
            return R.color.neutral_50;
        if (this.value < 5)
            return R.color.good_50;
        if (this.value < 10)
            return R.color.better_50;

        return R.color.best_50;
    }

    /**
     * Get the button color resources depending on a certain score
     *
     * @return the color resource id
     */
    public int getDarkTaskColor() {
        if (this.value < -20)
            return R.color.worst_10;
        if (this.value < -10)
            return R.color.worse_10;
        if (this.value < -1)
            return R.color.bad_10;
        if (this.value < 1)
            return R.color.neutral_10;
        if (this.value < 5)
            return R.color.good_10;
        if (this.value < 10)
            return R.color.better_10;

        return R.color.best_10;
    }

    public Boolean isDue(int offset) {
        if (this.getCompleted()) {
            return true;
        }

        Calendar today = new GregorianCalendar();
        today.add(Calendar.HOUR, -offset);

        Calendar startDate = new GregorianCalendar();
        Calendar startDateAtMidnight;
        if (this.getStartDate() != null) {
            startDate.setTime(this.getStartDate());
            startDateAtMidnight = new GregorianCalendar(startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH));

            if (startDateAtMidnight.after(today)) {
                return false;
            }
        } else {
            startDateAtMidnight = new GregorianCalendar(startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH));
        }

        if (this.getFrequency().equals(FREQUENCY_DAILY)) {
            if (getEveryX() == 0) {
                return false;
            }

            TimeUnit timeUnit = TimeUnit.DAYS;
            long diffInMillies = startDateAtMidnight.getTimeInMillis() - today.getTimeInMillis();
            long daySinceStart = timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
            return (daySinceStart % this.getEveryX() == 0);
        } else {
            return this.getRepeat().getForDay(today.get(Calendar.DAY_OF_WEEK));
        }
    }

    public Boolean isDisplayedActive(int offset) {
        return this.isDue(offset) && !this.completed;
    }

    public Boolean isChecklistDisplayActive(int offset) {
        return this.isDisplayedActive(offset) && (this.checklist.size() != this.getCompletedChecklistCount());
    }

    public Date getNextActiveDateAfter(Date oldTime) {
        Calendar today = Calendar.getInstance();

        Calendar newTime = new GregorianCalendar();
        newTime.setTime(oldTime);

        if (this.getFrequency().equals(FREQUENCY_DAILY)) {
            Calendar startDate = new GregorianCalendar();
            startDate.setTime(this.getStartDate());

            TimeUnit timeUnit = TimeUnit.DAYS;
            long diffInMillies = today.getTimeInMillis() - startDate.getTimeInMillis();
            long daySinceStart = timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
            long daysUntilNextReminder = this.getEveryX() - (daySinceStart % this.getEveryX());

            today.add(Calendar.DATE, (int) daysUntilNextReminder);
            newTime.setTime(today.getTime());
        } else {
            int nextActiveDayOfTheWeek = newTime.get(Calendar.DAY_OF_WEEK);
            while (!this.getRepeat().getForDay(nextActiveDayOfTheWeek) || newTime.before(today) || newTime.equals(today)) {
                if (nextActiveDayOfTheWeek == 6) nextActiveDayOfTheWeek = 0;
                nextActiveDayOfTheWeek += 1;
                newTime.add(Calendar.DATE, 1);
            }
        }

        return newTime.getTime();
    }

    @Override
    public void delete() {
        TaskDeleteEvent event = new TaskDeleteEvent();
        event.task = this;
        EventBus.getDefault().post(event);
        super.delete();
    }

    public boolean isGroupTask() {
        if (group != null) {
            if (group.approvalRequired) {
                return true;
            }
        }
        return false;
    }

    public boolean isPendingApproval() {
        if (group != null) {
            if (group.approvalRequired && group.approvalRequested && !group.approvalApproved) {
                return true;
            }
        }
        return false;
    }
}
