package com.habitrpg.android.habitica;

import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;
import com.magicmicky.habitrpgwrapper.lib.utils.DaysUtils;

import static com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType.*;
import static com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitType.habit;

public class AddTaskDialog extends DialogFragment implements OnDateSetListener{
	private EditText taskText, taskNote, taskValue;
	private boolean[] repeat = {false, false, false, false, false, false, false};
	private boolean[] down_up= {true,true};
	private String toDoDate;
	private HabitType hType;

	private String[] mShortWeekDayStrings = {"Mo","Tu","We","Th","Fr","Sa","Su"};
	private int colorDesactivated;
	private int colorActivated;
	private Typeface mRobotoBold;
	private Typeface mRobotoNormal;
	private ToggleButton[] buttons;
	private ViewGroup[] buttonsHolder = new ViewGroup[7];
	private LayoutInflater inflater;
	private TextView spinner;
	
	private OnTaskCreationListener mListener;
	private boolean mEditMode=false;
	private String mEditingId;
	private Double oldValue=null;

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		try {
            mListener = (OnTaskCreationListener) activity;
        } catch (ClassCastException e) {
        	e.printStackTrace();
        	this.dismiss();
        }
	}
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        this.inflater =  getActivity().getLayoutInflater();
        View mainView =inflater.inflate(R.layout.add_task_dialog, null);
        builder.setView(mainView);
        this.colorActivated = this.getResources().getColor(R.color.days_black);
        this.colorDesactivated = this.getResources().getColor(R.color.days_gray);
        this.mRobotoBold = Typeface.create("sans-serif-condensed", Typeface.BOLD);
        this.mRobotoNormal = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
        this.taskNote = (EditText) mainView.findViewById(R.id.ET_taskNote);
        this.taskText = (EditText) mainView.findViewById(R.id.ET_taskText);
        this.taskValue = (EditText) mainView.findViewById(R.id.ET_taskValue);

		Bundle b = this.getArguments();
        int pos = b.getInt("pos", -1);
		String type;
		String text;
		if(pos != -1) {
			this.mEditMode=true;
		}


			type = b.getString("type");
			text = b.getString("text");

			this.taskText.setText(text);
			Log.d("AddTaskDialog","type="+type);
			hType = type.equals(daily.toString()) ?
					daily : type.equals(reward.toString()) ?
					reward : type.equals(todo.toString()) ?
					todo : habit;
			LinearLayout specialView = (LinearLayout) mainView.findViewById(R.id.repeat_days);
			if(hType == daily) {
				this.initializeSpecialButtons(specialView, mShortWeekDayStrings, repeat);
			}
			else if(hType== habit) {
				String[] texts = {getString(R.string.minus_sign),getString(R.string.plus_sign)};
				this.initializeSpecialButtons(specialView, texts, this.down_up);
			}
			else if(hType== todo) {
				LinearLayout dueDate = (LinearLayout) mainView.findViewById(R.id.due_date);
				this.initializeDatePicker(dueDate);
			} else if(hType== reward) {
				LinearLayout value = (LinearLayout) mainView.findViewById(R.id.value);
				value.setVisibility(View.VISIBLE);

			}

		if(mEditMode) {
//			this.reconstructObjectFrom(json);
            switch(hType) {
                case todo:
                    this.populate(((MainActivity)getActivity()).getTodo(pos));
                    break;
                case daily:
                    this.populate(((MainActivity)getActivity()).getDaily(pos));
                    break;
                case reward:
                    this.populate(((MainActivity)getActivity()).getReward(pos));
                    break;
                case habit:
                    this.populate(((MainActivity)getActivity()).getHabit(pos));
                    break;
            }
		}
        builder.setTitle(getString(R.string.new_task, hType.toString()))
               .setPositiveButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {

					   if (AddTaskDialog.this.taskText.getText().length() > 0) {

						   mListener.onTaskCreation(AddTaskDialog.this.createTask(), mEditMode);
					   } else {
						   mListener.onTaskCreationFail(getActivity().getString(R.string.task_creation_fail));
					   }
				   }
			   })
               .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
					   // User cancelled the dialog
				   }
			   });
        // Create the AlertDialog object and return it
        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return d;
    }



	private void initializeDatePicker(LinearLayout specialView) {
    	specialView.setVisibility(ViewGroup.VISIBLE);
    	this.spinner = (TextView) specialView.findViewById(R.id.due_date_spinner);
    	spinner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    DialogFragment newFragment = new DatePickerFragment(); 
			    newFragment.setTargetFragment(AddTaskDialog.this, 0);
			    newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
			}
		});
    }



	private void initializeSpecialButtons(LinearLayout specialView, String[] texts, final boolean[] resArray) {
    	int length = texts.length;
    	this.buttonsHolder = new ViewGroup[length];
    	this.buttons = new ToggleButton[length];
    	specialView.setVisibility(ViewGroup.VISIBLE);
        for (int i = 0; i < length; i++) {
            final ViewGroup viewgroup = (ViewGroup) inflater.inflate(R.layout.button_day_of_week,
            		specialView, false);
            final ToggleButton button = (ToggleButton) viewgroup.getChildAt(0);
            button.setText(texts[i]);
            button.setTextOn(texts[i]);
            button.setTextOff(texts[i]);
            specialView.addView(viewgroup);
            this.buttonsHolder[i] = viewgroup;
            this.buttons[i] = button;
            if(i<5) {
            	buttonOn(i);
            	resArray[i] = true;
            }
        }
        for(int i=0;i<length;i++) {
            final int buttonIndex = i;
        	this.buttonsHolder[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					buttons[buttonIndex].toggle();
                    final boolean checked = buttons[buttonIndex].isChecked();
                    if (checked) {
                    	buttonOn(buttonIndex);
                    	resArray[buttonIndex] = true;
                    } else {
                    	buttonOff(buttonIndex);
                    	resArray[buttonIndex] = false;
                    }

				}
			});
        }
	}
	private void buttonOff(int index) {
		buttons[index].setChecked(false);
		buttons[index].setTextColor(this.colorDesactivated);
		buttons[index].setTypeface(this.mRobotoNormal);
	}
	private void buttonOn(int index) {
		buttons[index].setChecked(true);
		buttons[index].setTextColor(this.colorActivated);
		buttons[index].setTypeface(this.mRobotoBold);
	}
	public HabitItem createTask() {
		HabitItem h=null;

		String notes = this.taskNote.getText().toString();
		String text = this.taskText.getText().toString();
		if(text != null) {
			switch(this.hType) {
			case daily:
                Daily.Days d = DaysUtils.getDaysFromBooleans(repeat);
				h = new Daily(null, notes, null, text, oldValue!=null?oldValue:0, false, d);
				break;
			case habit:
				h = new Habit(null, notes, null, text, oldValue!=null?oldValue:0, this.down_up[1], this.down_up[0	]);
				break;
			case todo:
				h = new ToDo(null, notes, null, text, oldValue!=null?oldValue:0, false, toDoDate);
				break;
			case reward:
					int value=20;
					try {
						value = Double.valueOf(this.taskValue.getText().toString()).intValue();
					} catch(Exception e) {
                        Crashlytics.logException(e);
						e.printStackTrace();
					}
				h = new Reward(null, notes, null, text, value);
			}
			if(mEditMode) {
				h.setId(this.mEditingId);
			}
		}

		return h;
		
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.set(year, monthOfYear, dayOfMonth);
		Date d = c.getTime();
		String date = android.text.format.DateFormat.getMediumDateFormat(getActivity()).format(d);
        int month = (c.get(Calendar.MONTH)+1)%12;
		this.toDoDate = getString(R.string.format_todo_date, month, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR));
		Log.v("AddTaskDialog","modifying todoDate to:" + toDoDate);
		if(this.spinner!=null)
			this.spinner.setText(date);
	}


	private void populate(ToDo result) {
		populate((HabitItem) result);
		if(result.getDate()!=null) {
			String[] newDate = result.getDate().split("-");
            if(newDate.length!=3) {
                newDate = result.getDate().split("/");
            }
            if(newDate.length==3) {
                Calendar c = Calendar.getInstance();
                Log.e("date", "date is here!!!" + result.getDate());
                c.set(Integer.valueOf(newDate[2]), (Integer.valueOf(newDate[0])+11)%12, Integer.valueOf(newDate[1]));
                Date d = c.getTime();
                String date = android.text.format.DateFormat.getMediumDateFormat(getActivity()).format(d);
                int month = c.get(Calendar.MONTH);
                this.toDoDate = getString(R.string.format_todo_date,month, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR));
                if(this.spinner!=null)
                    this.spinner.setText(date);
            }
		}
	}

	private void populate(Reward result) {
		populate((HabitItem) result);
		this.taskValue.setText(Double.valueOf(result.getValue()).intValue() + "");
	}

	private void populate(Habit result) {
		populate((HabitItem) result);
		if(result.isUp() ){
			buttonOn(1);
			down_up[1]=true;
		} else {
			buttonOff(1);
			down_up[1]=false;

		}

		if(result.isDown() ){
			buttonOn(0);
			down_up[0]=true;

		} else {
			buttonOff(0);
			down_up[0]=false;

		}

	}

	private void populate(Daily result) {
		populate((HabitItem) result);
		int length = buttons.length;
		if(result.getRepeat()!=null) {
			for (int i = 0; i < length; i++) {
				if(DaysUtils.getBooleansFromDays(result.getRepeat())[i]) {
					buttonOn(i);
					repeat[i] = true;

				} else {
					buttonOff(i);
					repeat[i] = false;

				}
			}
		}
	}

	private void populate(HabitItem result) {
		if(this.taskNote!=null)
			this.taskNote.setText(result.getNotes());
		if(this.taskText!=null)
			this.taskText.setText(result.getText());
		this.mEditingId = result.getId();
		this.oldValue = result.getValue();
	}
}
