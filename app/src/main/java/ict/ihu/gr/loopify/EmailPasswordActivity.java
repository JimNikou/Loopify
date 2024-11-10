//package ict.ihu.gr.loopify;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.google.firebase.auth.UserProfileChangeRequest;
//
//public class EmailPasswordActivity extends Activity {
//
//    private static final String TAG = "EmailPassword";
//    private static final int RC_SIGN_IN = 9001;  // Request code for Google Sign-In
//
//    private FirebaseAuth mAuth;
//    private GoogleSignInClient mGoogleSignInClient;
//    private EditText emailField;
//    private EditText passwordField;
//    private EditText displayNameField;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        // Initialize UI elements
//        emailField = findViewById(R.id.emailField);
//        passwordField = findViewById(R.id.passwordField);
//        displayNameField = findViewById(R.id.displayNameField);
//        Button loginButton = findViewById(R.id.loginButton);
//        Button signupButton = findViewById(R.id.signupButton);
//        findViewById(R.id.googleSignInButton).setOnClickListener(v -> signInWithGoogle());
//
//        // Configure Google Sign-In options
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this is set up in your project
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        // Login button listener
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String email = emailField.getText().toString();
//                String password = passwordField.getText().toString();
//                signIn(email, password);
//            }
//        });
//
//        // Sign-up button listener
//        signupButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String email = emailField.getText().toString();
//                String password = passwordField.getText().toString();
//                String displayName = displayNameField.getText().toString();
//                createAccount(email, password, displayName);
//            }
//        });
//    }
//
//    private void createAccount(String email, String password, final String displayName) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "createUserWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            if (user != null) {
//                                updateDisplayName(user, displayName);
//                            }
//                        } else {
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
//
//    private void updateDisplayName(FirebaseUser user, String displayName) {
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(displayName)
//                .build();
//
//        user.updateProfile(profileUpdates)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "User profile updated.");
//                        updateUI(user);
//                    } else {
//                        Log.w(TAG, "User profile update failed.");
//                    }
//                });
//    }
//
//    private void signIn(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "signInWithEmail:success");
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        updateUI(user);
//                    } else {
//                        Log.w(TAG, "signInWithEmail:failure", task.getException());
//                        Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void signInWithGoogle() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                Log.w(TAG, "Google sign in failed", e);
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "signInWithCredential:success");
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        updateUI(user);
//                    } else {
//                        Log.w(TAG, "signInWithCredential:failure", task.getException());
//                        Toast.makeText(EmailPasswordActivity.this, "Google Authentication failed.",
//                                Toast.LENGTH_SHORT).show();
//                        updateUI(null);
//                    }
//                });
//    }
//
//    private void updateUI(FirebaseUser user) {
//        if (user != null) {
//            Intent intent = new Intent(EmailPasswordActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }
//}
