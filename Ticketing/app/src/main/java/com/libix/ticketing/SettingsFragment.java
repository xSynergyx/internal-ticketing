package com.libix.ticketing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.microsoft.graph.models.extensions.Shared;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        if (key.equals("notifications") && sharedPreferences.getBoolean("notifications",true)){
            FirebaseMessaging.getInstance().subscribeToTopic("all");
            Toast.makeText(getContext(), "Subscribed to notifications", Toast.LENGTH_SHORT).show();
        } else if (key.equals("notifications")){
            Toast.makeText(getContext(), "Unsubscribed from notifications", Toast.LENGTH_SHORT).show();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
        }
    }
}