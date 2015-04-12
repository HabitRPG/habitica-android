package com.magicmicky.habitrpgmobileapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

/*
 * This class is useful for using inside of ListView that needs to have checkable items.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private CheckedTextView _checkbox;

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// find checked text view
		int childCount = getChildCount();
		/*for (int i = 0; i < childCount; ++i) {
			View v = getChildAt(i);
			if (v instanceof CheckedTextView) {
				_checkbox = (CheckedTextView)v;
				Log.v("Checkable", "found CheckedTextViex");
			}
		}*/
		View v = findViewById(R.id.TV_habit);
		if(v==null) {
			v= findViewById(R.id.TV_reward);
		}
		if(v==null) {
			v=findViewById(R.id.TV_title);
		}
		if(v instanceof CheckedTextView) {
			_checkbox = (CheckedTextView)v;
		}
	}

	@Override
	public boolean isChecked() {
		return _checkbox != null ? _checkbox.isChecked() : false;
	}

	@Override
	public void setChecked(boolean checked) {
		if (_checkbox != null) {
			_checkbox.setChecked(checked);
			if(checked) {
				this.setBackgroundResource(R.drawable.list_focused_holo);
			} else {
				this.setBackgroundResource(R.drawable.item_background);
			}
		}
	}

	@Override
	public void toggle() {
		if (_checkbox != null) {

			_checkbox.toggle();
		}
	}
}