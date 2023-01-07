package com.libix.ticketing;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class StatisticsFragment extends Fragment {

    RequestQueue myQueue;
    String[] months;
    public static final int START_YEAR = 2022;

    TextView closedTicketsTv;
    TextView patronHelpTv;
    Button getStatsButton;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myQueue = Volley.newRequestQueue(requireContext());
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        months = getResources().getStringArray(R.array.months);
        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH);

        closedTicketsTv = view.findViewById(R.id.statistics_closed_tickets);
        patronHelpTv = view.findViewById(R.id.statistics_patron_help);
        getStatsButton = view.findViewById(R.id.get_statistics_button);

        NumberPicker monthPicker = view.findViewById(R.id.month_picker);
        NumberPicker yearPicker = view.findViewById(R.id.year_picker);

        // Set up month and year pickers
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        yearPicker.setMinValue(START_YEAR);
        yearPicker.setMaxValue(currentYear);
        // Set current month/year for pickers
        monthPicker.setValue(currentMonth);
        yearPicker.setValue(currentYear);

        getStats(yearPicker.getValue(), monthPicker.getValue() + 1);

        getStatsButton.setOnClickListener(v -> {
            // Adding 1 to the month so we get back 1 indexed month instead of 0 indexed.
            getStats(yearPicker.getValue(), monthPicker.getValue() + 1);
        });

        /*
        // Theme_DeviceDefault_Light_Dialog
        DatePickerDialog newDate = new DatePickerDialog(requireContext(), android.R.style.Theme_DeviceDefault_Light_Dialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Log.d("New date", String.valueOf(i));
            }
        }, year, month, day);
        newDate.show();


        // Remove day selector from date picker dialog
        int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
        Log.d("Day spinner", String.valueOf(daySpinnerId));
        if (daySpinnerId != 0) {
            View daySpinner = newDate.findViewById(daySpinnerId);
            if (daySpinner != null) {
                Log.d("Day spinner", "daySpinner view not null");
                daySpinner.setVisibility(View.GONE);
            } else {
                Log.d("Day spinner", "daySpinner view be null");
            }
        }

        Date minDate = new GregorianCalendar(2022, Calendar.OCTOBER, 1).getTime();
        newDate.getDatePicker().setMinDate(minDate.getTime());
        newDate.getDatePicker().setMaxDate(new Date().getTime());

         */
    }

    public void getStats(int year, int month){
        String stringMonth = "";

        if (month <= 9) {
            stringMonth = "0" + month;
        }

        getMonthlyTicketsClosed(year, stringMonth);
        getMonthlyPatronHelp(year, stringMonth);
    }

    private void getMonthlyTicketsClosed(int year, String month){
        JSONObject yearMonthJson = new JSONObject();

        //Snackbar.make(view,"Updated stats", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        try {
            yearMonthJson.put("month", year + "-" + month);
            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.GETMONTHLYCLOSEDTICKETSURL, yearMonthJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("MonthlyClosedTicketsGetResponse", res.toString());
                    try {
                        closedTicketsTv.setText(res.get("count").toString());
                    } catch (JSONException e){
                        e.printStackTrace();
                        closedTicketsTv.setText("?");
                        Log.d("MonthlyClosedTicketsGetResponse", "Could not get monthly count from server");
                    }
                }
            }, (VolleyError error) -> {
                Log.d("MonthlyClosedTicketsGetResponse", "Unable to receive response from server. Error: " + error.toString());
                closedTicketsTv.setText("?");
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMonthlyPatronHelp(int year, String month){
        JSONObject yearMonthJson = new JSONObject();

        try {
            yearMonthJson.put("month", year + "-" + month);
            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.GETMONTHLYPATRONHELPURL, yearMonthJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("MonthlyPatronHelpGetResponse", res.toString());
                    try {
                        if (!res.get("monthly_total").toString().equals("null")){
                            patronHelpTv.setText(res.get("monthly_total").toString());
                        } else {
                            patronHelpTv.setText("0");
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                        patronHelpTv.setText("?");
                        Log.d("MonthlyPatronHelpGetResponse", "Could not get monthly count from server");
                    }
                }
            }, (VolleyError error) -> {
                Log.d("MonthlyPatronHelpGetResponse", "Unable to receive response from server. Error: " + error.toString());
                patronHelpTv.setText("?");
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}