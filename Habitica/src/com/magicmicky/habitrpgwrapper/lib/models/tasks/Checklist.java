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
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of Checklist on HabitRPG
 * Created by MagicMicky
 */
public abstract class Checklist extends HabitItem{
    List<ChecklistItem> checklistItems;

    public Checklist() {
        this(null,null,null,null);
    }

    public Checklist(String notes, Float priority, String text, Double value) {
	        super(notes, priority, text, value);
			this.checklistItems = new ArrayList<ChecklistItem>();
	}
	public Checklist(String notes, Float priority, String text,
                     double value, Checklist checklistToCopy) {
        this(notes, priority, text, value);
		for(ChecklistItem item : checklistToCopy.getItems()) {
			checklistItems.add(new ChecklistItem(item));
		}
	}

	public void addItems(Checklist list){
		for (ChecklistItem l : list.getItems())
		{
			this.checklistItems.add(l);
		}
	}

    public void addItem(ChecklistItem item) {
		this.checklistItems.add(item);
	}


	public void toggleItem(String id) {
		boolean flag = false;
		for(int i=0;i<this.checklistItems.size() && !flag; i++) {
			ChecklistItem it = this.checklistItems.get(i);
			if(it.getId().equals(id)) {
				it.setCompleted(!it.getCompleted());
				flag=true;
			}
		}
	}

    public  List<ChecklistItem> getItems() {return checklistItems;};

	public List<ChecklistItem> getChecklistItems() {
		return checklistItems;
	}

	public int getSize() {
		return checklistItems.size();
	}
	public int getNbCompleted() {
		int nbCompleted =0;
		for(ChecklistItem it : checklistItems) {
			if(it.getCompleted()) nbCompleted++;
		}
		return nbCompleted;
	}

}