package com.henry.quarantinetime;

import androidx.annotation.NonNull;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private String startDate;
    private String startTime;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String START_DATE = "startDate";
    public static final String START_TIME = "startTime";
    public static final String START_YEAR = "startDateYear";
    public static final String START_MONTH = "startDateMonth";
    public static final String START_DAY = "startDateDay";
    public static final String START_HOUR = "startDateHour";
    public static final String START_MIN = "startDateMin";

    private LocalDateTime startDateTime;
    private LocalDateTime currDateTime;

    public static final String SETTINGS_FULLDAYS = "showFullDays";
    private boolean showFullDaysOnly = true;

    private BroadcastReceiver minuteUpdateReceiver;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset: // Reset start date
                if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null")))  { // No start date
                    Toast.makeText(getApplicationContext(), "Choose a start date", Toast.LENGTH_LONG).show();
                } else {
                    final TextView textViewStartTime = (TextView) findViewById(R.id.text_view_start_time);
                    final TextView textViewElapsedTime = (TextView) findViewById(R.id.text_view_elapsed_time);
                    textViewStartTime.setText(R.string.days_since); // to reset strings
                    textViewElapsedTime.setText(R.string.elapsed_time);

                    clearStartDate();
                    loadStartDate();
                    updateViews();
                    Toast.makeText(getApplicationContext(), "Start date reset", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.date_format:
                return true;
            case R.id.date_format_fullday:
                if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null")))  { // Exit method if date/time is not set
                    Toast.makeText(getApplicationContext(), "Choose a start date", Toast.LENGTH_LONG).show();
                } else {
                    showFullDaysOnly = true;
                    saveDateFormat();
                    updateViews();
                    Toast.makeText(getApplicationContext(), "Switched to full day format", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.date_format_years:
                if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null")))  { // Exit method if date/time is not set
                    Toast.makeText(getApplicationContext(), "Choose a start date", Toast.LENGTH_LONG).show();
                } else {
                    showFullDaysOnly = false;
                    saveDateFormat();
                    updateViews();
                    Toast.makeText(getApplicationContext(), "Switched to year/month format", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

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

        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE); // Get shared pref of date format
        showFullDaysOnly = sharedPref.getBoolean(SETTINGS_FULLDAYS, false);

        final TextView textViewElapsedTime = (TextView) findViewById(R.id.text_view_elapsed_time);
        textViewElapsedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null")))  { // Exit method if date/time is not set
                    Toast.makeText(getApplicationContext(), "Choose a start date", Toast.LENGTH_LONG).show();
                } else {
                    showFullDaysOnly = !showFullDaysOnly; // Toggle
                    saveDateFormat();
                    updateViews();
                    Toast.makeText(getApplicationContext(), "Switched date format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadStartDate();
        updateViews();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        startDateTime = LocalDateTime.of(year, month+1, dayOfMonth, 0, 0, 0); // I believe LocalDateTime monthvalue needs month+1
        startDate = startDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/YYYY"));

        // If time is not set yet, default to 12:00AM
        if (startTime.equals("time_null")) {
            startDateTime = startDateTime.withHour(0);
            startDateTime = startDateTime.withMinute(0);
            startTime = startDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        }

        // Open TimePickerFragment
        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");

        saveStartDate();
        updateViews();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        startDateTime = startDateTime.withHour(hourOfDay);
        startDateTime = startDateTime.withMinute(minute);
        startTime = startDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

        saveStartDate();
        updateViews();
    }

    public void saveDateFormat() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE); // Update shared prefs

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SETTINGS_FULLDAYS, showFullDaysOnly);
        editor.apply();
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

    public void clearStartDate() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(START_DATE);
        editor.remove(START_TIME);
        editor.remove(START_YEAR);
        editor.remove(START_MONTH);
        editor.remove(START_DAY);
        editor.remove(START_HOUR);
        editor.remove(START_MIN);
        editor.remove(SETTINGS_FULLDAYS);
        editor.apply();
    }

    public void updateViews() {
        // Exit method if date/time is not set
        if (startDateTime == null || (startDate.equals("date_null") && startTime.equals("time_null")))  {
            return;
        }

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

        // Update current date
        currDateTime = LocalDateTime.now();
        currDateTime = currDateTime.withSecond(0);
        currDateTime = currDateTime.withNano(0);
        Duration elapsedDuration = Duration.between(startDateTime.toLocalTime(), currDateTime.toLocalTime());
        long hours = elapsedDuration.toHours();
        long mins = elapsedDuration.toMinutes() - (hours*60);

        // Time elapsed (middle text)
        TextView textViewElapsedTime = (TextView) findViewById(R.id.text_view_elapsed_time);

        if (showFullDaysOnly == true) { // DAYS ONLY
            long days_full = ChronoUnit.DAYS.between(startDateTime.toLocalDate(), currDateTime.toLocalDate());

            SpannableStringBuilder days_full_text = new SpannableStringBuilder(days_full + " days");
            days_full_text.setSpan(new RelativeSizeSpan(2f), 0, days_full_text.length(), 0);
            days_full_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, days_full_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewElapsedTime.setText(days_full_text);
            textViewElapsedTime.append("\n" + hours + " hours, " + mins + " minutes");
        } else { // YEARS/MONTHS/DAYS
            Period elapsedPeriod = Period.between(startDateTime.toLocalDate(), currDateTime.toLocalDate());
            int years = elapsedPeriod.getYears();
            int months = elapsedPeriod.getMonths();
            int days = elapsedPeriod.getDays();

            if (months > 0 || years > 0) { // Show months/years (bold)
                SpannableStringBuilder months_text = new SpannableStringBuilder(months + " month");
                if (months != 1) months_text.append("s"); // Plural if month count is not 1

                months_text.setSpan(new RelativeSizeSpan(2f), 0, months_text.length(), 0);
                months_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, months_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (years > 0) { // Show years
                    SpannableStringBuilder years_text = new SpannableStringBuilder(years + " year");
                    if (years != 1) years_text.append("s"); // Plural if month count is not 1

                    years_text.setSpan(new RelativeSizeSpan(2f), 0, years_text.length(), 0);
                    years_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, years_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textViewElapsedTime.setText(years_text);
                    textViewElapsedTime.append("\n" + months_text + ", " + days + " days,");
                    textViewElapsedTime.append("\n" + hours + " hours, " + mins + " minutes");
                } else {
                    textViewElapsedTime.setText(months_text);
                    textViewElapsedTime.append("\n" + days + " days, " + hours + " hours, " + mins + " minutes");
                }
            } else if (months == 0) { // Show days (bold)
                SpannableStringBuilder days_text = new SpannableStringBuilder(days + " days");
                days_text.setSpan(new RelativeSizeSpan(2f), 0, days_text.length(), 0);
                days_text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, days_text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                textViewElapsedTime.setText(days_text);
                textViewElapsedTime.append("\n" + hours + " hours, " + mins + " minutes");
            }
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
