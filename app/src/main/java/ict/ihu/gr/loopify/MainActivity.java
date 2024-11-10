package ict.ihu.gr.loopify;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;
import ict.ihu.gr.loopify.databinding.ActivityMainBinding;
import ict.ihu.gr.loopify.ui.home.HomeFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

//TEST TEST TEST TEST TEST TEST TEST TEST TEST
public class MainActivity extends AppCompatActivity implements ApiManager.ApiResponseListener {
    private MediaPlayerManager mediaPlayerManager;
    private ExoPlayerManager exoPlayerManager;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView email, dispName;
    public String songDuration;
    private Button signInButton;
    private LinearLayout linearLayout;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;

    private EditText emailField, passwordField, displayNameField;
    private Button loginButton, signupButton;

    // Declare a FirebaseUser to handle the current user
    private FirebaseUser currentUser;
    // After email/password login, if displayName is null, show a dialog
    private void updateDisplayName(String displayName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Update the displayName using Firebase User Profile
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)  // Set the new display name
                    .build();

            // Update user profile
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "User profile updated.");
                        } else {
                            Log.e("Firebase", "Error updating profile.");
                        }
                    });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Call recreate after 1 second (1000 milliseconds)
                    recreate();
                }
            }, 1300);
        }
    }
    private void promptForDisplayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && (user.getDisplayName() == null || user.getDisplayName().isEmpty())) {
            // Show dialog to prompt for a name
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("How would you like to be called?");

            final EditText input = new EditText(this);
            input.setHint("Enter your preferred name");
            builder.setView(input);

            builder.setPositiveButton("Continue", (dialog, which) -> {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    // Update the displayName
                    updateDisplayName(newName);
                }else{
                    Toast.makeText(MainActivity.this, "Name can't be empty!", Toast.LENGTH_SHORT).show();
                    promptForDisplayName();
                }
            });
            builder.show();
        }
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK){
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                signInButton.setVisibility(View.GONE);
                                auth = FirebaseAuth.getInstance();
                                linearLayout.setVisibility(View.GONE);
//                                recreate();
                            }
                        }
                    });
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });

    private Button playButton, stopButton, pauseButton, resetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Create AppBarConfiguration with both BottomNav and Drawer items
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_library,  // Bottom navigation items
                R.id.nav_notification, R.id.nav_settings, R.id.nav_account)  // Drawer items
                .setOpenableLayout(binding.drawerLayout)  // Associate drawer layout
                .build();

        linearLayout = findViewById(R.id.linLayout);

        // Setup NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Link the toolbar with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Link the Navigation Drawer with NavController
        NavigationView navigationView = binding.navView;
        NavigationUI.setupWithNavController(navigationView, navController);

        // Link the BottomNavigationView with NavController
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        mediaPlayerManager = new MediaPlayerManager();

        Button openMediaPlayerButton = findViewById(R.id.open_media_player_button);
        openMediaPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Button clicked, trying to load MediaPlayerManager");
                loadFragment(new MediaPlayerManager());
                Log.d("MainActivity", "MediaPlayerManager fragment should now be visible.");
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = findViewById(R.id.emailTextView);
        dispName = findViewById(R.id.displayNameTextView);

        FirebaseApp.initializeApp(this);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);
        auth = FirebaseAuth.getInstance();
        signInButton = findViewById(R.id.googleSignInButton);
        if (user != null) {
            // User is already signed in, hide the button
//            signInButton.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
        } else {
            // Set up the sign-in button click listener if user not signed in
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = googleSignInClient.getSignInIntent();
                    activityResultLauncher.launch(intent);
                }
            });
        }
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });

        //button declaration
//        playButton = findViewById(R.id.playButton); //uncomment if you want to test the functionalities
//        stopButton = findViewById(R.id.stopButton);
//        pauseButton = findViewById(R.id.pauseButton);
//        resetButton = findViewById(R.id.resetButton);


//        playButton.setVisibility(View.INVISIBLE);
//        stopButton.setVisibility(View.INVISIBLE);
//        pauseButton.setVisibility(View.INVISIBLE);
//        resetButton.setVisibility(View.INVISIBLE);

//        String wrong_track = "Baet It";
//        String artist = "moby";
        String track = "heartless";

        ApiManager apiManager = new ApiManager();
        exoPlayerManager = new ExoPlayerManager(this);
