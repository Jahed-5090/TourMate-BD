package com.tourmate.bd.network;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tourmate.bd.database.TourMateDatabaseHelper;
import com.tourmate.bd.models.UserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Firebase Authentication (Email/Password & Google Sign-In) and
 * synchronizes user account info into Firebase Realtime Database (/users/{uid}).
 */
public class FirebaseAuthManager {

    private static final String RTDB_URL = "https://tourmate-bd-1d966-default-rtdb.firebaseio.com";

    private static FirebaseAuthManager instance;
    private final FirebaseAuth auth;
    private final FirebaseDatabase database;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user, UserProfile profile);
        void onFailure(String errorMessage);
    }

    public interface UserProfileCallback {
        void onProfileLoaded(UserProfile profile);
        void onError(String errorMessage);
    }

    private FirebaseAuthManager() {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase dbInstance;
        try {
            dbInstance = FirebaseDatabase.getInstance(RTDB_URL);
        } catch (Exception e) {
            dbInstance = FirebaseDatabase.getInstance();
        }
        database = dbInstance;
    }

    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    /**
     * Signs up with Email & Password, sends verification email, and stores user details in Firebase.
     */
    public void registerWithEmail(String name, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        // Update profile display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        firebaseUser.updateProfile(profileUpdates);

                        // Send Email Verification Link
                        firebaseUser.sendEmailVerification();

                        // Save to Realtime Database /users/{uid}
                        saveUserProfileToDatabase(firebaseUser, name, "password", null);

                        // Sign out so user must verify email before logging in
                        auth.signOut();

                        UserProfile profile = new UserProfile(
                                firebaseUser.getUid(),
                                name,
                                email,
                                null,
                                "password",
                                System.currentTimeMillis(),
                                System.currentTimeMillis()
                        );

                        if (callback != null) {
                            callback.onSuccess(firebaseUser, profile);
                        }
                    } else {
                        String error = parseAuthError(task.getException());
                        if (callback != null) {
                            callback.onFailure(error);
                        }
                    }
                });
    }

    /**
     * Logs in using Email & Password after checking email verification status.
     */
    public void loginWithEmail(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        // Enforce Email Verification check
                        if (!firebaseUser.isEmailVerified()) {
                            firebaseUser.sendEmailVerification();
                            auth.signOut();
                            if (callback != null) {
                                callback.onFailure("Your email address is not verified. A verification link has been sent to your email inbox/spam folder.");
                            }
                            return;
                        }

                        updateLastLogin(firebaseUser.getUid());

                        UserProfile basicProfile = new UserProfile(
                                firebaseUser.getUid(),
                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Traveler",
                                firebaseUser.getEmail(),
                                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                                "password",
                                System.currentTimeMillis(),
                                System.currentTimeMillis()
                        );

                        if (callback != null) {
                            callback.onSuccess(firebaseUser, basicProfile);
                        }

                        fetchUserProfile(firebaseUser.getUid(), new UserProfileCallback() {
                            @Override
                            public void onProfileLoaded(UserProfile profile) {}

                            @Override
                            public void onError(String errorMessage) {}
                        });
                    } else {
                        String error = parseAuthError(task.getException());
                        if (callback != null) {
                            callback.onFailure(error);
                        }
                    }
                });
    }

    /**
     * Authenticates with Google ID Token and saves profile into Realtime Database.
     */
    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        FirebaseUser user = auth.getCurrentUser();
                        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Google User";
                        saveUserProfileToDatabase(user, displayName, "google.com", callback);
                    } else {
                        String error = parseAuthError(task.getException());
                        if (callback != null) {
                            callback.onFailure(error);
                        }
                    }
                });
    }

    private String parseAuthError(Exception e) {
        if (e == null || e.getMessage() == null) {
            return "Authentication failed. Please try again.";
        }
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("credential") || msg.contains("password") || msg.contains("invalid_login_credentials") || msg.contains("invalid-credential")) {
            return "Incorrect email or password. (If signed up via Google, click 'Sign in with Google')";
        } else if (msg.contains("no user record") || msg.contains("user-not-found")) {
            return "No account found with this email address. Please sign up.";
        } else if (msg.contains("already in use") || msg.contains("email-already-in-use")) {
            return "An account already exists with this email address.";
        } else if (msg.contains("network") || msg.contains("connection")) {
            return "Please check your internet connection and try again.";
        }
        return e.getMessage();
    }

    /**
     * Saves or merges the user account information into Firebase Realtime Database at /users/{uid}.
     */
    private void saveUserProfileToDatabase(FirebaseUser user, String name, String provider, AuthCallback callback) {
        long now = System.currentTimeMillis();
        Uri photoUri = user.getPhotoUrl();
        String photoUrl = photoUri != null ? photoUri.toString() : null;

        UserProfile profile = new UserProfile(
                user.getUid(),
                name,
                user.getEmail(),
                photoUrl,
                provider,
                now,
                now
        );

        // Instantly notify UI callback of success
        if (callback != null) {
            callback.onSuccess(user, profile);
        }

        // Save to Realtime Database asynchronously without blocking UI callback
        try {
            DatabaseReference userRef = database.getReference("users").child(user.getUid());
            userRef.setValue(profile);
        } catch (Exception ignored) {}
    }

    /**
     * Updates the last login timestamp in Realtime Database.
     */
    private void updateLastLogin(String uid) {
        try {
            DatabaseReference userRef = database.getReference("users").child(uid);
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastLoginAt", System.currentTimeMillis());
            userRef.updateChildren(updates);
        } catch (Exception ignored) {}
    }

    /**
     * Fetches the user profile from Realtime Database.
     */
    public void fetchUserProfile(String uid, UserProfileCallback callback) {
        database.getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserProfile profile = snapshot.getValue(UserProfile.class);
                        if (profile != null) {
                            callback.onProfileLoaded(profile);
                        } else {
                            callback.onError("User profile not found in cloud database.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * Updates the user's display name in Firebase Auth and Realtime Database.
     */
    public void updateProfileName(String newName, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure("User not logged in.");
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    DatabaseReference userRef = database.getReference("users").child(user.getUid());
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", newName);
                    userRef.updateChildren(updates);
                } catch (Exception ignored) {}

                if (callback != null) {
                    UserProfile profile = new UserProfile(user.getUid(), newName, user.getEmail(), null, "password", System.currentTimeMillis(), System.currentTimeMillis());
                    callback.onSuccess(user, profile);
                }
            } else {
                String err = parseAuthError(task.getException());
                if (callback != null) callback.onFailure(err);
            }
        });
    }

    /**
     * Changes user's password after re-authenticating with current password.
     */
    public void updateUserPassword(String currentPassword, String newPassword, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            if (callback != null) callback.onFailure("User not logged in.");
            return;
        }

        AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        if (callback != null) callback.onSuccess(user, null);
                    } else {
                        String err = parseAuthError(updateTask.getException());
                        if (callback != null) callback.onFailure(err);
                    }
                });
            } else {
                if (callback != null) callback.onFailure("বর্তমান পাসওয়ার্ড সঠিক নয়।");
            }
        });
    }

    /**
     * Sends verification link to new email address before updating email.
     */
    public void updateUserEmail(String newEmail, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure("User not logged in.");
            return;
        }

        user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) callback.onSuccess(user, null);
            } else {
                String err = parseAuthError(task.getException());
                if (callback != null) callback.onFailure(err);
            }
        });
    }

    /**
     * Instantly adds a favorite spot to Firebase Realtime Database for current logged in user.
     */
    public void addFavoriteToCloud(String spotId) {
        if (!isLoggedIn() || spotId == null) return;
        try {
            String uid = getCurrentUserId();
            DatabaseReference favRef = database.getReference("users").child(uid).child("favorites").child(spotId);
            favRef.setValue(true);
        } catch (Exception ignored) {}
    }

    /**
     * Instantly removes a favorite spot from Firebase Realtime Database for current logged in user.
     */
    public void removeFavoriteFromCloud(String spotId) {
        if (!isLoggedIn() || spotId == null) return;
        try {
            String uid = getCurrentUserId();
            DatabaseReference favRef = database.getReference("users").child(uid).child("favorites").child(spotId);
            favRef.removeValue();
        } catch (Exception ignored) {}
    }

    /**
     * Synchronizes local favorites into /users/{uid}/favorites in Firebase Realtime Database.
     * If local device has favorites, syncs current local state to cloud.
     * If local device has 0 favorites (fresh login), restores cloud favorites into local SQLite.
     */
    public void syncUserFavoritesToCloud(Context context, FirebaseManager.CloudSyncCallback callback) {
        if (!isLoggedIn()) {
            if (callback != null) callback.onSyncFailure("User is not signed in.");
            return;
        }

        String uid = getCurrentUserId();
        TourMateDatabaseHelper db = TourMateDatabaseHelper.getInstance(context);
        DatabaseReference favRef = database.getReference("users").child(uid).child("favorites");

        List<String> localFavSpotIds = db.getAllFavoriteSpotIds();

        if (localFavSpotIds.isEmpty()) {
            // Restore from cloud into local SQLite
            favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int restoredCount = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String spotId = child.getKey();
                        if (spotId != null) {
                            com.tourmate.bd.models.TouristSpot spot = com.tourmate.bd.data.DataRepository.getInstance().getSpotById(spotId);
                            if (spot != null) {
                                db.addFavorite(spot);
                                restoredCount++;
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onSyncSuccess("Restored " + restoredCount + " favorites from cloud!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (callback != null) callback.onSyncFailure("Failed to read cloud data: " + error.getMessage());
                }
            });
        } else {
            // Push exact current local SQLite favorites state to Cloud
            Map<String, Object> favMap = new HashMap<>();
            for (String spotId : localFavSpotIds) {
                favMap.put(spotId, true);
            }
            favRef.setValue(favMap)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) {
                            callback.onSyncSuccess("Sync successful! " + localFavSpotIds.size() + " favorites saved to cloud.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onSyncFailure("Failed to save to cloud: " + e.getMessage());
                    });
        }
    }

    /**
     * Signs out from Firebase and Google client.
     */
    public void signOut(Context context) {
        auth.signOut();
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
            googleSignInClient.signOut();
        } catch (Exception ignored) {}
    }
}

