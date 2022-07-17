package com.libix.ticketing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;

import com.microsoft.graph.authentication.IAuthenticationProvider; //Imports the Graph sdk Auth interface
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IMessageCollectionPage;
import com.microsoft.identity.client.AuthenticationCallback; // Imports MSAL auth methods
import com.microsoft.identity.client.*;
import com.microsoft.identity.client.exception.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * NOTE: Emails received are from all folders (inbox, sent, deleted, etc)
 *
 **/

//TODO: In notes activity, clear out the EditText views after submitting a ticket
//TODO: Change font style of title. Maybe try to center it too (or leave that for v2 refinement).
//TODO: Add shadow to action bar
//TODO: Add progress bar when syncing tickets
//TODO: Animate buttons
//TODO: Re-add options menu (check some nice styles). add notification on/off functionality there
//TODO: Add shadow to ticket close buttons


public class MainActivity extends AppCompatActivity implements OnTicketCloseClick {

    private final static String[] SCOPES = {"Mail.ReadWrite"};
    /* Azure AD v2 Configs */
    final static String AUTHORITY = "https://login.microsoftonline.com/common";
    private ISingleAccountPublicClientApplication mSingleAccountApp;

    private static final String TAG = MainActivity.class.getSimpleName();

    /* UI & Debugging Variables */
    Button signInButton;
    Button signOutButton;
    Button callGraphApiInteractiveButton;
    Button callGraphApiSilentButton;
    TextView logTextView;
    TextView currentUserTextView;
    RecyclerView ticketsRecyclerView;
    TicketAdapter myTicketAdapter;

    ArrayList<TroubleTicket> ticketArrayList = new ArrayList<TroubleTicket>(); // This one is used to load tickets from the server database
    ArrayList<TroubleTicket> graphDataArrayList = new ArrayList<>(); // This one is used to update the database with the information from the GraphAPI call