//        new ApiManager().fetchArtistFromTrack(track,this); //get back the artist from a selected track
//        new ApiManager().fetchTADB_Artist_ID(track, artist,this); //get the artist id for track search
//        new ApiManager().fetchCorrectedTrackInfo(wrong_track, artist, this); //get corrected artist info
//        new ApiManager().fetchYtURL("112424", this); //get youtube URL for a specified track with the id
//        new ApiManager().fetchAlbumInfo("Cher", "Believe", this);

        // OTI EINAI PANW APO AUTO TO COMMENT EINAI DEPRECATED, XRHSIMOPOIEITAI TA KATW CALLS GIA TA API GIA NA PARETE PISW MIA TIMH TA ALLA EINAI IN SERIES CONNECTED

//        new ApiManager().startTrackServe(track,this); // kanei olh thn diadikasia apo to na brei ton kalitexnh mexri na katebasei to tragoudi (menei na kanei elenxo ean einai)
                                                        // hdh katebasmeno

//        apiManager.fetchMP3file("https://www.youtube.com/watch?v=Jy1D6caG8nU", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if(jsonResponse != null){
//                    Log.d("mp3song", jsonResponse);
//                }else {
//                    Log.d("mp3song", "No artist found");
//                }
//            }
//        });
//        apiManager.fetchArtistFromTrack("porcelain", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if(jsonResponse != null){
//                    Log.d("FetchArtistReturn", jsonResponse);
//                }else {
//                    Log.d("FetchArtistReturn", "No artist found");
//                }
//            }
//        });
//        apiManager.fetchAlbumInfo("Cher", "Believe", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null) {
//                    // Handle the album info (e.g., display it in UI)
//                    Log.d("AlbumInfo", jsonResponse);
//                } else {
//                    Log.d("AlbumInfo", "No album info found");
//                }
//            }
//        });
//
//        apiManager.fetchGenreInfo("disco", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null) {
//                    // Handle the genre info (e.g., display it in UI)
//                    Log.d("GenreInfo", jsonResponse);
//                } else {
//                    Log.d("GenreInfo", "No genre info found");
//                }
//            }
//        });
//
//        apiManager.fetchSimilarTracks("moby", "porcelain", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null){
//                    Log.d("SimilarTracks", jsonResponse);
//                }else {
//                    Log.d("SimilarTracks", "No similar tracks found");
//                }
//            }
//        });

//        new ApiManager().fetchTrackMBID(track, artist, this); // When this is finished it sends the jsonResponse to the onResponseReceived func
                                                                     // The listener is here because the MainActivity is the one listening
      

        //uncomment if you want to test the functionalities
//        playButton.setOnClickListener(v -> {
////            exoPlayerManager.playSong("https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/Ti%20mou%20zitas%20(Live).mp3?alt=media");
//            startMusicService("PLAY");
//        });
//
//        pauseButton.setOnClickListener(v -> {
//            if (exoPlayerManager != null) {
//                exoPlayerManager.pauseSong();
//            }
//            startMusicService("PAUSE");
//        });
//
//
//        stopButton.setOnClickListener(v -> {
//            exoPlayerManager.stopSong();
//            logOut();
//        });
//        resetButton.setOnClickListener(v -> { if (exoPlayerManager != null) {exoPlayerManager.resetSong();}});


        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            // Hide the login UI and proceed
                            linearLayout.setVisibility(View.GONE);
                            recreate();
                        } else {
                            // If sign-in fails
                            Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Handle Sign-Up Button Click
        signupButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            // Hide the login UI and proceed
                            linearLayout.setVisibility(View.GONE);
                            promptForDisplayName();
                        } else {
                            // If sign-up fails
                            Toast.makeText(MainActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        createNotificationChannel();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "MEDIA_CHANNEL_ID",
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    public void startMusicService(String action) {
        Intent serviceIntent = new Intent(this, MediaPlayerService.class);
        serviceIntent.setAction(action);
        startService(serviceIntent); // Start the service with the action
    }

    @Override
    public void onResponseReceived(String jsonResponse) {
        // Extract the song URL and start playing it
        String songUrl = extractSongUrlFromJson(jsonResponse);
        if (songUrl != null) {
            mediaPlayerManager.playSong(Uri.parse(songUrl));
        }
    }

    private String extractSongUrlFromJson(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("track").getString("url"); // Adjust as needed for the actual URL
        } catch (Exception e) {
            Log.e("MainActivity", "Error extracting song URL: " + e.getMessage());
            return null;
        }
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        if (exoPlayerManager != null) {
            exoPlayerManager.release();
        }
        super.onDestroy();
        startMusicService("STOP");
        exoPlayerManager.release(); // Release MediaPlayer resources
    }

}
