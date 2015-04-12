package com.magicmicky.habitrpgmobileapp;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;

public class HabitFragment extends CardFragment{

	private static final String TITLE = "Habits";
	private HabitAdapter adapter = null;
	private List<HabitItem> res = new ArrayList<HabitItem>();

	@Override
	public MyAdapter instantiateAdapter(Context c) {
		if(adapter==null) {
			List<HabitItem> habit = new ArrayList<HabitItem>();
			this.adapter =  new HabitAdapter(c,habit);
		}
		return adapter;
	}
	@Override
	protected String getTitle() {
		return HabitFragment.TITLE;
	}
	@Override
	protected HabitType getTaskType() {
		return HabitType.habit;
	}
	@Override
	protected List<HabitItem> filterData(List<HabitItem> items) {
		res.clear();
		Log.d("filtering", "filter habit");
		for(HabitItem it:items) {
			if(it instanceof Habit) {
				//Log.d("filter", "" + it.getText());
				res.add(it);
			} else {
				//Log.v("filter",""+it.getText() + " - ");
			}
		}
		return res;
	}
	private class HabitAdapter extends MyAdapter {
		public HabitAdapter(Context c, List<HabitItem> habits) {
			super(c,habits);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	if (convertView == null){  
	    		convertView = inflator.inflate(R.layout.habit, parent, false);
	        }
	    	convertView.setId(position);
	    	Habit currentItem = (Habit) this.getItem(position);
	        //Log.v("HabitFragment", currentItem.getText());
	    	Button habitPlus = (Button) convertView.findViewById(R.id.plus);
	    	habitPlus.setOnClickListener(getOnClickListener());
	    	Button habitMinus = (Button) convertView.findViewById(R.id.minus);
	    	habitMinus.setOnClickListener(getOnClickListener());
			TextView habit = (TextView) convertView.findViewById(R.id.TV_habit);
			//View color_indic = convertView.findViewById(R.id.V_color_indic);
			//color_indic.setBackgroundColor(getResources().getColor(getColorRes(currentItem.getValue())));
			ImageView color_indic = (ImageView) convertView.findViewById(R.id.IV_task_color);
			color_indic.setImageDrawable(getResources().getDrawable(getColorRes(currentItem.getValue())));
			habit.setText(currentItem.getText());
			habitPlus.setVisibility(currentItem.isUp()==true?Button.VISIBLE:Button.GONE);
			habitMinus.setVisibility(currentItem.isDown()==true?Button.VISIBLE:Button.GONE);
			if(!currentItem.isUp() || !currentItem.isDown()) {
				convertView.findViewById(R.id.btnDivider).setVisibility(View.INVISIBLE);
			} else {
				convertView.findViewById(R.id.btnDivider).setVisibility(View.VISIBLE);
			}
			convertView.setLongClickable(true);
			if(isItemChecked(position)) {
				convertView.setBackgroundResource(R.drawable.list_focused_holo);
			} else {
				convertView.setBackgroundResource(R.drawable.item_background);
			}
			return convertView;

		}
		
	}
	


}
