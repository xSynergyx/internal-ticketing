package com.libix.ticketing;

import static android.widget.Toast.LENGTH_LONG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CounterActivity extends AppCompatActivity {


    Button decrementButton;
    Button incrementButton;
    Button submitCountButton;
    Button resetButton;

    TextView monthlyCounterTextView;
    TextView dailyCounterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        decrementButton = findViewById(R.id.decrement_button);
        incrementButton = findViewById(R.id.increment_button);
        submitCountButton = findViewById(R.id.submit_counter_button);
        resetButton = findViewById(R.id.reset_counter);

        monthlyCounterTextView = findViewById(R.id.monthly_counter);
        dailyCounterTextView = findViewById(R.id.daily_counter);

        counterGetRequest();

        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Clicked decrement", LENGTH_LONG).show();
            }
        });

        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Clicked increment", LENGTH_LONG).show();
            }
        });

        submitCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterGetRequest();
                Toast.makeText(getBaseContext(), "Clicked submit", LENGTH_LONG).show();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Clicked reset", LENGTH_LONG).show();
            }
        });
    }

    private void counterGetRequest(){
        String url = Config.GETCOUNTERURL;
        RequestQueue queue = Volley.newRequestQueue(this);

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

                                } catch (JSONException e){
                                    e.printStackTrace();
                                    Log.d("CounterGetResponse", "Could not get counter from server");
                                }
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Make a toast "sync failed" message
                            Log.d("CounterGetRequest", "Unable to receive response from server");
                            Log.d("CounterGetRequest", error.toString());
                            dailyCounterTextView.setText("error");
                        }
                    });

            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void counterUpdateRequest(){
        String url = Config.UPDATECOUNTERURL;
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject counterJson = new JSONObject();

        try {
            Log.d("URLPostRequest", "Loading counter from DB");
            counterJson.put("counter", 5);

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, counterJson, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {

                            if (res != null) {
                                Log.d("URLPostResponse", res.toString());
                                //Do something
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Make a toast "sync failed" message
                            Log.d("URLGetRequest", "Unable to receive JSON response from server");
                            Log.d("URLGetRequest", error.toString());
                        }
                    });

            queue.add(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}