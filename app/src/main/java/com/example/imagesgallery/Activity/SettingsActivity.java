package com.example.imagesgallery.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.imagesgallery.R;

public class SettingsActivity extends PreferenceActivity {

    private SwitchPreference switchPreferenceDarkMode;
    private LinearLayout preferenceScreenLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        switchPreferenceDarkMode = (SwitchPreference) findPreference("darkMode");
        preferenceScreenLayout = (LinearLayout) findViewById(android.R.id.list).getParent().getParent();

        // Retrieve the saved dark mode state
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("nightMode", false);

        // Set the default value only if the preference is not already set
        if (!sharedPreferences.contains("nightMode")) {
            switchPreferenceDarkMode.setChecked(true);  // Set the default value to true
        } else {
            switchPreferenceDarkMode.setChecked(isDarkModeEnabled);
        }

        switchPreferenceDarkMode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Handle the switch preference change
                boolean isChecked = switchPreferenceDarkMode.isChecked();
                setDarkMode(isChecked);
                return true;
            }
        });

        // Apply the initial dark mode state
        setDarkMode(isDarkModeEnabled);
    }

    public void setDarkMode(boolean enabled) {
        int mode = enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);

        // Save the dark mode state in SharedPreferences
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putBoolean("nightMode", enabled);
        editor.apply();

        // Update the background color or theme based on the dark mode state
        updateBackground(enabled);
    }

    private void updateBackground(boolean isDarkModeEnabled) {
        int backgroundColor = isDarkModeEnabled ? android.R.color.background_dark : android.R.color.white;
        preferenceScreenLayout.setBackgroundColor(getResources().getColor(backgroundColor));
    }
}
