package com.habitrpg.android.habitica.helpers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by keithholliday on 7/12/16.
 */
public class RemindersManager {

    DateFormat dateFormater = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

    public RemindersItem createReminderFromDateString(String dateString) {
        try {
            Date date = dateFormater.parse(dateString);
            RemindersItem item = new RemindersItem();
            UUID randomUUID = UUID.randomUUID();
            item.setId(randomUUID.toString());
            item.setStartDate(date);
            item.setTime(date);
            return item;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String reminderTimeToString (Date time) {
        return dateFormater.format(time);
    }

    public void createDialogeForEditText(EditText editText, String taskType, Context context) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        if (taskType.equals("todo")) {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_date_time_dialogue);
            dialog.setTitle("Select Date and Time");

            Button dialogConfirmButton = (Button) dialog.findViewById(R.id.customDialogConfirmButton);
            TimePicker dialogTimePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
            DatePicker dialogDatePicker = (DatePicker) dialog.findViewById(R.id.datePicker);

            dialogConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int day = dialogDatePicker.getDayOfMonth();
                    int month = dialogDatePicker.getMonth();
                    int year =  dialogDatePicker.getYear();
                    int hour = dialogTimePicker.getCurrentHour();
                    int minute = dialogTimePicker.getCurrentMinute();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day, hour, minute, 0);

                    editText.setText(dateFormater.format(calendar.getTime()));
                    dialog.hide();
                }
            });
            dialog.show();
        } else {
            TimePickerDialog timePickerDialog;
            timePickerDialog = new TimePickerDialog(context, (timePicker, selectedHour, selectedMinute) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), selectedHour, selectedMinute, 0);
                editText.setText(dateFormater.format(calendar.getTime()));
            }, hour, minute, true);
            timePickerDialog.setTitle("Select Time");
            timePickerDialog.show();
        }
    }

}
