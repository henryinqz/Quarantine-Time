package com.henry.quarantinetime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private String startDate;
    private String startTime;
    private long startDateMilliseconds;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String START_DATE = "startDate";
    public static final String START_DATE_MILLISECONDS = "startDateMilliseconds";
    public static final String START_TIME = "startTime";

    private Calendar calendarStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button butPickDate = (Button) findViewById(R.id.button_pickdate);
        butPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // EDIT TO OPEN TO EXISTING START DATE IF IT EXISTS
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        loadStartDate();
        if (startDateMilliseconds != -1) { // Date is set
            updateViews();
        }

        //calendarCurrent = Calendar.getInstance();
        /*String currentDate = DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTime());

        TextView textViewDate = findViewById(R.id.text_view_date);
        textViewDate.setText(currentDate);*/
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.YEAR, year);
        calendarStart.set(Calendar.MONTH, month);
        calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE,0);
        calendarStart.set(Calendar.SECOND,0);
        calendarStart.set(Calendar.MILLISECOND,0);

        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");

        startDate = DateFormat.getDateInstance(DateFormat.LONG).format(calendarStart.getTime());
        startDateMilliseconds = calendarStart.getTimeInMillis();

        saveStartDate();
        updateViews();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendarStart.set(Calendar.MINUTE, minute);
        startTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendarStart.getTime());
        startDateMilliseconds = calendarStart.getTimeInMillis();

        saveStartDate();
        updateViews();
    }

    public void saveStartDate() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(START_DATE, startDate);
        editor.putString(START_TIME, startTime);
        editor.putLong(START_DATE_MILLISECONDS, startDateMilliseconds);
        editor.apply();

        //Toast.makeText(this, "Date saved", Toast.LENGTH_SHORT);
    }

    public void loadStartDate() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        startDate = sharedPref.getString(START_DATE, "date_null");
        startTime = sharedPref.getString(START_TIME, "time_null");
        startDateMilliseconds = sharedPref.getLong(START_DATE_MILLISECONDS, -1);
    }

    public void updateViews() {
        long end = Calendar.getInstance().getTimeInMillis();
        //long start = calendarStart.getTimeInMillis();
        long start = startDateMilliseconds;

        long days = TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
        long hours = TimeUnit.MILLISECONDS.toHours(Math.abs(end - start)) - TimeUnit.DAYS.toHours(days);
        long mins = TimeUnit.MILLISECONDS.toMinutes(Math.abs(end - start)) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);

        TextView textViewDaysSince = (TextView) findViewById(R.id.text_view_start_time);
        SpannableStringBuilder strDate = new SpannableStringBuilder(startDate);
        strDate.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0, strDate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder strTime = new SpannableStringBuilder(startTime);
        strTime.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0, strTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewDaysSince.setText("Days since quarantine began on ");
        textViewDaysSince.append(strDate);
        textViewDaysSince.append(" at ");
        textViewDaysSince.append(strTime);
        textViewDaysSince.append(":");

        TextView textViewElapsedTime = (TextView) findViewById(R.id.text_view_elapsed_time);
        SpannableStringBuilder strDays = new SpannableStringBuilder(days + " days");
        strDays.setSpan(new RelativeSizeSpan(2f), 0, strDays.length(), 0);
        strDays.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0, strDays.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        textViewElapsedTime.setText(strDays);
        textViewElapsedTime.append("\n" + hours + " hours, " + mins + " minutes");
    }
}
