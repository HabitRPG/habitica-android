package com.magicmicky.habitrpgwrapper.lib.models.tasks;


import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

/**
 * A ToDo task that you can see of the website
 * You can set a complete date to a ToDo, and you can complete them using a boolean
 * @author MagicMicky
 *
 */

@Table(databaseName = HabitDatabase.NAME)
public class ToDo extends Checklist{
	private final HabitType type=HabitType.todo;

	@Column
	private Boolean completed;

	@Column
	private String date;
	/**
	 * Construct a daily based on all the information needed
	 * @param id the id of the daily
	 * @param notes the notes associated to a daily
	 * @param priority the priority of the daily
	 * @param text the text of the daily
	 * @param value the value (points) of the daily
	 * @param completed whether or not the daily is completed
	 * @param date the due date
	 */
	public ToDo(String id, String notes, Float priority, String text,
			double value, boolean completed, String date) {
		super(notes, priority, text, value);
		this.setId(id);
		this.setCompleted(completed);
		this.setDate(date);
	}

	public ToDo() {
		super();
		this.setCompleted(null);
		this.setDate(null);
	}
	/**
	 * @return if the todo is completed
	 */
	public boolean getCompleted() {
		return completed;
	}

	/**
	 *  Set whether or not the todo is completed
	 * @param completed
	 */
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	/**
	 * @return the due date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Set the due date
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public HabitType getType() {
		return type;
	}

}
