package com.henry.quarantinetime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private String startDate;
    private String startTime;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String START_DATE = "startDate";
    public static final String START_TIME = "startTime";
    //public static final String START_DATE_MILLISECONDS = "startDateMilliseconds";
    public static final String START_YEAR = "startDateYear";
    public static final String START_MONTH = "startDateMonth";
    public static final String START_DAY = "startDateDay";
    public static final String START_HOUR = "startDateHour";
    public static final String START_MIN = "startDateMin";

    private LocalDateTime startDateTime;
    private LocalDateTime currDateTime;
    //private Calendar calendarStart;

    private BroadcastReceiver minuteUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button butPickDate = (Button) findViewById(R.id.button_pickdate);
        butPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker;
                if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null"))) {
                    datePicker = new DatePickerFragment();
                } else { // Opens DatePickerFragment to existing start date
                    datePicker = new DatePickerFragment(startDateTime.getYear(), startDateTime.getMonthValue()-1, startDateTime.getDayOfMonth());
                }
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        loadStartDate();
        updateViews();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        startDateTime = LocalDateTime.of(year, month+1, dayOfMonth, 0, 0, 0); // I believe LocalDateTime monthvalue needs month+1
        startDate = startDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/YYYY"));

        /*
        calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.YEAR, year);
        calendarStart.set(Calendar.MONTH, month);
        calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE,0);
        calendarStart.set(Calendar.SECOND,0);
        calendarStart.set(Calendar.MILLISECOND,0);
        */

        // Open TimePickerFragment
        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");

        /*
        startDate = DateFormat.getDateInstance(DateFormat.LONG).format(calendarStart.getTime());
        startDateMilliseconds = calendarStart.getTimeInMillis();
        */

        saveStartDate();
        updateViews();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        startDateTime = startDateTime.withHour(hourOfDay);
        startDateTime = startDateTime.withMinute(minute);
        startTime = startDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        /*
        calendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendarStart.set(Calendar.MINUTE, minute);
        startTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendarStart.getTime());
        startDateMilliseconds = calendarStart.getTimeInMillis();
        */

        saveStartDate();
        updateViews();
    }

    public void saveStartDate() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(START_DATE, startDate);
        editor.putString(START_TIME, startTime);
        editor.putInt(START_YEAR, startDateTime.getYear());
        editor.putInt(START_MONTH, startDateTime.getMonthValue());
        editor.putInt(START_DAY, startDateTime.getDayOfMonth());
        editor.putInt(START_HOUR, startDateTime.getHour());
        editor.putInt(START_MIN, startDateTime.getMinute());
        editor.apply();
    }

    public void loadStartDate() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        startDate = sharedPref.getString(START_DATE, "date_null");
        startTime = sharedPref.getString(START_TIME, "time_null");

        int year = sharedPref.getInt(START_YEAR, 0);
        int month = sharedPref.getInt(START_MONTH, 1);
        int day = sharedPref.getInt(START_DAY, 1);
        int hour = sharedPref.getInt(START_HOUR, 0);
        int min = sharedPref.getInt(START_MIN, 0);

        startDateTime = LocalDateTime.of(year, month, day, hour, min, 0);
    }

    public void updateViews() {
        /*
        long end = Calendar.getInstance().getTimeInMillis();
        //long start = calendarStart.getTimeInMillis();
        long start = startDateMilliseconds;

        long days = TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
        long hours = TimeUnit.MILLISECONDS.toHours(Math.abs(end - start)) - TimeUnit.DAYS.toHours(days);
        long mins = TimeUnit.MILLISECONDS.toMinutes(Math.abs(end - start)) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
        */

        // Exit method if date/time is not set
        if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null")))  {
            return;
        }

        // Update current date
        currDateTime = LocalDateTime.now();
        currDateTime = currDateTime.withSecond(0);
        currDateTime = currDateTime.withNano(0);

        Period elapsedPeriod = Period.between(startDateTime.toLocalDate(), currDateTime.toLocalDate());
        int years = elapsedPeriod.getYears();
        int months = elapsedPeriod.getMonths();
        int days = elapsedPeriod.getDays();

        Duration elapsedDuration = Duration.between(startDateTime.toLocalTime(), currDateTime.toLocalTime());
        long hours = elapsedDuration.toHours();
        long mins = elapsedDuration.toMinutes() - (hours*60);

        // Days since (top text)
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

        // Time elapsed (middle text)
        TextView textViewElapsedTime = (TextView) findViewById(R.id.text_view_elapsed_time);
        if (months > 0 || years > 0) { // Show months/years (bold)
            SpannableStringBuilder months_text = new SpannableStringBuilder(months + " month");
            if (months != 1) months_text.append("s"); // Plural if month count is not 1

            months_text.setSpan(new RelativeSizeSpan(2f), 0, months_text.length(), 0);
            months_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0, months_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (years > 0) { // Show years
                SpannableStringBuilder years_text = new SpannableStringBuilder(years + " year");
                if (years != 1) years_text.append("s"); // Plural if month count is not 1

                years_text.setSpan(new RelativeSizeSpan(2f), 0, years_text.length(), 0);
                years_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0, years_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                textViewElapsedTime.setText(years_text);
                textViewElapsedTime.append("\n" + months_text + ", " + days + " days,");
                textViewElapsedTime.append("\n" + hours + " hours, " + mins + " minutes");
            } else {
                textViewElapsedTime.setText(months_text);
                textViewElapsedTime.append("\n" + days + " days, " + hours + " hours, " + mins + " minutes");
            }
        } else if (months == 0){ // Show days (bold)
            SpannableStringBuilder days_text = new SpannableStringBuilder(days + " days");
            days_text.setSpan(new RelativeSizeSpan(2f), 0, days_text.length(), 0);
            days_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0, days_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            textViewElapsedTime.setText(days_text);
            textViewElapsedTime.append("\n" + hours + " hours, " + mins + " minutes");
        }
    }

    public void startMinuteUpdater() { // Update timer every minute
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        minuteUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateViews();
            }
        };

        registerReceiver(minuteUpdateReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMinuteUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(minuteUpdateReceiver);
    }
}
