package com.tourmate.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.tourmate.bd.R;
import com.tourmate.bd.databinding.ActivityLoginBinding;
import com.tourmate.bd.models.UserProfile;
import com.tourmate.bd.network.FirebaseAuthManager;
import com.tourmate.bd.utils.ThemeHelper;

/**
 * LoginActivity for Firebase Email/Password & Google Sign-In authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupGoogleSignIn();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        // Request ID token for Firebase Authentication
        String webClientId = getString(R.string.default_web_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        setLoading(false);
                        Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode() + " (Check SHA-1 in Firebase Console)", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            setLoading(true);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.tvSkipGuest.setOnClickListener(v -> finish());
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("ইমেইল লিখুন");
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

        setLoading(true);
        FirebaseAuthManager.getInstance().loginWithEmail(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user, UserProfile profile) {
                setLoading(false);
                String name = profile != null && profile.getName() != null ? profile.getName() : user.getEmail();
                Toast.makeText(LoginActivity.this, "স্বাগতম " + name + "!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "লগইন ব্যর্থ: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        FirebaseAuthManager.getInstance().firebaseAuthWithGoogle(idToken, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user, UserProfile profile) {
                setLoading(false);
                String name = profile != null ? profile.getName() : "Traveler";
                Toast.makeText(LoginActivity.this, "Google দিয়ে সফলভাবে লগইন হয়েছে! স্বাগতম " + name, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Google প্রমাণীকরণ ব্যর্থ: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnGoogleSignIn.setEnabled(!isLoading);
    }
}

