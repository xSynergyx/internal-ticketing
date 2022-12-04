package com.libix.ticketing;

import static android.widget.Toast.LENGTH_LONG;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("SetTextI18n")
public class CounterFragment extends Fragment {

    public CounterFragment() {
        // Required empty public constructor
    }

    Button decrementButton;
    Button incrementButton;
    Button incrementFiveButton;
    FloatingActionButton submitCountButton;
    SwipeRefreshLayout swipeRefreshLayout;

    TextView monthlyCounterTextView;
    TextView dailyCounterTextView;
    RequestQueue myQueue;

    int countDelta = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_counter, container, false);
        myQueue = Volley.newRequestQueue(requireContext());

        decrementButton = view.findViewById(R.id.decrement_button);
        incrementButton = view.findViewById(R.id.increment_button);
        incrementFiveButton = view.findViewById(R.id.increment_five_button);
        submitCountButton = view.findViewById(R.id.submit_counter_button);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_container);

        monthlyCounterTextView = view.findViewById(R.id.monthly_counter);
        dailyCounterTextView = view.findViewById(R.id.daily_counter);

        counterGetRequest(false);
        countDelta = 0;

        decrementButton.setOnClickListener(v -> {
            if (!(dailyCounterTextView.getText().equals("error"))) {
                int currentCount = Integer.parseInt(dailyCounterTextView.getText().toString());
                currentCount--;
                countDelta--;

                dailyCounterTextView.setText(Integer.toString(currentCount));
            }

        });

        incrementButton.setOnClickListener(v -> {
            if (!(dailyCounterTextView.getText().equals("error"))) {
                int currentCount = Integer.parseInt(dailyCounterTextView.getText().toString());
                currentCount++;
                countDelta++;

                dailyCounterTextView.setText(Integer.toString(currentCount));
            }
        });

        incrementFiveButton.setOnClickListener(v -> {
            if (!(dailyCounterTextView.getText().equals("error"))) {
                int currentCount = Integer.parseInt(dailyCounterTextView.getText().toString());
                currentCount += 5;
                countDelta += 5;

                dailyCounterTextView.setText(Integer.toString(currentCount));
            }
        });

        submitCountButton.setOnClickListener(v -> {
            // Getting count update from server before updating it
            counterGetRequest(true);
            Toast.makeText(getContext(), "Submitted new count for the today", Toast.LENGTH_SHORT).show();
        });

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    Log.d("Swipe Refresh", "onRefresh called from SwipeRefreshLayout");

                    counterGetRequest(false);
                    countDelta = 0;
                    Toast.makeText(getContext(), "Refreshed count", Toast.LENGTH_SHORT).show();
                }
        );
        return view;
    }

    private void counterGetRequest(Boolean updateDB){

        try {
            Log.d("URLGetRequest", "Loading counter from DB");

            myQueue.add(VolleyUtils.jsonObjectGetRequest(Config.GETCOUNTERURL, (JSONObject res) -> {
                if (res != null) {
                    Log.d("CounterGetResponse", res.toString());
                    try {
                        dailyCounterTextView.setText(res.get("questions_answered").toString());
                        if (updateDB) {
                            counterUpdateRequest(false);
                        } else {
                            monthlyCounterGetRequest();
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Count sync failed", Toast.LENGTH_SHORT).show();
                        Log.d("CounterGetResponse", "Error setting the count in daily counter textview: " + e);
                    }
                }
            }, error -> {
                Log.d("CounterGetRequest", "Unable to receive response from server. Error: + error.toString()");
                dailyCounterTextView.setText(R.string.error);
                counterUpdateRequest(true);
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void counterUpdateRequest(Boolean newDay){

        JSONObject counterJsonObject = new JSONObject();
        JSONArray counterJson = new JSONArray();
        int newCount;

        if (!newDay) {
            newCount = Integer.parseInt(dailyCounterTextView.getText().toString()) + countDelta;
        } else {
            newCount = 0;
        }

        try {
            Log.d("URLPostRequest", "Loading counter from DB");
            counterJsonObject.put("counter", newCount);
            counterJson.put(counterJsonObject);

            myQueue.add(VolleyUtils.jsonArrayPostRequest(Config.UPDATECOUNTERURL, counterJson, (JSONArray res) -> {
                if (res != null) {
                    Log.d("URLPostResponse", res.toString());
                    dailyCounterTextView.setText(Integer.toString(newCount));
                    countDelta = 0; // reset the increment and decrement clicks
                    monthlyCounterGetRequest(); // Update monthly count only after updating daily count
                }
            }, (VolleyError error) -> {
                Log.d("CounterUpdateRequest", "Unable to receive JSON response from server. Error: " + error.toString());
                countDelta = 0; // reset it even if there's an error
                Toast.makeText(getContext(), "Unable to connect to server", LENGTH_LONG).show();
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void monthlyCounterGetRequest(){
        try {
            Log.d("MonthlyCounterGetRequest", "Loading monthly count from DB");

            myQueue.add(VolleyUtils.jsonObjectGetRequest(Config.GETMONTHLYCOUNTURL, (JSONObject res) -> {
                if (res != null) {
                    Log.d("MonthlyCounterGetResponse", res.toString());
                    try {
                        monthlyCounterTextView.setText(res.get("monthly_total").toString());
                        swipeRefreshLayout.setRefreshing(false);
                    } catch (JSONException e){
                        e.printStackTrace();
                        monthlyCounterTextView.setText(R.string.error);
                        Log.d("MonthlyCounterGetResponse", "Could not get monthly count from server");
                    }
                }
            }, (VolleyError error) -> {
                Log.d("MonthlyCounterGetResponse", "Unable to receive response from server. Error: " + error.toString());
                monthlyCounterTextView.setText(R.string.error);
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}