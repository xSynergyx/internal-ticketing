package com.example.ticketing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SearchActivity extends MainActivity {

    //ListView listView;
    ClosedTicketAdapter closedTicketAdapter;
    RecyclerView closedTicketsRecyclerView;
    ArrayList<TroubleTicket> closedTicketArrayList = new ArrayList<TroubleTicket>();
    Button searchButton;
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //downloadJSON(Config.CLOSEDTICKETSSCRIPT);

        closedTicketsRecyclerView = findViewById(R.id.closed_tickets_recycler_view);
        searchButton = findViewById(R.id.search_button);
        searchEditText = (EditText)findViewById(R.id.search_edit_text_view);

        closedTicketAdapter = new ClosedTicketAdapter(this, closedTicketArrayList);
        closedTicketsRecyclerView.setAdapter(closedTicketAdapter);
        closedTicketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Handle empty string case
                closedTicketArrayList.clear();
                String searchText = searchEditText.getText().toString().trim();
                searchEditText.getText().clear();
                closedTicketGetRequest(searchText);
            }
        });
    }

    /*
    * OLD STUFF
    private void downloadJSON(final String urlWebService){

        class DownloadJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s){
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    loadIntoListView(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null){
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        DownloadJSON getJSON = new DownloadJSON();
        getJSON.execute();
    }


    private void loadIntoListView(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        String[] closed_tickets = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject obj = jsonArray.getJSONObject(i);
            closed_tickets[i] = obj.getString("subject") + " " + obj.getString("solution");
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, closed_tickets);
        listView.setAdapter(arrayAdapter);
    }

     */
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
            //TODO: Handle no search results case
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
                            Log.d("URLGetRequest", "Unable to receive JSON response from server");
                            Log.d("URLGetRequest", error.toString());
                        }
                    });

            queue.add(jsonArrayRequest);
            //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
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