package com.magicmicky.habitrpgwrapper.lib.models.tasks;


/**
 * An habit item. It contains the item called "Habits" on the website
 * @author MagicMicky
 *
 */
public class Habit extends HabitItem{
	private final HabitType type = HabitType.habit;
	private Boolean up;
	private Boolean down;
	/**
	 * Create a new Habit based on all the information needed
	 * @param id the id of the habit
	 * @param notes the notes associated to a habit
	 * @param priority the priority of the habit
	 * @param text the text of the habit
	 * @param value the value (points) of the habit
	 * @param up whether or not the habit can be "upped"
	 * @param down whether or not the habit can be "downed"
	 */
	public Habit(String id, String notes, Float priority, String text, double value
			, Boolean up, Boolean down) {
		super(id, notes, priority, text, value);
		this.setUp(up);
		this.setDown(down);
	}
	public Habit() {
		super();
		this.setDown(null);
		this.setUp(null);
	}
	/**
	 * @return whether or not the habit can be "upped"
	 */
	public boolean isUp() {
		return up;
	}
	/**
	 * Set the Up value
	 * @param up
	 */
	public void setUp(Boolean up) {
		this.up = up;
	}
	/**
	 * @return whether or not the habit can be "down"
	 */
	public boolean isDown() {
		return down;
	}
	/**
	 * Set the Down value
	 * @param down 
	 */
	public void setDown(Boolean down) {
		this.down = down;
	}
	@Override
	protected HabitType getType() {
		return type;
	}

}
