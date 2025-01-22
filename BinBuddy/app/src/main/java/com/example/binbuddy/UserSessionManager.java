package com.example.binbuddy;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_LOGGED_IN_USER = "loggedInUser";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constructor
    public UserSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save the logged-in user's username
    public void saveUserSession(String username) {
        editor.putString(KEY_LOGGED_IN_USER, username);
        editor.apply();
    }

    // Get the logged-in user's username
    public String getLoggedInUser() {
        return sharedPreferences.getString(KEY_LOGGED_IN_USER, null);
    }

    // Clear the user session (logout)
    public void clearUserSession() {
        editor.clear();
        editor.apply();
    }

    // Check if a user is logged in
    public boolean isLoggedIn() {
        return getLoggedInUser() != null;
    }
}
