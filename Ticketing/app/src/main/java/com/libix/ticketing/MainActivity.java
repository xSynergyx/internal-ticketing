package com.libix.ticketing;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
//TODO: Change font style of title.
//TODO: Add progress bar when syncing tickets
//TODO: Animate buttons
//TODO: Re-add options menu (check some nice styles). add notification on/off functionality there
//TODO: Add shadow to ticket close buttons

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        launchFirebaseUI();

        changeFragment(new MainFragment(), "MainFragment");

        //Action bar setup
        ActionBar actionBar = getSupportActionBar();
        //centerTitle();

        //Display logo
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setTitle("");
        actionBar.setElevation(4);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        /*
         * Setting up OnClickListeners
         */

        // OnClickListener for OpenTicketsFragment
        final TextView open = (TextView) findViewById(R.id.open);
        open.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                changeFragment(new MainFragment(), "MainFragment");
            }
        });

        // OnClickListener for ClosedTicketsFragment
        final TextView search = (TextView) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                changeFragment(new ClosedTicketsFragment(), "ClosedTicketsFragment");
            }
        });

        // OnClickListener for CounterFragment
        final TextView counter = (TextView) findViewById(R.id.counter);
        counter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                changeFragment(new CounterFragment(), "CounterFragment");
            }
        });

        // OnClickListener for NewTicketFragment
        final TextView new_ticket = (TextView) findViewById(R.id.new_ticket);
        new_ticket.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                changeFragment(new NewTicketFragment(), "NewTicketFragment");
            }
        });

    } // End of onCreate method


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
            case R.id.settings:
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
                changeFragment(new SettingsFragment(), "SettingsFragment");
                break;
/*
            case R.id.account:
                Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.info:
                Toast.makeText(this, "Info clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.update:
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show();
                break;
 */

            case R.id.logout:
                Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
                signOutFirebaseUser();
                recreate();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void changeFragment(Fragment fragment, String fragTag){

        FragmentManager fm = getSupportFragmentManager();

        // Check to see if fragment is already created. If so replace the "new" fragment with the already-created one
        if (fm.findFragmentByTag(fragTag) != null){
            fragment = fm.findFragmentByTag(fragTag);
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_layout, fragment, fragTag);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
            // ...
        } else {
            Log.d("FIREBASEUI", response.toString());
            Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show();
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    private void signOutFirebaseUser(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchFirebaseUI(){

        final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                    @Override
                    public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                        onSignInResult(result);
                    }
                }
        );

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.mipmap.ic_launcher)
                .setTheme(R.style.AppTheme)
                .build();
        signInLauncher.launch(signInIntent);
    }
}