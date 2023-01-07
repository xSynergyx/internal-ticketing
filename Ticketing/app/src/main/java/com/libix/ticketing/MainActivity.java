package com.libix.ticketing;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FragmentManager fm = getSupportFragmentManager();
    BottomNavigationView bottomNavigationView;
    int backStackCount;
    final int MY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        launchFirebaseUI();

        changeFragment(new MainFragment(), "MainFragment");
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Action bar setup
        ActionBar actionBar = getSupportActionBar();

        //Display logo
        if (actionBar != null) {
            actionBar.setIcon(R.mipmap.ic_launcher);
            actionBar.setTitle("");
            actionBar.setElevation(4);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Log.d("Bottom Navigation", Integer.toString(id));

            switch (id){
                case R.id.main_fragment_bottom_nav:
                    changeFragment(new MainFragment(), "MainFragment");
                    break;
                case R.id.closed_tickets_fragment_bottom_nav:
                    changeFragment(new ClosedTicketsFragment(), "ClosedTicketsFragment");
                    break;
                case R.id.counter_fragment_bottom_nav:
                    changeFragment(new CounterFragment(), "CounterFragment");
                    break;
                case R.id.new_ticket_fragment_bottom_nav:
                    changeFragment(new NewTicketFragment(), "NewTicketFragment");
                    break;
                case R.id.profile_fragment_bottom_nav:
                    changeFragment(new ProfileFragment(), "ProfileFragment");
            }
            backStackCount++;
            return true;
        });

        // Update selected item in bottom nav bar on back button press
        backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        fm.addOnBackStackChangedListener(() -> {
            // Check if back stack count decreased first
            if( fm.getBackStackEntryCount() <= backStackCount){
                backStackCount--;
                Fragment currentFrag =  fm.findFragmentById(R.id.frame_layout);

                assert currentFrag != null;
                if (currentFrag instanceof MainFragment){ bottomNavigationView.getMenu().getItem(0).setChecked(true); }
                else if (currentFrag instanceof ClosedTicketsFragment){ bottomNavigationView.getMenu().getItem(1).setChecked(true); }
                else if (currentFrag instanceof CounterFragment) { bottomNavigationView.getMenu().getItem(2).setChecked(true); }
                else if (currentFrag instanceof NewTicketFragment) { bottomNavigationView.getMenu().getItem(3).setChecked(true); }

                Log.d("CurrentFragment", currentFrag.toString());
            }
        });

        checkUpdate();
    } // End of onCreate method

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.d("Update", "Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails, request to start the update again.
                checkUpdate();
            }
        }
    }


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
                changeFragment(new SettingsFragment(), "SettingsFragment");
                break;
            case R.id.logout:
                signOutFirebaseUser();
                recreate();
                break;
            case R.id.statistics:
                changeFragment(new StatisticsFragment(), "StatisticsFragment");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void changeFragment(Fragment fragment, String fragTag){

        // Check to see if fragment is already created. If so replace the "new" fragment with the already-created one
        if (fm.findFragmentByTag(fragTag) != null){
            fragment = fm.findFragmentByTag(fragTag);
        }

        FragmentTransaction ft = fm.beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        ft.replace(R.id.frame_layout, Objects.requireNonNull(fragment), fragTag);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //String email = user.getEmail();
        } else {
            Log.d("FirebaseUI", Objects.requireNonNull(response).toString());
            Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show();
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
        }
    }

    private void signOutFirebaseUser(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show());
    }

    private void launchFirebaseUI(){

        final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                this::onSignInResult
        );

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.MicrosoftBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.mipmap.ic_launcher)
                .setTheme(R.style.AppTheme)
                .build();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Check the play store for new app version
     */
    private void checkUpdate(){

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                // Request the update
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            // Include a request code to later monitor this update request
                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}