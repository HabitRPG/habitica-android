package com.habitrpg.android.habitica.helpers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import javax.inject.Inject;

public class RemindersManager {

    @Inject
    CrashlyticsProxy crashlyticsProxy;
    private DateFormat dateFormater;

    public RemindersManager(String taskType) {
        Objects.requireNonNull(HabiticaBaseApplication.Companion.getUserComponent()).inject(this);
        if (taskType.equals("todo")) {
            dateFormater = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        } else {
            dateFormater = DateFormat.getTimeInstance(DateFormat.SHORT);
        }
    }

    @Nullable
    private RemindersItem createReminderFromDateString(String dateString) {
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
            crashlyticsProxy.logException(e);
            return null;
        }
    }

    public String reminderTimeToString(Date time) {
        return dateFormater.format(time);
    }

    public void createReminderTimeDialog(@Nullable ReminderTimeSelectedCallback callback, String taskType,
                                         Context context, @Nullable RemindersItem reminder) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        if (taskType.equals("todo")) {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_date_time_dialogue);
            dialog.setTitle("Select Date and Time");

            Button dialogConfirmButton = dialog.findViewById(R.id.customDialogConfirmButton);
            TimePicker dialogTimePicker = dialog.findViewById(R.id.timePicker);
            DatePicker dialogDatePicker = dialog.findViewById(R.id.datePicker);

            dialogTimePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(context));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                    Integer.toString(Calendar.getInstance().getFirstDayOfWeek()));
            FirstDayOfTheWeekHelper firstDayOfTheWeekHelper =
                    FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
                dialogDatePicker.getCalendarView().setFirstDayOfWeek(
                        firstDayOfTheWeekHelper.getFirstDayOfTheWeek());
            } else {
                dialogDatePicker.setFirstDayOfWeek(firstDayOfTheWeekHelper.getFirstDayOfTheWeek());
            }

            dialogConfirmButton.setOnClickListener(view -> {
                int day = dialogDatePicker.getDayOfMonth();
                int month = dialogDatePicker.getMonth();
                int year = dialogDatePicker.getYear();
                int hour1 = 0;
                int minute1 = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hour1 = dialogTimePicker.getHour();
                    minute1 = dialogTimePicker.getMinute();
                } else {
                    hour1 = dialogTimePicker.getCurrentHour();
                    minute1 = dialogTimePicker.getCurrentMinute();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, hour1, minute1, 0);

                onReminderTimeSelected(callback, reminder, calendar);
                dialog.hide();
            });
            dialog.show();
        } else {
            TimePickerDialog timePickerDialog;
            timePickerDialog = new TimePickerDialog(context, (timePicker, selectedHour, selectedMinute) -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), selectedHour, selectedMinute, 0);

                onReminderTimeSelected(callback, reminder, calendar);
            }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
            timePickerDialog.setTitle("Select Time");
            timePickerDialog.show();
        }
    }

    private void onReminderTimeSelected(@Nullable ReminderTimeSelectedCallback callback, @Nullable RemindersItem reminder, Calendar calendar) {
        RemindersItem remindersItem = reminder;
        if (remindersItem == null) {
            remindersItem = createReminderFromDateString(dateFormater.format(calendar.getTime()));
        } else {
            remindersItem.setTime(calendar.getTime());
        }
        if (callback != null) {
            callback.onReminderTimeSelected(remindersItem);
        }
    }

    public interface ReminderTimeSelectedCallback {
        void onReminderTimeSelected(@Nullable RemindersItem remindersItem);
    }
}
