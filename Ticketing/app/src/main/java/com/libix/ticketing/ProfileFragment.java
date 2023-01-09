package com.libix.ticketing;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    RequestQueue myQueue;
    TextView nameTextView;
    TextView ticketsClosedTextView;
    User currentUser;
    RecyclerView profileTicketsRecyclerView;
    ClosedTicketAdapter profileTicketsAdapter;
    ArrayList<TroubleTicket> profileTicketsArrayList = new ArrayList<>();

    final String USER = "USER";

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

        profileTicketsRecyclerView = view.findViewById(R.id.profile_closed_tickets_rv);
        profileTicketsAdapter = new ClosedTicketAdapter(getContext(), profileTicketsArrayList);
        profileTicketsRecyclerView.setAdapter(profileTicketsAdapter);
        profileTicketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        nameTextView = view.findViewById(R.id.profile_name);
        ticketsClosedTextView = view.findViewById(R.id.profile_tickets_closed);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();

        profileGetRequest(email);
        view.findViewById(R.id.profile_upload_button).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable(USER, currentUser);
            Fragment editProfileFragment = new EditProfileFragment();
            editProfileFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, editProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    public void setProfile(JSONObject user) throws JSONException {

        String firstName = user.getString("first_name");
        String lastName = user.getString("last_name");
        String email = user.getString("email");
        int ticketsClosed = user.getInt("tickets_closed");

        String fullName = firstName + " " + lastName;
        nameTextView.setText(fullName);
        ticketsClosedTextView.setText(Integer.toString(user.getInt("tickets_closed")));

        currentUser = new User(email, firstName, lastName, ticketsClosed);

        // After successfully loading user's info, get the tickets closed by this user
        profileTicketsArrayList.clear();
        closedTicketGetRequest();
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
                    profileGetRequest(email);
                }
            }, (VolleyError error) -> {
                Log.d("ProfileCreateResponse", "Unable to receive response from server. Error: " + error.toString());
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: add closed_by to TroubleTicket or create new class called ClosedTicket with closed_by (so i can display who closed the ticket)
    private void closedTicketGetRequest(){

        JSONArray searchTermJsonArray = new JSONArray();
        JSONObject searchTextObj = new JSONObject();
        try {
            searchTextObj.put("email", currentUser.email);
            searchTermJsonArray.put(searchTextObj);
            Log.d("ProfileTicketsPostRequest", "Loading closed tickets from DB");

            myQueue.add(VolleyUtils.jsonArrayPostRequest(Config.GETPROFILETICKETS, searchTermJsonArray, (JSONArray res) -> {
                if (res != null) {
                    Log.d("ProfileTicketsPostResponse", res.toString());
                    jsonArrayToArrayList(res);
                }
            }, (VolleyError error) -> {
                Log.d("ProfileTicketsPostResponse", "Unable to receive JSON response from server. Error: " + error.toString());
                Snackbar.make(requireView(), "No tickets found for \"" + currentUser.firstName + "\"", Snackbar.LENGTH_SHORT).show();
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
                    profileTicketsArrayList.add(troubleTicket);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JsonToArrayList", "Conversion error");
                }
            }
            Log.d("TicketArrList", profileTicketsArrayList.toString());
            profileTicketsAdapter.notifyDataSetChanged();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*

    // Link below for the image chooser code
    // https://www.maxester.com/blog/2019/10/04/upload-file-image-to-the-server-using-volley-in-android/
    //TODO: File chooser kind of works. Having trouble retrieving the image


    //Global stuff

    private static final String ROOT_URL = "https://libixapi.com/update_image";
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST = 1 ;
    private Bitmap bitmap;
    private String filePath;
    ImageView imageView;
    TextView textView;



    // onCreate stuff:

    view.findViewById(R.id.profile_upload_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ((ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                if ((ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE))) {

                } else {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSIONS);
                }
            } else {
                Log.e("Else", "Else");
                showFileChooser();
            }
        }
    });



    // Other methods


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");


//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);


        Intent chooserIntent = Intent.createChooser(intent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            filePath = getPath(picUri);
            if (filePath != null) {
                try {

                    textView.setText("File Selected");
                    Log.d("filePath", String.valueOf(filePath));
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), picUri);
                    uploadBitmap(bitmap);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(
                        requireContext(),"no image selected",
                        Toast.LENGTH_LONG).show();
            }
        }

    }
    public String getPath(Uri uri) {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = requireContext().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }



    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadBitmap(final Bitmap bitmap) {

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, ROOT_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(requireContext()).add(volleyMultipartRequest);
    }

     */
}