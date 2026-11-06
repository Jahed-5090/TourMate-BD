package com.tourmate.bd.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility helper to manage, persist, and apply Dark and Light mode across the TourMate BD app.
 */
public class ThemeHelper {

    private static final String PREF_NAME = "tourmate_theme_pref";
    private static final String KEY_IS_DARK_MODE = "key_is_dark_mode";

    /**
     * Applies the saved theme preference globally.
     * Should be called on application launch or activity creation.
     */
    public static void applySavedTheme(Context context) {
        boolean isDarkMode = isDarkMode(context);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Checks if dark mode is currently selected.
     */
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_DARK_MODE, false);
    }

    /**
     * Sets and persists the dark mode preference, updating the night mode globally.
     */
    public static void setDarkMode(Context context, boolean darkMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, darkMode).apply();
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Toggles between Dark and Light mode, persisting the change.
     *
     * @return true if now Dark Mode, false if now Light Mode.
     */
    public static boolean toggleTheme(Context context) {
        boolean newDarkMode = !isDarkMode(context);
        setDarkMode(context, newDarkMode);
        return newDarkMode;
    }
}

