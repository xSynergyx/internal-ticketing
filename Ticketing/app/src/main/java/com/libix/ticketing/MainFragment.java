package com.libix.ticketing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.Message;
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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnTicketCloseClick {

    public MainFragment() {
        // Required empty public constructor
    }

    private final static String[] SCOPES = {"Mail.ReadWrite"};
    /* Azure AD v2 Configs */
    final static String AUTHORITY = "https://login.microsoftonline.com/common";
    private ISingleAccountPublicClientApplication mSingleAccountApp;

    private static final String TAG = MainFragment.class.getSimpleName();

    /* UI & Debugging Variables */
    Button signInButton;
    Button signOutButton;
    Button callGraphApiSilentButton;
    TextView logTextView;
    TextView openTicketsCountTextView;
    TextView ongoingTicketsCountTextView;
    String openTicketsCount;
    String ongoingTicketsCount;
    RecyclerView ticketsRecyclerView;
    TicketAdapter myTicketAdapter;
    ProgressBar progressBar;
    Animation scaleUp, scaleDown;
    final int maxTickets = 50;
    RequestQueue myQueue;

    ArrayList<TroubleTicket> ticketArrayList = new ArrayList<>(); // This one is used to load tickets from the server database
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

        /*
        Log.d("Notifications", "About to send notification");
        String title = "Ticket: \"" + subject + "\" has been closed";
        String key = keyGetRequest();
        PushNotificationSender notificationSender = new PushNotificationSender("/topics/all",
                title,
                solution,
                key,
                getActivity().getApplicationContext(),
                getActivity()
        );
        notificationSender.sendNotifications();*/

        ticketDeleteRequest(subject, solution); // Delete from database

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myQueue = Volley.newRequestQueue(requireContext());
        openTicketsCountGetRequest();
        ongoingTicketsCountGetRequest();

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        initializeUI(view);

        PublicClientApplication.createSingleAccountPublicClientApplication(requireActivity().getApplicationContext(),
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean notificationBoolean = sharedPreferences.getBoolean("notifications", true);
        if (notificationBoolean) {
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
        signOutButton = view.findViewById(R.id.clearCache);
        logTextView = view.findViewById(R.id.txt_log);
        progressBar = view.findViewById(R.id.progress_bar);
        scaleUp = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        openTicketsCountTextView = view.findViewById(R.id.open_tickets_count);
        ongoingTicketsCountTextView = view.findViewById(R.id.ongoing_tickets_count);



        //Sign in user
        signInButton.setOnClickListener(v -> {
            if (mSingleAccountApp == null) {
                return;
            }
            mSingleAccountApp.signIn(requireActivity(), null, SCOPES, getAuthInteractiveCallback());
        });

        //Sign out user
        signOutButton.setOnClickListener(v -> {
            scaleAnimations(signOutButton);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getResources().getString(R.string.sign_out_text));
            builder.setTitle(getResources().getString(R.string.sign_out_title));
            builder.setCancelable(false);
            builder.setNegativeButton(getResources().getString(R.string.sign_out_cancel), (dialog, which) -> dialog.cancel());
            builder.setPositiveButton(getResources().getString(R.string.sign_out_positive), (dialog, which) -> {
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
            });
            AlertDialog alert = builder.create();
            alert.show();
        });

        //Silent
        callGraphApiSilentButton.setOnClickListener(v -> {
            scaleAnimations(callGraphApiSilentButton);
            if (mSingleAccountApp == null){
                return;
            }
            requireActivity().runOnUiThread(() -> logTextView.setText(R.string.synchronizing_tickets));
            progressBar.setVisibility(View.VISIBLE);
            mSingleAccountApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, getAuthSilentCallback("get", ""));
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

        try {
            JSONArray postData = new JSONArray(json);

            myQueue.add(VolleyUtils.jsonArrayPostRequest(Config.ADDTICKETSURL, postData, (JSONArray res) -> {
                if (res != null) {
                    Log.d("URLResponse", res.toString());
                    // Calling the database to update the tickets ArrayList
                    ticketGetRequest();
                }
            }, (VolleyError error) -> Log.d("URLAddTicketRequest", "Unable to receive JSON response from server. Error: " + error.toString())));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void ticketGetRequest(){
        try {
            Log.d("ticketGetRequest", "Loading tickets from DB");
            myQueue.add(VolleyUtils.jsonArrayGetRequest(Config.GETTICKETSURL, (JSONArray res) -> {
                if (res != null) {
                    Log.d("ticketGetResponse", res.toString());
                    jsonArrayToArrayList(res);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void ticketDeleteRequest(String subject, String solution){

        JSONObject subjectJson = new JSONObject();
        try {
            subjectJson.put("subject", subject);
            subjectJson.put("solution", solution);

            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.DELETETICKETURL, subjectJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("URLDeleteResponse", res.toString());
                }
            }, (VolleyError error) -> Log.d("URLDeleteRequest", "Unable to receive JSON response from server. Error: " + error.toString())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void ticketStatusRequest(String subject){

        JSONObject subjectJson = new JSONObject();
        try {
            subjectJson.put("subject", subject);

            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.UPDATETICKETURL, subjectJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("URLUpdateResponse", res.toString());
                }
            }, (VolleyError error) -> Log.d("URLUpdateRequest", "Unable to receive JSON response from server. Error: " + error.toString())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void nonTicketRequest(String subject){

        JSONObject subjectJson = new JSONObject();
        try {
            subjectJson.put("subject", subject);

            myQueue.add(VolleyUtils.jsonObjectPostRequest(Config.DELETENONTICKETURL, subjectJson, (JSONObject res) -> {
                if (res != null) {
                    Log.d("URLNonTicketResponse", res.toString());
                }
            }, (VolleyError error) -> Log.d("URLNonTicketRequest", "Unable to receive JSON response from server. Error: " + error.toString())));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openTicketsCountGetRequest(){
        try {
            Log.d("openTicketsCountGetRequest", "Loading counter from DB");
            myQueue.add(VolleyUtils.jsonObjectGetRequest(Config.GETOPENTICKETSCOUNTURL, (JSONObject res) -> {
                if (res != null) {
                    Log.d("openTicketsCounterGetResponse", res.toString());
                    try {
                        openTicketsCount = res.get("count").toString();
                        openTicketsCountTextView.setText(openTicketsCount);
                    } catch (JSONException e){
                        e.printStackTrace();
                        openTicketsCountTextView.setText("N/A");
                        Log.d("openTicketCounterGetResponse", "Could not get monthly count from server");
                    }
                }
            }, (VolleyError error) -> {
                Log.d("openTicketCounterGetResponse", "Unable to receive response from server. Error: " + error.toString());
                openTicketsCountTextView.setText("N/A");
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ongoingTicketsCountGetRequest(){
        try {
            Log.d("URLGetRequest", "Loading counter from DB");

            myQueue.add(VolleyUtils.jsonObjectGetRequest(Config.GETONGOINGTICKETSCOUNTURL, (JSONObject res) -> {
                if (res != null) {
                    Log.d("ongoingTicketsCounterGetResponse", res.toString());
                    try {
                        ongoingTicketsCount = res.get("count").toString();
                        ongoingTicketsCountTextView.setText(ongoingTicketsCount);
                    } catch (JSONException e){
                        e.printStackTrace();
                        ongoingTicketsCountTextView.setText("N/A");
                        Log.d("ongoingTicketsCounterGetResponse", "Could not get monthly count from server");
                    }
                }
            }, (VolleyError error) -> {
                Log.d("CounterGetRequest", "Unable to receive response from server. Error: " + error.toString());
                ongoingTicketsCountTextView.setText("N/A");
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callGraphAPI(IAuthenticationResult authenticationResult, String caseString, String graph_id) {

        final String accessToken = authenticationResult.getAccessToken();

        IGraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(request -> {
                            Log.d(TAG, "Authenticating request," + request.getRequestUrl());
                            request.addHeader("Authorization", "Bearer " + accessToken);
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
                        .top(maxTickets)
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
            callGraphApiSilentButton.setEnabled(true);
            callGraphApiSilentButton.setVisibility(View.VISIBLE);
        } else {
            signInButton.setEnabled(true);
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setEnabled(false);
            signOutButton.setVisibility(View.INVISIBLE);
            callGraphApiSilentButton.setEnabled(false);
            callGraphApiSilentButton.setVisibility(View.INVISIBLE);
            logTextView.setText(R.string.outlook_disconnected);
        }
    }

    private void displayError(@NonNull final Exception exception) {

        requireActivity().runOnUiThread(() -> logTextView.setText(exception.toString()));
    }

    private void removeProgressBar(){

        requireActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }

    private void displayGraphResult(@NonNull final JsonObject graphResponse) {

        JsonArray myJsonArray = graphResponse.getAsJsonArray("value");

        // If no tickets received from server, display no tickets message
        if (myJsonArray.toString().equals("[]")) {
            Log.d("NoTicketsJSON", myJsonArray.toString());
            removeProgressBar();
            logTextView.setText(R.string.no_tech_support_tickets);
            return;
        }

        // Clearing both ArrayLists before adding new tickets (avoid duplicate tickets)
        ticketArrayList.clear();
        graphDataArrayList.clear();
        // Loop through myJsonArray get the subject, body, and from address
        for(JsonElement message: myJsonArray){
            if (message != null) {

                // Add the subject
                String subject = message.getAsJsonObject().get("subject").toString();
                subject = removeQuotations(subject);

                // Ignoring replies to an email
                if (subject.contains("Re:")){
                    continue;
                }

                // Add the graphId
                String graphId = message.getAsJsonObject().get("id").toString();

                // Add the body
                String body = message.getAsJsonObject().get("body").getAsJsonObject().get("content").toString();
                //TODO: the below is better but still has too many newlines. See what I can do about that
                String body2 = HtmlCompat.fromHtml(body, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim();
                //body2 = body2.replaceAll("\n\n\n")
                body = removeHtmlTags(body);
                body = removeQuotations(body);


                Log.d("HTML", body);
                Log.d("HTML2", body2);

                // Add sender address
                String from = message.getAsJsonObject().get("from").getAsJsonObject().get("emailAddress").getAsJsonObject().get("address").toString();
                from = removeQuotations(from);

                TroubleTicket troubleTicket = new TroubleTicket(subject, body, from, "Open", graphId, "");

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

        requireActivity().runOnUiThread(() -> logTextView.setText(R.string.fetched_data));
    }

    private void performOperationOnSignOut() {
        final String signOutText = "Signed Out.";
        Toast.makeText(getContext(), signOutText, Toast.LENGTH_SHORT)
                .show();
    }

    private String removeHtmlTags(String body){
        // Remove all html tags using regex matching pattern
        body = body.replaceAll("<.*?>", "");

        // Using plain text matching to replace new line characters
        body = body.replace("\\r", "");
        body = body.replace("\\n", " ");

        return body;
    }

    private String removeQuotations(String text){

        text = text.replaceAll("^\"|\"$", "");
        return text;
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