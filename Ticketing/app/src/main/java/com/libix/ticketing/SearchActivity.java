package com.libix.ticketing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    ClosedTicketAdapter closedTicketAdapter;
    RecyclerView closedTicketsRecyclerView;
    ArrayList<TroubleTicket> closedTicketArrayList = new ArrayList<TroubleTicket>();
    Button searchButton;
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        closedTicketsRecyclerView = findViewById(R.id.closed_tickets_recycler_view);
        searchButton = findViewById(R.id.search_button);
        searchEditText = (EditText)findViewById(R.id.search_edit_text_view);

        closedTicketAdapter = new ClosedTicketAdapter(this, closedTicketArrayList);
        closedTicketsRecyclerView.setAdapter(closedTicketAdapter);
        closedTicketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closedTicketArrayList.clear();
                String searchText = searchEditText.getText().toString().trim();
                searchEditText.getText().clear();
                closedTicketGetRequest(searchText);
            }
        });
    }

    private void closedTicketGetRequest(String searchText){
        String url = Config.SEARCHTICKETSURL;
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONArray searchTermJsonArray = new JSONArray();
        JSONObject searchTextObj = new JSONObject();

        try {
            searchTextObj.put("search", searchText);
            searchTermJsonArray.put(searchTextObj);
            Log.d("jsonArray", searchTermJsonArray.toString());
            Log.d("URLSearchRequest", "Loading tickets from DB");
            
            final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.POST, url, searchTermJsonArray, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray res) {

                            if (res != null) {
                                Log.d("URLSearchResponse", res.toString());
                                jsonArrayToArrayList(res);
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Make a toast "sync failed" message
                            Log.d("URLSearchResponse", "Unable to receive JSON response from server");
                            Toast.makeText(SearchActivity.this, "No results found for \"" + searchText + "\"", Toast.LENGTH_LONG).show();
                            Log.d("URLSearchResponse", error.toString());
                        }
                    });

            queue.add(jsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void jsonArrayToArrayList(JSONArray jsonArr){

        if (jsonArr != null) {
            for (int i = 0; i<jsonArr.length(); i++){
                try {
                    JSONObject ticket = jsonArr.getJSONObject(i);
                    String subject = ticket.get("subject").toString();
                    String body = ticket.get("body").toString();
                    String from = ticket.get("from_address").toString();
                    String status = ticket.get("status").toString();
                    String graphId = ticket.get("graph_id").toString();
                    String solutionText = ticket.get("solution").toString();
                    TroubleTicket troubleTicket = new TroubleTicket(subject, body, from, status, graphId, solutionText);
                    closedTicketArrayList.add(troubleTicket);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JsonToArrayList", "Conversion error");
                }
            }
            Log.d("TicketArrList", closedTicketArrayList.toString());
            closedTicketAdapter.notifyDataSetChanged();
        }
    }
}