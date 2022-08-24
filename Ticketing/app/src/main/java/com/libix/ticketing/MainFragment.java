package com.libix.ticketing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.Message;
import com.microsoft.graph.models.extensions.Shared;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IMessageCollectionPage;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements OnTicketCloseClick {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private final static String[] SCOPES = {"Mail.ReadWrite"};
    /* Azure AD v2 Configs */
    final static String AUTHORITY = "https://login.microsoftonline.com/common";
    private ISingleAccountPublicClientApplication mSingleAccountApp;

    private static final String TAG = MainFragment.class.getSimpleName();

    /* UI & Debugging Variables */
    Button signInButton;
    Button signOutButton;
    Button callGraphApiInteractiveButton;
    Button callGraphApiSilentButton;
    TextView logTextView;
    TextView currentUserTextView;
    TextView openTicketsCountTextView;
    TextView ongoingTicketsCountTextView;
    String openTicketsCount;
    String ongoingTicketsCount;
    RecyclerView ticketsRecyclerView;
    TicketAdapter myTicketAdapter;
    ProgressBar progressBar;
    Animation scaleUp, scaleDown;

    ArrayList<TroubleTicket> ticketArrayList = new ArrayList<TroubleTicket>(); // This one is used to load tickets from the server database
    ArrayList<TroubleTicket> graphDataArrayList = new ArrayList<>(); // This one is used to update the database with the information from the GraphAPI call

    /**
     * Receive subject from the OnTicketCloseClick interface and pass it on to the
     * ticketDeleteRequest method
     *
     * @param subject The subject of the ticket/email to be deleted
     */
    @Override
    public void onTicketCloseClick(String subject, String graphId, String solution){
        Log.d("Close", "Subject now in main activity");
        Log.d("Close", "Subject in MainActivity: " + subject);

        Log.d("Notifications", "About to send notification");
        String title = "Ticket: \"" + subject + "\" has been closed";
        PushNotificationSender notificationSender = new PushNotificationSender("/topics/all",
                title,
                solution,
                getActivity().getApplicationContext(),
                getActivity()
        );
        notificationSender.sendNotifications();

        ticketDeleteRequest(subject, solution); // Delete from database
        //Log.d("CloseGraphID", "graph_id: " + graph_id);

        mSingleAccountApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, getAuthSilentCallback("delete", graphId)); // Delete email from graphAPI
    }

    public void onTicketStatusClick(String subject){

        Toast.makeText(getContext(), "Status updated for ticket: " + subject, Toast.LENGTH_SHORT).show();
        ticketStatusRequest(subject);
    }

    public void onNotTicketClick(String subject, String graphId){

        Toast.makeText(getContext(), "Non-ticket removed", Toast.LENGTH_SHORT).show();
        nonTicketRequest(subject);
        mSingleAccountApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, getAuthSilentCallback("delete", graphId));
    }


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

        openTicketsCountGetRequest();
        ongoingTicketsCountGetRequest();

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        initializeUI(view);

        PublicClientApplication.createSingleAccountPublicClientApplication(getActivity().getApplicationContext(),
                R.raw.auth_config, new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(ISingleAccountPublicClientApplication application) {
                        mSingleAccountApp = application;
                        loadAccount();
                    }
                    @Override
                    public void onError(MsalException exception) {
                        displayError(exception);
                    }
                });

        ticketsRecyclerView = view.findViewById(R.id.ticket_recycler_view);

        myTicketAdapter = new TicketAdapter(getContext(), ticketArrayList, this);
        ticketsRecyclerView.setAdapter(myTicketAdapter);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean notifBoolean = sharedPreferences.getBoolean("notifications", true);
        if (notifBoolean) {
            FirebaseMessaging.getInstance().subscribeToTopic("all");
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
        }

        return view;
    }

    // When app comes to the foreground, load existing account to determine if user is signed in
    private void loadAccount() {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(@Nullable IAccount activeAccount) {
                // You can use the account data to update your UI or your app database.
                updateUI(activeAccount);
            }

            @Override
            public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    performOperationOnSignOut();
                }
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                displayError(exception);
            }
        });
    }

    private void initializeUI(View view){
        signInButton = view.findViewById(R.id.signIn);
        signInButton.setVisibility(View.VISIBLE);
        callGraphApiSilentButton = view.findViewById(R.id.callGraphSilent);
        //callGraphApiInteractiveButton = findViewById(R.id.callGraphInteractive);
        signOutButton = view.findViewById(R.id.clearCache);
        logTextView = view.findViewById(R.id.txt_log);
        //currentUserTextView = findViewById(R.id.current_user);
        progressBar = view.findViewById(R.id.progress_bar);
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        openTicketsCountTextView = view.findViewById(R.id.open_tickets_count);
        ongoingTicketsCountTextView = view.findViewById(R.id.ongoing_tickets_count);



        //Sign in user
        signInButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (mSingleAccountApp == null) {
                    return;
                }
                mSingleAccountApp.signIn(getActivity(), null, SCOPES, getAuthInteractiveCallback());
            }
        });

        //Sign out user
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleAnimations(signOutButton);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getResources().getString(R.string.sign_out_text));
                builder.setTitle(getResources().getString(R.string.sign_out_title));
                builder.setCancelable(false);
                builder.setNegativeButton(getResources().getString(R.string.sign_out_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(getResources().getString(R.string.sign_out_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSingleAccountApp == null){
                            return;
                        }
                        mSingleAccountApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
                            @Override
                            public void onSignOut() {
                                updateUI(null);
                                performOperationOnSignOut();
                                ticketArrayList.clear();
                            }
                            @Override
                            public void onError(@NonNull MsalException exception){
                                displayError(exception);
                            }
                        });
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        //Silent
        callGraphApiSilentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleAnimations(callGraphApiSilentButton);
                if (mSingleAccountApp == null){
                    return;
                }
                getActivity().runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        logTextView.setText("Syncing...");
                    }
                });
                progressBar.setVisibility(View.VISIBLE);
                mSingleAccountApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, getAuthSilentCallback("get", ""));
            }
        });
    }

    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated");
                /* Update UI */
                updateUI(authenticationResult.getAccount());
                /* call graph */
                callGraphAPI(authenticationResult, "get", "");
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                displayError(exception);
            }
            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    private SilentAuthenticationCallback getAuthSilentCallback(final String caseString, final String graph_id) {
        return new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.d(TAG, "Successfully authenticated");
                callGraphAPI(authenticationResult, caseString, graph_id);
            }
            @Override
            public void onError(MsalException exception) {
                Log.d(TAG, "Authentication failed: " + exception.toString());
                displayError(exception);
            }
        };
    }

    /**
     * Sends a POST request to the server in order to add tickets to the open-tickets table
     *
     * @param json JSON string of the ticket objects created after calling
     *             the Microsoft Graph API
     */
    private void ticketPostRequest(String json){
        String url = Config.ADDTICKETSURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        try {
            Log.d("URLRequest", "building the json object");
            JSONArray postData = new JSONArray(json);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.POST, url, postData, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray res) {

                            if (res != null) {
                                Log.d("URLResponse", res.toString());
                                // Calling the database to update the tickets ArrayList
                                ticketGetRequest();
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Make a toast "sync failed" message
                            Log.d("URLRequest", "Unable to receive JSON response from server");
                            Log.d("URLRequest", error.toString());
                        }
                    });

            queue.add(jsonArrayRequest);
            //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void ticketGetRequest(){
        String url = Config.GETTICKETSURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        try {
            Log.d("URLGetRequest", "Loading tickets from DB");

            final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray res) {

                            if (res != null) {
                                Log.d("URLGetResponse", res.toString());
                                jsonArrayToArrayList(res);
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
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

    protected void ticketDeleteRequest(String subject, String solution){

        String url = Config.DELETETICKETURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject subjectJson = new JSONObject();

        try {
            Log.d("URLDeleteRequest", "making the json object");
            subjectJson.put("subject", subject);
            subjectJson.put("solution", solution);

            Log.d("URLDeleteRequest", subjectJson.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, subjectJson, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {
                            if (res != null) {
                                Log.d("URLDeleteResponse", res.toString());
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("URLDeleteRequest", "Unable to receive JSON response from server");
                            Log.d("URLDeleteRequest", error.toString());
                        }
                    });

            queue.add(jsonObjectRequest);
            //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void ticketStatusRequest(String subject){

        String url = Config.UPDATETICKETURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject subjectJson = new JSONObject();

        try {
            subjectJson.put("subject", subject);

            Log.d("URLUpdateRequest", subjectJson.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, subjectJson, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {
                            if (res != null) {
                                Log.d("URLUpdateResponse", res.toString());
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("URLUpdateRequest", "Unable to receive JSON response from server");
                            Log.d("URLUpdateRequest", error.toString());
                        }
                    });

            queue.add(jsonObjectRequest);
            //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void nonTicketRequest(String subject){

        String url = Config.DELETENONTICKETURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JSONObject subjectJson = new JSONObject();

        try {
            subjectJson.put("subject", subject);

            Log.d("URLUpdateRequest", subjectJson.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, subjectJson, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {
                            if (res != null) {
                                Log.d("URLNonTicketResponse", res.toString());
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("URLNonTicketRequest", "Unable to receive JSON response from server");
                            Log.d("URLNonTicketRequest", error.toString());
                        }
                    });

            queue.add(jsonObjectRequest);
            //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openTicketsCountGetRequest(){
        String url = Config.GETOPENTICKETSCOUNTURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        try {
            Log.d("URLGetRequest", "Loading counter from DB");

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {

                            if (res != null) {
                                Log.d("OpenTicketsCounterGetResponse", res.toString());
                                try {
                                    openTicketsCount = res.get("count").toString();
                                    Log.d("OpenTicketsCounterGetResponse", openTicketsCount);
                                    openTicketsCountTextView.setText(openTicketsCount);
                                } catch (JSONException e){
                                    e.printStackTrace();
                                    openTicketsCountTextView.setText("N/A");
                                    Log.d("OpenTicketCounterGetResponse", "Could not get monthly count from server");
                                }
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("CounterGetRequest", "Unable to receive response from server");
                            Log.d("CounterGetRequest", error.toString());
                            openTicketsCountTextView.setText("N/A");
                        }
                    });

            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: Add open and ongoing tickets to a bundle and save in onsavedinstancestate, or figure out another way to save it
    private void ongoingTicketsCountGetRequest(){
        String url = Config.GETONGOINGTICKETSCOUNTURL;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        try {
            Log.d("URLGetRequest", "Loading counter from DB");

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject res) {

                            if (res != null) {
                                Log.d("OngoingTicketsCounterGetResponse", res.toString());
                                try {
                                    ongoingTicketsCount = res.get("count").toString();
                                    Log.d("OngoingTicketsCounterGetResponse", ongoingTicketsCount);
                                    ongoingTicketsCountTextView.setText(ongoingTicketsCount);
                                } catch (JSONException e){
                                    e.printStackTrace();
                                    ongoingTicketsCountTextView.setText("N/A");
                                    Log.d("OngoingTicketCounterGetResponse", "Could not get monthly count from server");
                                }
                            }
                        }
                    }, new Response.ErrorListener(){

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("CounterGetRequest", "Unable to receive response from server");
                            Log.d("CounterGetRequest", error.toString());
                            openTicketsCountTextView.setText("N/A");
                        }
                    });

            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Consider using method overloading?
    private void callGraphAPI(IAuthenticationResult authenticationResult, String caseString, String graph_id) {

        final String accessToken = authenticationResult.getAccessToken();

        IGraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(new IAuthenticationProvider() {
                            @Override
                            public void authenticateRequest(IHttpRequest request) {
                                Log.d(TAG, "Authenticating request," + request.getRequestUrl());
                                request.addHeader("Authorization", "Bearer " + accessToken);
                            }
                        })
                        .buildClient();

        switch(caseString) {
            case "get":
                graphClient
                        .me()
                        .mailFolders("Inbox")
                        .messages()
                        .buildRequest()
                        .select("id, subject, body, from")
                        .top(50) //TODO: Replace 50 with a global var
                        .get(new ICallback<IMessageCollectionPage>() {
                            @Override
                            public void success(IMessageCollectionPage iMessageCollectionPage) {
                                displayGraphResult(iMessageCollectionPage.getRawObject());
                            }

                            @Override
                            public void failure(ClientException ex) {
                                displayError(ex);
                            }
                        });
                break;
            case "delete":
                graph_id = graph_id.substring(1, graph_id.length()-1); // remove quotation marks
                Log.d("DeleteAPI", "Graph_ID: " + graph_id);

                graphClient
                        .me()
                        .messages(graph_id)
                        .buildRequest()
                        .delete(new ICallback<Message>() {
                            @Override
                            public void success(Message message) {
                                Log.d("DeleteAPI", "Successfully deleted!");
                            }

                            @Override
                            public void failure(ClientException ex) {
                                Log.d("DeleteAPIError", ex.toString());
                                displayError(ex);
                            }
                        });
                break;
            default:
                Toast.makeText(getContext(), "Graph API not called", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void updateUI(@Nullable final IAccount account) {
        if (account != null) {
            signInButton.setEnabled(false);
            signInButton.setVisibility(View.GONE);
            signOutButton.setEnabled(true);
            signOutButton.setVisibility(View.VISIBLE);
            //callGraphApiInteractiveButton.setEnabled(true);
            callGraphApiSilentButton.setEnabled(true);
            callGraphApiSilentButton.setVisibility(View.VISIBLE);
            //currentUserTextView.setText(account.getUsername());
        } else {
            signInButton.setEnabled(true);
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setEnabled(false);
            signOutButton.setVisibility(View.INVISIBLE);
            //callGraphApiInteractiveButton.setEnabled(false);
            callGraphApiSilentButton.setEnabled(false);
            callGraphApiSilentButton.setVisibility(View.INVISIBLE);
            //currentUserTextView.setText("");
            logTextView.setText(R.string.outlook_disconnected);
        }
    }

    private void displayError(@NonNull final Exception exception) {

        getActivity().runOnUiThread(new Runnable(){

            @Override
            public void run(){
                logTextView.setText(exception.toString());
            }
        });
    }

    private void removeProgressBar(){

        getActivity().runOnUiThread(new Runnable(){

            @Override
            public void run(){
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayGraphResult(@NonNull final JsonObject graphResponse) {

        final ArrayList<String> textToDisplay = new ArrayList();
        JsonArray myJsonArray = graphResponse.getAsJsonArray("value");

        // If no tickets received from server, display no tickets message
        if (myJsonArray.toString().equals("[]")) {
            Log.d("NOTICKETSJSON", myJsonArray.toString());
            removeProgressBar();
            logTextView.setText("No tickets. Congrats!");
            return;
        }

        // Clearing the both Array Lists before putting in new tickets (avoid duplicate tickets)
        ticketArrayList.clear();
        graphDataArrayList.clear();
        // Loop through myJsonArray get the subject, body, and from address
        for(JsonElement message: myJsonArray){
            if ( message instanceof JsonElement ) {

                // Temporary
                String subject = message.getAsJsonObject().get("subject").toString();
                subject = removeQuotations(subject);

                // Ignoring replies to an email
                if (subject.contains("Re:")){
                    continue;
                }

                // Add the subject
                textToDisplay.add(message.getAsJsonObject().get("subject").toString());

                String graphId = message.getAsJsonObject().get("id").toString();
                Log.d("GraphID", "Subject: " + subject + "Graph ID: " + graphId);

                // Add the body
                String body = message.getAsJsonObject().get("body").getAsJsonObject().get("content").toString();
                body = removeHtmlTags(body);
                body = removeQuotations(body);
                textToDisplay.add(body);

                // Temporary
                //Drafts result in null from address (that could be the issue). INVESTIGATE (not necessary now because I won't be saving drafts).
                String from = message.getAsJsonObject().get("from").getAsJsonObject().get("emailAddress").getAsJsonObject().get("address").toString();
                from = removeQuotations(from);
                // There has to be a better way to do this
                textToDisplay.add(message.getAsJsonObject().get("from").getAsJsonObject().get("emailAddress").getAsJsonObject().get("address").toString()); //added from address

                TroubleTicket troubleTicket = new TroubleTicket(subject, body, from, "Open", graphId, "");
                //Log.d("Tickets", troubleTicket.toString());

                //Add troubleTicket to ArrayList for loading on to RecyclerView and converting to json
                graphDataArrayList.add(troubleTicket);
            }
        }

        // Convert the ArrayList of ticket objects received from the Graph API into a JSON String
        String json = new Gson().toJson(graphDataArrayList);
        Log.d("JSON", json);
        Log.d("URLRequest", "About to send url request to DO droplet server");
        ticketPostRequest(json);
        openTicketsCountGetRequest();
        ongoingTicketsCountGetRequest();

        //logTextView.setText(graphResponse.toString());

        getActivity().runOnUiThread(new Runnable(){

            @Override
            public void run() {
                logTextView.setText("Fetched data from server");
            }
        });
        //logTextView.setText(textToDisplay.toString());
    }

    private void performOperationOnSignOut() {
        final String signOutText = "Signed Out.";
        //currentUserTextView.setText("");
        Toast.makeText(getContext(), signOutText, Toast.LENGTH_SHORT)
                .show();
    }

    private String removeHtmlTags(String body){
        // Remove all html tags using regex matching pattern
        body = body.replaceAll("\\<.*?\\>", "");

        // Using plain text matching to replace new line characters
        body = body.replace("\\r", "");
        body = body.replace("\\n", " ");

        return body;
    }

    private String removeQuotations(String text){

        text = text.replaceAll("^\"|\"$", "");
        return text;
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
                    ticketArrayList.add(troubleTicket);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JsonToArrayList", "Conversion error");
                }
            }
            Log.d("TicketArrList", ticketArrayList.toString());
            myTicketAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void scaleAnimations (Button button){
        button.startAnimation(scaleUp);
        button.startAnimation(scaleDown);
    }
}