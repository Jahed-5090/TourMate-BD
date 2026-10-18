package com.tourmate.bd.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.tourmate.bd.databinding.ActivitySplashBinding;

/**
 * Splash screen shown on app launch.
 * Features animated logo and tagline reveal.
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DURATION = 2800; // ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initial state — everything hidden
        binding.logoContainer.setAlpha(0f);
        binding.logoContainer.setTranslationY(60f);
        binding.tagline.setAlpha(0f);
        binding.tagline.setTranslationY(40f);
        binding.bottomBar.setAlpha(0f);

        // Animate logo in
        binding.logoContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate tagline in
        binding.tagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate bottom bar
        binding.bottomBar.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(1000)
                .start();

        // Navigate to MainActivity after splash
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}