    /**
     * Receive subject from the OnTicketCloseClick interface and pass it on to the
     * ticketDeleteRequest method
     *
     * @param subject The subject of the ticket/email to be deleted
     */
    @Override
    public void onTicketCloseClick(String subject, String graph_id, String solution){
        Log.d("Close", "Subject now in main activity");
        Log.d("Close", "Subject in MainActivity: " + subject);

        Log.d("Notifications", "About to send notification");
        String title = "Ticket: \"" + subject + "\" has been closed";
        PushNotificationSender notificationSender = new PushNotificationSender("/topics/all",
                title,
                solution,
                getApplicationContext(),
                MainActivity.this
        );
        notificationSender.sendNotifications();

        ticketDeleteRequest(subject, solution); // Delete from database
        //Log.d("CloseGraphID", "graph_id: " + graph_id);

        mSingleAccountApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, getAuthSilentCallback("delete", graph_id)); // Delete email from graphAPI
    }

    public void onTicketStatusClick(String subject){

        Toast.makeText(this, "Status updated for ticket: " + subject, Toast.LENGTH_LONG).show();
        ticketStatusRequest(subject);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initializeUI();

        PublicClientApplication.createSingleAccountPublicClientApplication(getApplicationContext(),
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

        //Action bar setup
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Libix");
        centerTitle();
        //Display logo
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setElevation(4);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        /*
         * Setting up OnClickListeners
         */

        // OnClickListener for SearchActivity
        final TextView search = (TextView) findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });

        // OnClickListener for CounterActivity
        final TextView counter = (TextView) findViewById(R.id.counter);
        counter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent counterIntent = new Intent(MainActivity.this, CounterActivity.class);
                startActivity(counterIntent);
            }
        });

        // OnClickListener for NotesActivity
        final TextView notes = (TextView) findViewById(R.id.notes);
        notes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent notesIntent = new Intent(MainActivity.this, NotesActivity.class);
                startActivity(notesIntent);
            }
        });

        ticketsRecyclerView = findViewById(R.id.ticket_recycler_view);

        myTicketAdapter = new TicketAdapter(this, ticketArrayList, this);
        ticketsRecyclerView.setAdapter(myTicketAdapter);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseMessaging.getInstance().subscribeToTopic("all");
    } // End of onCreate method

    /*
    // Displays the action bar created in main.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // Specifies what each item in the action bar does when clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.notifications:
                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.account:
                Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.info:
                Toast.makeText(this, "Info clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.update:
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.logout:
                Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
     */

    // Method for centering title without creating a custom action bar. Thanks THEPATEL on stackoverflow
    private void centerTitle() {
        ArrayList<View> textViews = new ArrayList<>();

        getWindow().getDecorView().findViewsWithText(textViews, getTitle(), View.FIND_VIEWS_WITH_TEXT);

        if(textViews.size() > 0) {
            AppCompatTextView appCompatTextView = null;
            if(textViews.size() == 1) {
                appCompatTextView = (AppCompatTextView) textViews.get(0);
            } else {
                for(View v : textViews) {
                    if(v.getParent() instanceof Toolbar) {
                        appCompatTextView = (AppCompatTextView) v;
                        break;
                    }
                }
            }

            if(appCompatTextView != null) {
                ViewGroup.LayoutParams params = appCompatTextView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                appCompatTextView.setLayoutParams(params);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    appCompatTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
            }
        }
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

    private void initializeUI(){
        signInButton = findViewById(R.id.signIn);
        callGraphApiSilentButton = findViewById(R.id.callGraphSilent);
        //callGraphApiInteractiveButton = findViewById(R.id.callGraphInteractive);
        signOutButton = findViewById(R.id.clearCache);
        logTextView = findViewById(R.id.txt_log);
        //currentUserTextView = findViewById(R.id.current_user);

        //Sign in user
        signInButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (mSingleAccountApp == null) {
                    return;
                }
                mSingleAccountApp.signIn(MainActivity.this, null, SCOPES, getAuthInteractiveCallback());
            }
        });

        //Sign out user
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSingleAccountApp == null){
                    return;
                }
                mSingleAccountApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
                    @Override
                    public void onSignOut() {
                        updateUI(null);
                        performOperationOnSignOut();
                    }
                    @Override
                    public void onError(@NonNull MsalException exception){
                        displayError(exception);
                    }
                });
            }
        });

        /*
        //Interactive
        callGraphApiInteractiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSingleAccountApp == null) {
                    return;
                }
                mSingleAccountApp.acquireToken(MainActivity.this, SCOPES, getAuthInteractiveCallback());
            }
        });
         */

        //Silent
        callGraphApiSilentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSingleAccountApp == null){
                    return;
                }
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
        RequestQueue queue = Volley.newRequestQueue(this);
        
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
        RequestQueue queue = Volley.newRequestQueue(this);

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

    protected void ticketDeleteRequest(String subject, String solution){

        String url = Config.DELETETICKETURL;
        RequestQueue queue = Volley.newRequestQueue(this);
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
                            // TODO: Make a toast "sync failed" message
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
        RequestQueue queue = Volley.newRequestQueue(this);
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
                            // TODO: Make a toast "ticket update failed" message
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
                Toast.makeText(this, "Deleting Email with API", Toast.LENGTH_LONG).show();
                Log.d("DeleteAPI", "Graph_ID: " + graph_id);

                // TODO: Find out why API delete call fails. So far failure doesn't crash the app
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
                                Log.d("DeleteAPIError", "Well, there's an error.");
                                displayError(ex);
                            }
                        });
                break;
            default:
                Toast.makeText(this, "Graph API not called", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void updateUI(@Nullable final IAccount account) {
        if (account != null) {
            signInButton.setEnabled(false);
            signOutButton.setEnabled(true);
            //callGraphApiInteractiveButton.setEnabled(true);
            callGraphApiSilentButton.setEnabled(true);
            //currentUserTextView.setText(account.getUsername());
        } else {
            signInButton.setEnabled(true);
            signOutButton.setEnabled(false);
            //callGraphApiInteractiveButton.setEnabled(false);
            callGraphApiSilentButton.setEnabled(false);
            //currentUserTextView.setText("");
            logTextView.setText("");
        }
    }

    private void displayError(@NonNull final Exception exception) {

        runOnUiThread(new Runnable(){

            @Override
            public void run(){
                logTextView.setText(exception.toString());
            }
        });
        //logTextView.setText(exception.toString());
    }

    private void displayGraphResult(@NonNull final JsonObject graphResponse) {

        final ArrayList<String> textToDisplay = new ArrayList();
        JsonArray myJsonArray = graphResponse.getAsJsonArray("value");

        // Clearing the both Array Lists before putting in new tickets (avoid duplicate tickets)
        ticketArrayList.clear();
        graphDataArrayList.clear();
        // Loop through myJsonArray get the subject, body, and from address
        for(JsonElement message: myJsonArray){
            if ( message instanceof JsonElement ) {

                // Temporary
                String subject = message.getAsJsonObject().get("subject").toString();
                subject = removeQuotations(subject);


                // TODO: Create a "recently deleted" table. So I can restore the ticket if needed.
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

        //logTextView.setText(graphResponse.toString());

        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                logTextView.setText(textToDisplay.toString());
            }
        });
        //logTextView.setText(textToDisplay.toString());
    }

    private void performOperationOnSignOut() {
        final String signOutText = "Signed Out.";
        //currentUserTextView.setText("");
        Toast.makeText(getApplicationContext(), signOutText, Toast.LENGTH_SHORT)
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
        }
    }
}