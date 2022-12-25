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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;

public class ProfileFragment extends Fragment {

    RequestQueue myQueue;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getProfile(view);
    }

    public void getProfile(@NonNull View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();

        TextView nameTextView = view.findViewById(R.id.profile_name);
        nameTextView.setText(name);
    }

    //TODO: finish this method to get user profile from db
    private void profileGetRequest(){
        try {
            Log.d("ticketGetRequest", "Loading tickets from DB");
            myQueue.add(VolleyUtils.jsonArrayGetRequest(Config.GETTICKETSURL, (JSONArray res) -> {
                if (res != null) {
                    Log.d("ticketGetResponse", res.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}