package com.magicmicky.habitrpgwrapper.lib.models.tasks;


import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * A reward. Contain a reward that you can see on the website
 * @author MagicMicky
 *
 */
@Table(databaseName = HabitDatabase.NAME, allFields = true)
public class Reward extends Task {

	public Reward() {
		super();
	}
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

}
