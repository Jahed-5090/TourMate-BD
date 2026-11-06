package com.tourmate.bd.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.tourmate.bd.databinding.ActivityRegisterBinding;
import com.tourmate.bd.models.UserProfile;
import com.tourmate.bd.network.FirebaseAuthManager;
import com.tourmate.bd.utils.ThemeHelper;

/**
 * RegisterActivity for creating a new user account with Email/Password
 * and storing the profile in Firebase Realtime Database.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("আপনার পুরো নাম লিখুন");
            return;
        }
        binding.tilName.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("ইমেইল অ্যাড্রেস লিখুন");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("সঠিক ইমেইল ফরম্যাট দিন");
            return;
        }
        binding.tilEmail.setError(null);

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("পাসওয়ার্ড লিখুন");
            return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে");
            return;
        }
        binding.tilPassword.setError(null);

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("পাসওয়ার্ড দুটি মিলছে না");
            return;
        }
        binding.tilConfirmPassword.setError(null);

        setLoading(true);
        FirebaseAuthManager.getInstance().registerWithEmail(name, email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user, UserProfile profile) {
                setLoading(false);
                new androidx.appcompat.app.AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("ইমেইল ভেরিফিকেশন পাঠানো হয়েছে 📩")
                        .setMessage("আপনার ইমেইলে (" + email + ") একটি ভেরিফিকেশন লিঙ্ক পাঠানো হয়েছে। অনুগ্রহ করে ইনবক্স/স্প্যাম ফোল্ডার থেকে লিঙ্কটি চেপে অ্যাকাউন্ট ভেরিফাই করার পর লগইন করুন।")
                        .setPositiveButton("ঠিক আছে (Log In)", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "নিবন্ধন ব্যর্থ: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
    }
}

