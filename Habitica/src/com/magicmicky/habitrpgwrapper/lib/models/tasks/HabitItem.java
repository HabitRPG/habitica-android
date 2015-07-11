package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONObject;

import java.util.List;

/**
 * Custom Item that regroup all the others.
 * @author MagicMicky
 *
 */
public abstract class HabitItem extends BaseModel {
    private String _id;

    @Column
    @PrimaryKey
    String id;

	@Column
	public String notes;

	@Column
    public Float priority;

	@Column
    public String text;

	@Column
    public Double value;

	@Column
    public String attribute;

	private Tags tags;
	/**
	 * Create a new HabitItem from what is necessary
	 * @param notes the notes associated to a habit
	 * @param priority the priority of the habit
	 * @param text the text of the habit
	 * @param value the value (points) of the habit
	 */
	public HabitItem(String notes, Float priority, String text, Double value) {
		this.setNotes(notes);
		this.setPriority(priority);
		this.setText(text);
		this.setValue(value);
		this.tags=new Tags();

	}
	public HabitItem() {
		this(null,null,null,null);
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
     * @param value the value to set
     */
    public void setValue(double value) {
        this.setValue(Double.valueOf(value));
    }
	
	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags.getTags();
	}
	/**
	 * @param tags the tagsId to set
	 */
	public void setTags(List<String> tags) {
		this.tags.setTags(tags);
	}
	
	public boolean isTagged(List<String> tags) {
		if(this.getTags()==null) {
			System.out.println("getTags is null!!!");
		}
        return (this.getTags() != null && this.getTags().size() != 0);

    }
	/**
	 * Returns a string of the type of the HabitItem
	 * @return the string of the Item type
	 */
	public abstract HabitType getType();

	/**
	 * Creates a JSON String for this HabitItem using the basic information.<br>
	 * Doesn't have the necessary open and close brackets to create an item.
	 * @return
	 */
	protected String getJSONBaseString() {
		StringBuilder json = new StringBuilder();
		if(this.getId()!=null)
			json.append("\"id\":").append(JSONObject.quote(this.getId())).append(",");
		json
			.append("\"type\":\"").append(this.getType()).append("\"," )
			.append("\"text\":").append(JSONObject.quote(this.getText())).append("," );
			if(this.getPriority()!=null)
				json.append("\"priority\":").append(this.getPriority()).append(",");
			if(this.getNotes()!=null && !this.getNotes().contentEquals(""))
				json.append("\"notes\":").append(JSONObject.quote(this.getNotes())).append("," );
			json.append("\"value\":").append(this.getValue()).append(",");
			if(this.getTags()!=null) { //TODO: && this.getTags().size()!=0
				json.append("\"tags\":{");
				/*for(String tagId : this.getTags()) {
					json.append("").append(JSONObject.quote(tagId)).append(":").append("true").append(",");
				}*/
				json.deleteCharAt(json.length()-1);
				json.append("},");
			}
			if(this.getAttribute()!=null) {
				json.append("\"attribute\":\"").append(this.getAttribute()).append("\",");
			}
		return 	json.toString();
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
}
