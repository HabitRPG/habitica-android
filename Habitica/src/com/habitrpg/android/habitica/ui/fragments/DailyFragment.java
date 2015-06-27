package com.habitrpg.android.habitica.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.ChecklistDialog;
import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.utils.DaysUtils;

public class DailyFragment extends CardFragment {

    private static final String TITLE = "Dailies";
    private DailyAdapter adapter=null;
    private int dayStart=0;
    @Override
    public MyAdapter instantiateAdapter(Context c) {
        if(adapter==null) {
            List<HabitItem> dailies = new ArrayList<HabitItem>();
            this.adapter = new DailyAdapter(c,dailies);
        }
        return adapter;
    }
    @Override
    protected HabitType getTaskType() {
        return HabitType.daily;
    }
    @Override
    protected String getTitle() {
        return DailyFragment.TITLE;
    }


    @Override
    protected List<HabitItem> filterData(List<HabitItem> items) {
        List<HabitItem> res = new ArrayList<HabitItem>();
        for(HabitItem it:items) {
            if(it instanceof Daily) {
                res.add(it);
            }
        }
        return res;
    }

    private class DailyAdapter extends MyAdapter {
        public DailyAdapter(Context c, List<HabitItem> dailies) {
            super(c,dailies);
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Daily currentItem = (Daily) this.getItem(position);
            LayoutInflater inflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null){
                convertView = inflator.inflate(R.layout.todo, parent, false);
            }

            convertView.setId(position);
            CheckBox completed = (CheckBox) convertView.findViewById(R.id.plus);
            completed.setOnClickListener(this.getOnClickListener());
            TextView text = (TextView) convertView.findViewById(R.id.TV_title);

            ImageButton checklist = (ImageButton) convertView.findViewById(R.id.BT_checklist);

            Calendar c = Calendar.getInstance();
            c.add(Calendar.HOUR_OF_DAY, -getDayStart());
            int index = c.get(Calendar.DAY_OF_WEEK)-2;
            if(index<0) {
                index+=7;
            }
            if(currentItem.getRepeat() !=null && DaysUtils.getBooleansFromDays(currentItem.getRepeat()).length >= index && !DaysUtils.getBooleansFromDays(currentItem.getRepeat())[index]) {//What do we say to death? Not today!
                text.setTypeface(null, Typeface.ITALIC);
                text.setTextColor(getResources().getColor(R.color.card_light_text));
                ImageView color_indic = (ImageView) convertView.findViewById(R.id.IV_task_color);
                color_indic.setImageResource(R.color.transparent);
            } else {
                if(!currentItem.isCompleted()) {
                    ImageView color_indic = (ImageView) convertView.findViewById(R.id.IV_task_color);
                    color_indic.setImageDrawable(getResources().getDrawable(getColorRes(currentItem.getValue())));
                    text.setTypeface(null, Typeface.BOLD);
                } else {
                    ImageView color_indic = (ImageView) convertView.findViewById(R.id.IV_task_color);
                    color_indic.setImageDrawable(getResources().getDrawable(R.drawable.triangle_completed));
                    text.setTypeface(null, Typeface.NORMAL);
                }
                text.setTextColor(getResources().getColor(R.color.card_text));
            }
            text.setText(currentItem.getText());
            completed.setChecked(isChecked(currentItem));
            convertView.setLongClickable(true);

            List<com.magicmicky.habitrpgwrapper.lib.models.tasks.Checklist.ChecklistItem> items =currentItem.getItems();

            if (items != null && !items.isEmpty()) {
                checklist.setVisibility(View.VISIBLE);
                checklist.setOnClickListener(openChecklist);
            } else {
                checklist.setVisibility(View.GONE);
            }

            if(isItemChecked(position)) {
                convertView.setBackgroundResource(R.drawable.list_focused_holo);
            } else {
                convertView.setBackgroundResource(R.drawable.item_background);
            }

            return convertView;
        }
        private boolean isChecked(Daily task) {
			/*TODO:Calendar resetTime = new GregorianCalendar();
			// reset hour, minutes, seconds and millis
			resetTime.set(Calendar.HOUR_OF_DAY, getDayStart());
			resetTime.set(Calendar.MINUTE, 0);
			resetTime.set(Calendar.SECOND, 0);
			resetTime.set(Calendar.MILLISECOND, 0);
			Timestamp last= new Timestamp(task.getLastCompleted());
			//last = 29/05/2013 23:59
			//date =30/05/2013 00:00
			if(task.isCompleted() && last.compareTo(new Timestamp(resetTime.getTime().getTime())) < 0) {// if last is before today
				return false;
			} else {*/
            return task.isCompleted();
            //}
        }
        @Override
        protected String upOrDown(View v, HabitItem h) {
            boolean isChecked = ((CheckBox) v).isChecked();
            Calendar now = new GregorianCalendar();
            ((Daily) h).setCompleted(isChecked);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//            ((Daily) h).setLastCompleted(format.format(new Date(now.getTimeInMillis())));
            //TODO wth is that ?
            return !isChecked ? "down" : "up";

        }
        private View.OnClickListener openChecklist = new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                if(v.getParent() != null && v.getParent().getParent() != null) {

                    int position = ((View) v.getParent().getParent()).getId();

                    ChecklistDialog dialog = new ChecklistDialog(getActivity(),mAPIHelper,(Daily) getItem(position), null);

                    dialog.show();
                }
            }
        };
    }

    public int getDayStart() {
        return this.dayStart;
    }
    public void setDayStart(int dayStart) {
        this.dayStart = dayStart;
    }


}
