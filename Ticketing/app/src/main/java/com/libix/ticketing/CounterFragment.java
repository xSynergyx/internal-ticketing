package com.libix.ticketing;

import static android.widget.Toast.LENGTH_LONG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class CounterFragment extends Fragment {

    public CounterFragment() {
        // Required empty public constructor
    }

    Button decrementButton;
    Button incrementButton;
    Button submitCountButton;
    Button resetButton;

    TextView monthlyCounterTextView;
    TextView dailyCounterTextView;

    int countDelta = 0;

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
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        decrementButton = view.findViewById(R.id.decrement_button);
        incrementButton = view.findViewById(R.id.increment_button);
        submitCountButton = view.findViewById(R.id.submit_counter_button);
        //resetButton = findViewById(R.id.reset_counter);

        monthlyCounterTextView = view.findViewById(R.id.monthly_counter);
        dailyCounterTextView = view.findViewById(R.id.daily_counter);

        counterGetRequest(false);

        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentCount = Integer.parseInt(dailyCounterTextView.getText().toString());
                currentCount--;
                countDelta--;

                dailyCounterTextView.setText(Integer.toString(currentCount));
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentCount = Integer.parseInt(dailyCounterTextView.getText().toString());
                currentCount++;
                countDelta++;

                dailyCounterTextView.setText(Integer.toString(currentCount));
            }
        });

        submitCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting count update from server before updating it
                counterGetRequest(true);
                Toast.makeText(getContext(), "Submitted new count for the day", LENGTH_LONG).show();
            }
        });

        /*
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Clicked reset", LENGTH_LONG).show();
            }
        });
         */

        return view;
    }

    private void counterGetRequest(Boolean update){
        String url = Config.GETCOUNTERURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        try {
            Log.d("URLGetRequest", "Loading counter from DB");

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {

                            if (res != null) {
                                Log.d("CounterGetResponse", res.toString());
                                try {
                                    String answers = res.get("questions_answered").toString();
                                    dailyCounterTextView.setText(answers);
                                    if (update) counterUpdateRequest(false);

                                } catch (JSONException e){
                                    e.printStackTrace();
                                    // TODO: Add toast
                                    Log.d("CounterGetResponse", "Could not get counter from server");
                                }
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("CounterGetRequest", "Unable to receive response from server");
                            Log.d("CounterGetRequest", error.toString());
                            dailyCounterTextView.setText("error");
                            counterUpdateRequest(true);
                        }
                    });

            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void counterUpdateRequest(Boolean newDay){

        String url = Config.UPDATECOUNTERURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());
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

            final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.POST, url, counterJson, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray res) {

                            if (res != null) {
                                Log.d("URLPostResponse", res.toString());
                                dailyCounterTextView.setText(Integer.toString(newCount));
                                countDelta = 0; // reset the increment and decrement clicks
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("URLPostRequest", "Unable to receive JSON response from server");
                            Log.d("URLPostRequest", error.toString());
                            countDelta = 0; // reset it even if there's an error
                            Toast.makeText(getContext(), "Unable to connect to server", LENGTH_LONG).show();
                        }
                    });

            queue.add(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}