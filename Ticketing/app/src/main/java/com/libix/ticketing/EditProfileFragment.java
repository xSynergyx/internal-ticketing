package com.libix.ticketing;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EditProfileFragment extends Fragment {

    private static final String USER = "USER";

    RequestQueue myQueue;
    private User currentUser;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditProfileFragment newInstance(String param1, String param2) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = getArguments().getParcelable(USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myQueue = Volley.newRequestQueue(requireContext());
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        TextInputEditText firstNameTi = view.findViewById(R.id.edit_user_first_name_et);
        TextInputLayout firstNameLayoutTi = view.findViewById(R.id.edit_user_first_name_layout_et);

        TextInputEditText lastNameTi = view.findViewById(R.id.edit_user_last_name_et);
        TextInputLayout lastNameLayoutTi = view.findViewById(R.id.edit_user_last_name_layout_et);

        MaterialButton editUserSubmitButton = view.findViewById(R.id.edit_user_submit_button);


        // Set up enable-editing button for each edit text field
        firstNameLayoutTi.setEndIconOnClickListener(v -> firstNameTi.setEnabled(true));
        lastNameLayoutTi.setEndIconOnClickListener(v -> lastNameTi.setEnabled(true));

        firstNameTi.setText(currentUser.getFirstName());
        lastNameTi.setText(currentUser.getLastName());

        editUserSubmitButton.setOnClickListener( v -> {

            if (firstNameTi.isEnabled()) {
                currentUser.setFirstName(Objects.requireNonNull(firstNameTi.getText()).toString());
                firstNameTi.setEnabled(false);
            }
            if (lastNameTi.isEnabled()) {
                currentUser.setLastName(Objects.requireNonNull(lastNameTi.getText()).toString());
                lastNameTi.setEnabled(false);
            }
            /*
            // TODO: Updated image stuff
            if (photoFile != null) {
                listing?.setImage(ParseFile(photoFile))
            }
            */

            saveProfileChanges();
        });
    }

    public void saveProfileChanges() {
        Log.d("ProfileChanges", "Saved the changes: " + currentUser.getFirstName());
        //TODO: Send changes to server
        updateProfileRequest();
    }

    private void updateProfileRequest() {
        String email = currentUser.email;
        String firstName = currentUser.firstName;
        String lastName = currentUser.lastName;
        int ticketsClosed = currentUser.tickets_closed;

        JSONObject userJson = new JSONObject();
        try {
            userJson.put("email", email);
            userJson.put("first_name", firstName);
            userJson.put("last_name", lastName);
            userJson.put("tickets_closed", ticketsClosed);
            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.UPDATEPROFILE, userJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("ProfileUpdateResponse", "WOOOOO" + res.toString());
                }
            }, (VolleyError error) -> {
                Log.d("ProfileUpdateResponse", "Unable to receive response from server. Error: " + error.toString());
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}