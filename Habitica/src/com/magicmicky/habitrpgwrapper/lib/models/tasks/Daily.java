package com.magicmicky.habitrpgwrapper.lib.models.tasks;


/**
 * A daily item. It contains the item called "Daily" on the website
 * @author MagicMicky
 */
public class Daily extends Checklist{
	private final HabitType type=HabitType.daily;
	private Boolean completed;
	private Days repeat;
	//TODO: private String lastCompleted;
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
        super(id,notes,priority,text,value);
        this.setCompleted(completed);
        this.setRepeat(repeat);
        this.setStreak(streak);
        //this.setLastCompleted(lastCompleted);
    }
	public Daily(String id, String notes, Float priority, String text,
			Double value, Boolean completed, Days repeat) {
		this(id, notes, priority, text, value,completed,repeat,null,null);
	}

	public Daily() {
		this(null,null,null,null,null,null,null);
	}
	/**
	 * @return if the daily is completed
	 */
	public boolean isCompleted() {
		return completed;
	}
	/**
	 *  Set whether or not the daily is completed
	 * @param completed
	 */
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
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
	protected HabitType getType() {
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

    public static class Days {
        private boolean m, t,w, th,f,s,su;
        public Days() {
            this.m=false;
            this.t=false;
            this.w=false;
            this.th=false;
            this.f=false;
            this.s=true;
            this.su=true;
        }

        public boolean isT() {
            return t;
        }

        public void setT(boolean t) {
            this.t = t;
        }

        public boolean isW() {
            return w;
        }

        public void setW(boolean w) {
            this.w = w;
        }

        public boolean isTh() {
            return th;
        }

        public void setTh(boolean th) {
            this.th = th;
        }

        public boolean isF() {
            return f;
        }

        public void setF(boolean f) {
            this.f = f;
        }

        public boolean isS() {
            return s;
        }

        public void setS(boolean s) {
            this.s = s;
        }

        public boolean isSu() {
            return su;
        }

        public void setSu(boolean su) {
            this.su = su;
        }

        public boolean isM() {
            return m;
        }

        public void setM(boolean m) {
            this.m = m;
        }
    }
}
