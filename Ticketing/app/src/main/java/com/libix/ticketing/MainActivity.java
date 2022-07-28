package com.libix.ticketing;

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

        changeFragment(new MainFragment());

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

        //TODO: Did I never set up a listener for main activity? If not make that button and then make it switch to the main fragment
        //TODO: Load up the main fragment on creating the main activity
        //TODO: See if it works with the methods still in MainActivity.java instead of MainFragment.java -----> If not, start the painful switch

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

    private void changeFragment(Fragment fragment){

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_layout, fragment);
        ft.commit();
    }
}