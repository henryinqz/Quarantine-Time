package com.henry.quarantinetime;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {
    private boolean existing_date = false;
    private int year = 0;
    private int month = 0;
    private int day = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (existing_date == false) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        return datePickerDialog;

    }
    public DatePickerFragment() {
        this.existing_date = false;
    }
    public DatePickerFragment(int existing_year, int existing_month, int existing_day) {
        this.year = existing_year;
        this.month = existing_month;
        this.day = existing_day;
        this.existing_date = true;
    }
}
