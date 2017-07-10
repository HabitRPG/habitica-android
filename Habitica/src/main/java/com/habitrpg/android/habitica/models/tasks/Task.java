package com.habitrpg.android.habitica.models.tasks;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Parcelable {
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


    public String userId;
    public float priority;
    public String text, notes, attribute, type;
    public double value;
    public RealmList<Tag> tags;
    public Date dateCreated;
    public int position;
    public TaskGroupPlan group;
    //Habits
    public Boolean up, down;
    //todos/dailies
    public boolean completed;
    public RealmList<ChecklistItem> checklist;
    public RealmList<RemindersItem> reminders;
    //dailies
    public String frequency;
    public Integer everyX, streak;
    public Date startDate;
    public Days repeat;
    //todos
    @SerializedName("date")
    public Date duedate;
    //TODO: private String lastCompleted;
    // used for buyable items
    public String specialTag;
    @Ignore
    public CharSequence parsedText;
    @Ignore
    public CharSequence parsedNotes;
    @PrimaryKey
    @SerializedName("_id")
    String id;

    public Boolean isDue;

    public Date nextDue;
    public Boolean yesterDaily;

    private String daysOfMonthString;
    private String weeksOfMonthString;

    @Ignore
    private List<Integer> daysOfMonth;
    @Ignore
    private List<Integer> weeksOfMonth;


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
        if (repeat != null) {
            repeat.setTaskId(id);
        }
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

    public RealmList<Tag> getTags() {
        return tags;
    }

    public void setTags(RealmList<Tag> tags) {
        this.tags = tags;
    }

    public boolean containsAnyTagId(List<String> tagIdList) {
        getTags();

        for (Tag t : tags) {
            if (tagIdList.contains(t.getId())) {
                return true;
            }
        }

        return false;
    }

    public boolean containsAllTagIds(List<String> tagIdList) {
        getTags();

        List<String> allTagIds = new ArrayList<>();

        for (Tag t : tags) {
            allTagIds.add(t.getId());
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


    public List<ChecklistItem> getChecklist() {
        return this.checklist;
    }

    public void setChecklist(RealmList<ChecklistItem> checklist) {
        this.checklist = checklist;
    }

    public List<RemindersItem> getReminders() {
        return this.reminders;
    }

    public void setReminders(RealmList<RemindersItem> reminders) {
        for (RemindersItem reminder : reminders) {
            reminder.setTask(this);
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

    public Boolean checkIfDue(int offset) {
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
        if (this.isDue != null && !this.completed) {
            return this.isDue;
        }
        return this.checkIfDue(offset) && !this.completed;
    }

    public Boolean isChecklistDisplayActive(int offset) {
        return this.isDisplayedActive(offset) && (this.checklist.size() != this.getCompletedChecklistCount());
    }

    public Date getNextReminderOccurence(Date oldTime) {
        Calendar today = Calendar.getInstance();

        Calendar newTime = new GregorianCalendar();
        newTime.setTime(oldTime);
        newTime.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        if (today.before(newTime)) {
            today.add(Calendar.DAY_OF_MONTH, -1);
        }

        if (nextDue != null) {
            Calendar nextDueCalendar = new GregorianCalendar();
            nextDueCalendar.setTime(nextDue);
            newTime.set(nextDueCalendar.get(Calendar.YEAR), nextDueCalendar.get(Calendar.MONTH), nextDueCalendar.get(Calendar.DAY_OF_MONTH));
            return newTime.getTime();
        }

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

    public void parseMarkdown() {
        try {
            this.parsedText = MarkdownParser.parseMarkdown(this.getText());
        } catch (NullPointerException e) {
            this.parsedText = this.getText();
        }
        try {
            this.parsedNotes = MarkdownParser.parseMarkdown(this.getNotes());
        } catch (NullPointerException e) {
            this.parsedNotes = this.getNotes();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (Task.class.isAssignableFrom(obj.getClass())) {
            Task otherTask = (Task) obj;
            return this.id.equals(otherTask.getId());
        }
        return super.equals(obj);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeValue(this.priority);
        dest.writeString(this.text);
        dest.writeString(this.notes);
        dest.writeString(this.attribute);
        dest.writeString(this.type);
        dest.writeDouble(this.value);
        dest.writeList(this.tags);
        dest.writeLong(this.dateCreated != null ? this.dateCreated.getTime() : -1);
        dest.writeInt(this.position);
        dest.writeValue(this.up);
        dest.writeValue(this.down);
        dest.writeByte(this.completed ? (byte) 1 : (byte) 0);
        dest.writeList(this.checklist);
        dest.writeList(this.reminders);
        dest.writeString(this.frequency);
        dest.writeValue(this.everyX);
        dest.writeValue(this.streak);
        dest.writeLong(this.startDate != null ? this.startDate.getTime() : -1);
        dest.writeParcelable(this.repeat, flags);
        dest.writeLong(this.duedate != null ? this.duedate.getTime() : -1);
        dest.writeString(this.specialTag);
        dest.writeString(this.id);
    }

    public Task() {
    }

    protected Task(Parcel in) {
        this.userId = in.readString();
        this.priority = (Float) in.readValue(Float.class.getClassLoader());
        this.text = in.readString();
        this.notes = in.readString();
        this.attribute = in.readString();
        this.type = in.readString();
        this.value = in.readDouble();
        this.tags = new RealmList<Tag>();
        in.readList(this.tags, TaskTag.class.getClassLoader());
        long tmpDateCreated = in.readLong();
        this.dateCreated = tmpDateCreated == -1 ? null : new Date(tmpDateCreated);
        this.position = in.readInt();
        this.up = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.down = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.completed = in.readByte() != 0;
        this.checklist = new RealmList<>();
        in.readList(this.checklist, ChecklistItem.class.getClassLoader());
        this.reminders = new RealmList<>();
        in.readList(this.reminders, RemindersItem.class.getClassLoader());
        this.frequency = in.readString();
        this.everyX = (Integer) in.readValue(Integer.class.getClassLoader());
        this.streak = (Integer) in.readValue(Integer.class.getClassLoader());
        long tmpStartDate = in.readLong();
        this.startDate = tmpStartDate == -1 ? null : new Date(tmpStartDate);
        this.repeat = in.readParcelable(Days.class.getClassLoader());
        long tmpDuedate = in.readLong();
        this.duedate = tmpDuedate == -1 ? null : new Date(tmpDuedate);
        this.specialTag = in.readString();
        this.id = in.readString();
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };


    public void setWeeksOfMonth(List<Integer> weeksOfMonth) {
        this.weeksOfMonth = weeksOfMonth;
        this.weeksOfMonthString = this.weeksOfMonth.toString();
    }

    public List<Integer> getWeeksOfMonth() {
        if (weeksOfMonth == null) {
            weeksOfMonth = new ArrayList<>();
            if (weeksOfMonthString != null) {
                try {
                    JSONArray obj = new JSONArray(weeksOfMonthString);
                    for (int i = 0; i < obj.length(); i += 1) {
                        weeksOfMonth.add(obj.getInt(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                weeksOfMonth = new ArrayList<>();
            }
        }
        return weeksOfMonth;
    }

    public void setDaysOfMonth(List<Integer> daysOfMonth) {
        this.daysOfMonth = daysOfMonth;
        this.daysOfMonthString = this.daysOfMonth.toString();
    }

    public List<Integer> getDaysOfMonth() {
        if (daysOfMonth == null) {
            daysOfMonth = new ArrayList<>();
            if (daysOfMonthString != null) {
                try {
                    JSONArray obj = new JSONArray(daysOfMonthString);
                    for (int i = 0; i < obj.length(); i += 1) {
                        daysOfMonth.add(obj.getInt(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                daysOfMonth = new ArrayList<>();
            }
        }

        return daysOfMonth;
    }
}
