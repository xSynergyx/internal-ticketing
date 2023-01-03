package com.libix.ticketing;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    RequestQueue myQueue;
    TextView nameTextView;
    TextView ticketsClosedTextView;

    public ProfileFragment() {
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
        myQueue = Volley.newRequestQueue(requireContext());
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTextView = view.findViewById(R.id.profile_name);
        ticketsClosedTextView = view.findViewById(R.id.profile_tickets_closed);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();

        profileGetRequest(email);
    }

    public void setProfile(JSONObject user) throws JSONException {

        /*
        currentUser.setFirstName(user.getString("first_name"));
        currentUser.setLastName(user.getString("last_name"));
        currentUser.setTicketsClosed(user.getInt("tickets_closed"));
         */

        String fullName = user.getString("first_name") + " " + user.getString("last_name");
        nameTextView.setText(fullName);
        ticketsClosedTextView.setText(Integer.toString(user.getInt("tickets_closed")));
    }

    // Get user details from DB
    private void profileGetRequest(String email) {

        JSONObject emailJson = new JSONObject();
        try {
            emailJson.put("email", email);
            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.GETPROFILE, emailJson, (JSONObject res) -> {
                if (res != null) {
                    try {
                        // If user has not been saved to DB before, get details from Firebase and send to DB
                        if (res.getString("email").equals("null")) {
                            profileCreateRequest();
                        } else {
                            setProfile(res);
                            Log.d("ProfileGetResponse", res.toString());
                        }
                    } catch (JSONException e) {
                        Log.d("ProfileGetResponse", e.toString());
                    }
                } else {
                    Log.d("ProfileGetResponse", "About to create profile");
                }
            }, (VolleyError error) -> {
                Log.d("ProfileGetResponse", "Unable to receive response from server. Error: " + error.toString());
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create new user in DB
    private void profileCreateRequest() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String email = firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName(); //Gets full name all together
        String[] names = name.split(" ");
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : " ";

        int ticketsClosed = 0;

        JSONObject userJson = new JSONObject();
        try {
            userJson.put("email", email);
            userJson.put("first_name", firstName);
            userJson.put("last_name", lastName);
            userJson.put("ticketsClosed", ticketsClosed);
            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.CREATEPROFILE, userJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("ProfileCreateResponse", res.toString());
                }
            }, (VolleyError error) -> {
                Log.d("ProfileCreateResponse", "Unable to receive response from server. Error: " + error.toString());
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}