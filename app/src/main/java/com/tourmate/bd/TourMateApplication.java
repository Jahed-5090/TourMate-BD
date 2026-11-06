package com.tourmate.bd;

import android.app.Application;

import com.tourmate.bd.utils.ThemeHelper;

/**
 * Custom Application class for TourMate BD.
 * Initializes global application settings, including Theme (Dark/Light mode).
 */
public class TourMateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply saved Dark/Light theme mode on app launch
        ThemeHelper.applySavedTheme(this);
    }
}

