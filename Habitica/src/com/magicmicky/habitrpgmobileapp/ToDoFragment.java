package com.magicmicky.habitrpgmobileapp;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;

public class ToDoFragment extends CardFragment {
	private static final String TITLE = "To do";
	private ToDoAdapter adapter = null;
	
	@Override
	public MyAdapter instantiateAdapter(Context c) {
		if(adapter == null) {
			List<HabitItem> todos = new ArrayList<HabitItem>();
			this.adapter = new ToDoAdapter(c,todos);
		}
		return adapter;
	}

	@Override
	protected HabitType getTaskType() {
		return HabitType.todo;
	}
	@Override
	protected String getTitle() {
		return ToDoFragment.TITLE;
	}

	@Override
	protected List<HabitItem> filterData(List<HabitItem> items) {
		List<HabitItem> res = new ArrayList<HabitItem>();
		for(HabitItem it:items) {
			if(it instanceof ToDo) {
				if(((ToDo) it).isCompleted()) {
					
				} else {
					res.add(it);
				}
			}
		}
		return res;
	}

	protected class ToDoAdapter extends MyAdapter {
		public ToDoAdapter(Context c, List<HabitItem> todos) {
			super(c,todos);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        LayoutInflater inflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	if (convertView == null){  
	    		convertView = inflator.inflate(R.layout.todo, parent, false);
	        }
	    	convertView.setId(position);
	    	ToDo currentItem = (ToDo) this.getItem(position);
			ImageView color_indic = (ImageView) convertView.findViewById(R.id.IV_task_color);
			color_indic.setImageDrawable(getResources().getDrawable(getColorRes(currentItem.getValue())));
	    	CheckBox completed = (CheckBox) convertView.findViewById(R.id.plus);
	    	completed.setOnClickListener(this.getOnClickListener());
	    	TextView text = (TextView) convertView.findViewById(R.id.TV_title);
			text.setText(currentItem.getText());
			completed.setChecked(currentItem.isCompleted());
			convertView.setLongClickable(true);
			if(isItemChecked(position)) {
				convertView.setBackgroundResource(R.drawable.list_focused_holo);
			} else {
				convertView.setBackgroundResource(R.drawable.item_background);
			}

			return convertView;

		}
		@Override
		protected String upOrDown(View v, HabitItem h) {
			boolean isChecked = ((CheckBox) v).isChecked();
			((ToDo) h).setCompleted(isChecked);
			return !isChecked ? "down" : "up";
			
		}
	}


}
