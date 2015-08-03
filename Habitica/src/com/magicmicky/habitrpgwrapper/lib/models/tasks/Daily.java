package com.magicmicky.habitrpgwrapper.lib.models.tasks;


import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;
import java.util.List;

/**
 * A daily item. It contains the item called "Daily" on the website
 * @author MagicMicky
 */
@Table(databaseName = HabitDatabase.NAME)
public class Daily extends Checklist{
	private final HabitType type=HabitType.daily;

    @Column
	private Boolean completed;

	@Column
    private String frequency;

    @Column
    private Integer everyX;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "days_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    private Days repeat;
	//TODO: private String lastCompleted;

    @Column
	private Integer streak;
	/**
	 * Construct a daily based on all the information needed
	 * @param id the id of the daily
	 * @param notes the notes associated to a daily
	 * @param priority the priority of the daily
	 * @param text the text of the daily
	 * @param value the value (points) of the daily
	 * @param completed whether or not the daily is completed
	 * @param repeat when does it repeat?
     * @param streak the streak
     * @param lastCompleted when was the last time it was completed?
	 */
    public Daily(String id, String notes, Float priority, String text,
                 Double value, Boolean completed, Days repeat, Integer streak, String lastCompleted) {
        //this(id, notes, priority, text, value,completed,repeat,lastCompleted);
        super(notes,priority,text,value);
        this.setId(id);
        this.setCompleted(completed);
        this.setRepeat(repeat);
        this.setStreak(streak);
        //this.setLastCompleted(lastCompleted);
    }
	public Daily(String id, String notes, Float priority, String text,
			Double value, Boolean completed, Days repeat) {
		this(id, notes, priority, text, value, completed, repeat, null, null);
	}

	public Daily() {
		this(null, null, null, null, null, null, null);
	}

	/**
	 * @return if the daily is completed
	 */
	public boolean getCompleted() {
		return completed;
	}
	/**
	 *  Set whether or not the daily is completed
	 * @param completed
	 */
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getEveryX() { return everyX; }
    public void setEveryX(Integer everyX) { this.everyX = everyX; }

	/**
	 * @return the repeat array.<br/>
	 * This array contains 7 values, one for each days, starting from monday.
	 */
	public Days getRepeat() {
		return repeat;
	}
	/**
	 * @param repeat the repeat array to set
	 */
	public void setRepeat(Days repeat) {
		this.repeat = repeat;
	}
	@Override
	public HabitType getType() {
		return type;
	}
	/**
	 * Formated: 
	 * SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	 * @return the lastCompleted
	 */
/*	public String getLastCompleted() {
		return lastCompleted;
	}
	/**
	 * @param lastCompleted the lastCompleted to set
	 */
/*	public void setLastCompleted(String lastCompleted) {
		this.lastCompleted = lastCompleted;
	}
	/**
	 * @return the streak
	 */
	public int getStreak() {
		return streak;
	}
	/**
	 * @param streak the streak to set
	 */
	public void setStreak(Integer streak) {
		this.streak = streak;
	}
}
