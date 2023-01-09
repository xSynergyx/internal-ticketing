package com.libix.ticketing;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClosedTicketsFragment extends Fragment {

    //TODO: Change closed closed-tickets layout (Use cardview within a constraint view)
    //      Add the user that closed it (name, not email) (handle null values)
    public ClosedTicketsFragment() {
        // Required empty public constructor
    }

    ClosedTicketAdapter closedTicketsAdapter;
    RecyclerView closedTicketsRecyclerView;
    ArrayList<TroubleTicket> closedTicketsArrayList = new ArrayList<>();
    ImageButton searchButton;
    EditText searchEditText;
    RequestQueue myQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_closed_tickets, container, false);
        myQueue = Volley.newRequestQueue(requireContext());

        closedTicketsRecyclerView = view.findViewById(R.id.closed_tickets_recycler_view);
        searchButton = view.findViewById(R.id.search_button);
        searchEditText = view.findViewById(R.id.search_edit_text_view);

        closedTicketsAdapter = new ClosedTicketAdapter(getContext(), closedTicketsArrayList);
        closedTicketsRecyclerView.setAdapter(closedTicketsAdapter);
        closedTicketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchButton.setOnClickListener(v -> {
            closedTicketsArrayList.clear();
            String searchText = searchEditText.getText().toString().trim();
            searchEditText.getText().clear();
            searchEditText.clearFocus();
            closedTicketGetRequest(searchText);
        });

        return view;
    }

    private void closedTicketGetRequest(String searchText){

        JSONArray searchTermJsonArray = new JSONArray();
        JSONObject searchTextObj = new JSONObject();
        try {
            searchTextObj.put("search", searchText);
            searchTermJsonArray.put(searchTextObj);
            Log.d("ClosedTicketsPostRequest", "Loading closed tickets from DB");

            myQueue.add(VolleyUtils.jsonArrayPostRequest(Config.SEARCHTICKETSURL, searchTermJsonArray, (JSONArray res) -> {
                if (res != null) {
                    Log.d("ClosedTicketsPostResponse", res.toString());
                    jsonArrayToArrayList(res);
                }
            }, (VolleyError error) -> {
                Log.d("ClosedTicketsPostResponse", "Unable to receive JSON response from server. Error: " + error.toString());
                Toast.makeText(getContext(), "No results found for \"" + searchText + "\"", Toast.LENGTH_LONG).show();
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
                    closedTicketsArrayList.add(troubleTicket);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JsonToArrayList", "Conversion error");
                }
            }
            Log.d("TicketArrList", closedTicketsArrayList.toString());
            closedTicketsAdapter.notifyDataSetChanged();
        }
    }
}