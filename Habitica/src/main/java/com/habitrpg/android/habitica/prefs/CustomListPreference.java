package com.habitrpg.android.habitica.prefs;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import java.util.ArrayList;

public class CustomListPreference extends ListPreference {
    private CustomListPreferenceAdapter customListPreferenceAdapter = null;
    private Context mContext;
    private LayoutInflater mInflater;
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private ArrayList<RadioButton> rButtonList;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String value = "";
    private EditText text;

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        rButtonList = new ArrayList<RadioButton>();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        entries = getEntries();
        entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        customListPreferenceAdapter = new CustomListPreferenceAdapter(mContext);
        builder.setAdapter(customListPreferenceAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (value != null && value.equals("custom")) {
                    Log.d("commiting", "custom value");
                    value = text.getText().toString();
                }
                if (value == null || !value.startsWith("http")) {
                    Log.v("Commiting", "changing values to default");
                    value = mContext.getString(R.string.SP_address_default);
                }

                Log.d("Commiting", "putting string: " + value);
                editor.putString(mContext.getString(R.string.SP_address), value);
                editor.commit();
                arg0.dismiss();
            }
        });

    }

    private class CustomListPreferenceAdapter extends BaseAdapter {
        public CustomListPreferenceAdapter(Context context) {

        }

        public int getCount() {
            return entries.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                // do whatever you need here, for me I wanted the last item to be greyed out and unclickable
                if (position < 2) {
                    NormalHolder holder = null;

                    row = mInflater.inflate(R.layout.normal_list_preference_row, parent, false);
                    if (prefs.getString(mContext.getString(R.string.SP_address), "0").equals(entryValues[position])) {
                        holder = new NormalHolder(row, position, true);
                        Log.v("Prefs", entryValues[position] + " already exists at position " + position);
                        value = entryValues[position].toString();
                    } else {
                        holder = new NormalHolder(row, position, false);
                    }


                    row.setTag(holder);
                    // row.setClickable(true);
                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (!rButtonList.get(position).isChecked()) {
                                for (RadioButton rb : rButtonList) {
                                    if (rb.getId() == position) {
                                        Log.d("row.OnClickListener - " + position, "isChecked");
                                        if (!rb.isChecked())
                                            rb.setChecked(true);
                                    }
                                }
                            }
                        }
                    });
                } else {
                    row = mInflater.inflate(R.layout.custom_list_preference_row, parent, false);
                    String fromPref = prefs.getString(mContext.getString(R.string.SP_address), "0");
                    boolean flag = false;
                    for (CharSequence entry : entryValues) {
                        if (entry.toString().equals(fromPref)) {
                            Log.v("ListPref", entry.toString() + " already exists");
                            flag = true;
                        }
                    }
                    CustomHolder holder;
                    if (!flag && fromPref != null && !fromPref.equals("")) {
                        holder = new CustomHolder(row, position, fromPref, true);
                        value = "custom";
                    } else {
                        holder = new CustomHolder(row, position, "", false);

                    }
                    row.setTag(holder);
                }
            }

            return row;
        }

        class NormalHolder {
            private TextView text = null;
            private RadioButton rButton = null;

            NormalHolder(View row, int position, boolean isCheked) {
                text = (TextView) row.findViewById(R.id.custom_list_view_row_text_view);
                text.setText(entries[position]);
                rButton = (RadioButton) row.findViewById(R.id.custom_list_view_row_radio_button);
                rButton.setId(position);
                rButton.setChecked(isCheked);

                // also need to do something to check your preference and set the right button as checked

                rButtonList.add(rButton);
                rButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            for (RadioButton rb : rButtonList) {
                                if (rb.getId() != buttonView.getId())
                                    rb.setChecked(false);
                            }

                            int index = buttonView.getId();
                            value = entryValues[index].toString();
                            Log.v("NormalHolder.onCheckedChanged", "putting string" + value);
                        }
                    }
                });
            }

        }

        class CustomHolder {
            private RadioButton rButton = null;

            CustomHolder(View row, int position, String pref, boolean checked) {
                rButton = (RadioButton) row.findViewById(R.id.custom_list_view_row_radio_button);
                rButton.setId(position);
                rButton.setChecked(checked);

                text = (EditText) row.findViewById(R.id.ET_prefs_customText);
                text.setText(pref);
                text.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            if (!rButton.isChecked())
                                rButton.setChecked(true);
                        }
                    }
                });
                getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                rButtonList.add(rButton);

                // also need to do something to check your preference and set the right button as checked

                rButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            for (RadioButton rb : rButtonList) {
                                if (rb != buttonView)
                                    rb.setChecked(false);
                            }
                            if (!text.hasFocus())
                                text.requestFocus();
                            value = String.valueOf("custom");
                            Log.v("CustomHolder.onCheckedChanged", "putting string" + value);
                        } else {
                            if (text.hasFocus())
                                text.clearFocus();
                        }
                    }
                });
            }
        }
    }
}