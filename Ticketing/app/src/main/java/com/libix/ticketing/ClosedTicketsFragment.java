package com.libix.ticketing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClosedTicketsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClosedTicketsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ClosedTicketsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClosedTicketsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClosedTicketsFragment newInstance(String param1, String param2) {
        ClosedTicketsFragment fragment = new ClosedTicketsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    ClosedTicketAdapter closedTicketsAdapter;
    RecyclerView closedTicketsRecyclerView;
    ArrayList<TroubleTicket> closedTicketsArrayList = new ArrayList<TroubleTicket>();
    Button searchButton;
    EditText searchEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_closed_tickets, container, false);


        closedTicketsRecyclerView = view.findViewById(R.id.closed_tickets_recycler_view);
        searchButton = view.findViewById(R.id.search_button);
        searchEditText = (EditText) view.findViewById(R.id.search_edit_text_view);

        closedTicketsAdapter = new ClosedTicketAdapter(getContext(), closedTicketsArrayList);
        closedTicketsRecyclerView.setAdapter(closedTicketsAdapter);
        closedTicketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closedTicketsArrayList.clear();
                String searchText = searchEditText.getText().toString().trim();
                searchEditText.getText().clear();
                closedTicketGetRequest(searchText);
            }
        });

        return view;
    }

    private void closedTicketGetRequest(String searchText){
        String url = Config.SEARCHTICKETSURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());
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
                            Toast.makeText(getContext(), "No results found for \"" + searchText + "\"", Toast.LENGTH_LONG).show();
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