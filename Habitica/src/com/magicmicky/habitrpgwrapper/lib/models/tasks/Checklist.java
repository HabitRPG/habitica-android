package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of Checklist on HabitRPG
 * Created by MagicMicky
 */
public abstract class Checklist extends HabitItem{
	final private List<ChecklistItem> checklist;

    public Checklist() {
        this(null,null,null,null,null);
    }

    public Checklist(String id, String notes, Float priority, String text, Double value) {
	        super(id, notes, priority, text, value);
			this.checklist = new ArrayList<ChecklistItem>();
	}
	public Checklist(String id, String notes, Float priority, String text,
                     double value, Checklist checklistToCopy) {
        this(id, notes, priority, text, value);
		for(ChecklistItem item : checklistToCopy.getItems()) {
			checklist.add(new ChecklistItem(item));
		}
	}

	public void addItems(Checklist list){
		for (ChecklistItem l : list.getItems())
		{
			this.checklist.add(l);
		}
	}

    public void addItem(ChecklistItem item) {
		this.checklist.add(item);
	}


	public void toggleItem(String id) {
		boolean flag = false;
		for(int i=0;i<this.checklist.size() && !flag; i++) {
			ChecklistItem it = this.checklist.get(i);
			if(it.getId().equals(id)) {
				it.setCompleted(!it.isCompleted());
				flag=true;
			}
		}
	}
	public List<ChecklistItem> getItems() {
		return checklist;
	}
	
	public int getSize() {
		return checklist.size();
	}
	public int getNbCompleted() {
		int nbCompleted =0;
		for(ChecklistItem it : checklist) {
			if(it.isCompleted()) nbCompleted++;
		}
		return nbCompleted;
	}
	

public static class ChecklistItem {
    private String id;
	private String text;
	private boolean completed;
	public ChecklistItem() {
		this(null,null);
	}
	public ChecklistItem(String id, String text) {
		this(id,text,false);
	}
	public ChecklistItem(String id,String text, boolean completed) {
		this.setText(text);
		this.setId(id);
		this.setCompleted(completed);
	}
    public ChecklistItem(String s) {
        this(null,s);
    }
	public ChecklistItem(ChecklistItem item) {
		this.text = item.getText();
		this.id= item.getId();
		this.completed=item.isCompleted();
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}


}