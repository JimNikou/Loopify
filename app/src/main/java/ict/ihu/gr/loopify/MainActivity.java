package ict.ihu.gr.loopify;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import ict.ihu.gr.loopify.databinding.ActivityMainBinding;

//TEST TEST TEST TEST TEST TEST TEST TEST TEST

// Custom deserializer to handle the `#text` field
class ImageDeserializer implements JsonDeserializer<Image> {
    @Override
    public Image deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject imageObject = json.getAsJsonObject();
        Image image = new Image();
        // Handle the #text field by mapping it to the text property in Image
        if (imageObject.has("#text")) {
            image.setText(imageObject.get("#text").getAsString());
        }
        return image;
    }
}

public class MainActivity extends AppCompatActivity implements ApiManager.ApiResponseListener, TrackManager.OnTracksFetchedListener  {
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
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private EditText emailField, passwordField, displayNameField;
    private Button loginButton, signupButton;

    private TrackManager trackManager;

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

    final BroadcastReceiver mediaPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("SHOW_MEDIA_PLAYER".equals(intent.getAction())) {
                Log.d("MainActivity", "Received broadcast to show MediaPlayerManager.");
                loadFragment(new MediaPlayerManager());
            }
        }
    };
    private Button playButton, stopButton, pauseButton, resetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter("SHOW_MEDIA_PLAYER");
        registerReceiver(mediaPlayerReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            // Permission granted
//                            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                        } else {
                            // Permission denied
                            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                        }
                    });

//            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            requestNotificationPermission();
        }


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

        // Initialize TrackManager
        trackManager = new TrackManager();


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

        // Fetch tracks for all genres
        trackManager.fetchTracksForAllGenres(apiManager, this);

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

//                apiManager.fetchSimilarTracksRAW("Dr. Dre", "Nuthin' but a \"G\" Thang", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null){
//// Initialize Gson
//                    // Initialize Gson with custom deserializer
//                    Gson gson = new GsonBuilder()
//                            .registerTypeAdapter(Image.class, new ImageDeserializer())  // Register custom deserializer for Image class
//                            .create();
//
//                    try {
//                        // Use TypeToken to specify the exact type (List<Track>) for Gson to parse
//                        TypeToken<List<Track>> typeToken = new TypeToken<List<Track>>() {};
//                        List<Track> tracks = gson.fromJson(jsonResponse, typeToken.getType());
//
//                        // Process the tracks if the response is valid
//                        if (tracks != null && !tracks.isEmpty()) {
//                            for (Track track : tracks) {
//                                // Extract the required details (Name, Artist Name, Image URL)
//                                System.out.println("Track Name: " + track.getName());
//                                System.out.println("Artist Name: " + track.getArtist().getName());
//
//                                // Check if image URLs are available and print them
//                                if (track.getImage() != null && !track.getImage().isEmpty()) {
//                                    // Accessing the first image URL
//                                    String imageUrl = track.getImage().get(0).getText();  // Get the first image's text (URL)
//                                    System.out.println("Image URL: " + imageUrl);
//                                } else {
//                                    System.out.println("No image available.");
//                                }
//                            }
//                        } else {
//                            System.out.println("No tracks found.");
//                        }
//                    } catch (Exception e) {
//                        // Handle parsing errors
//                        System.out.println("Error while parsing JSON: " + e.getMessage());
//                    }
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
           return;
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


    // Method to load the MediaPlayerManager fragment as a full-screen overlay
    private void loadFragment(Fragment fragment) {
        FrameLayout fragmentContainer = findViewById(R.id.fragment_MediaPlayerFragment);
        fragmentContainer.setVisibility(View.VISIBLE);
        fragmentContainer.setBackgroundColor(getResources().getColor(android.R.color.black));

        // Hide other views to simulate full-screen effect
        findViewById(R.id.nav_host_fragment_content_main).setVisibility(View.GONE);
        findViewById(R.id.bottomNavView).setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setCustomAnimations(
//                R.anim.slide_in_up,  // Enter animation
//                R.anim.fade_out,     // Exit animation
//                R.anim.fade_in,      // Pop enter animation
//                R.anim.slide_out_down // Pop exit animation
//        );
        transaction.replace(R.id.fragment_MediaPlayerFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Handle back press to close the media player fragment and restore layout
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            // Restore visibility of main content and bottom navigation
            findViewById(R.id.nav_host_fragment_content_main).setVisibility(View.VISIBLE);
            findViewById(R.id.bottomNavView).setVisibility(View.VISIBLE);

            // Hide the fragment container to avoid overlaying the restored content
            findViewById(R.id.fragment_MediaPlayerFragment).setVisibility(View.GONE);
            findViewById(R.id.fragment_MediaPlayerFragment).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            super.onBackPressed();
        }
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
        unregisterReceiver(mediaPlayerReceiver);
    }
//
    @Override
    public void onTracksFetched(String genre, List<Track> tracks) {
        // Handle the fetched tracks for each genre here
        Log.d("MainActivity", "Tracks fetched for genre: " + genre);
        if (tracks != null && !tracks.isEmpty()) {
            for (Track track : tracks) {
                // You can log track details or update the UI with the fetched tracks
                Log.d("MainActivity", "Track Name: " + track.getName());
                Log.d("MainActivity", "Artist: " + track.getArtist().getName());
                if (track.getImage() != null && !track.getImage().isEmpty()) {
                    // Accessing the first image URL
                    String imageUrl = track.getImage().get(0).getText();  // Get the first image's text (URL)
                    Log.d("MainActivity", "Image URL: " + imageUrl);
                } else {
                    Log.d("MainActivity", "No image available.");
                }
            }
        } else {
            Log.d("MainActivity", "No tracks found for genre: " + genre);
        }
    }

    private void requestNotificationPermission() {
        Log.e("NOTIFICATIONSSTATUS", "INITIAL");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is already granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Show the custom rationale dialog first
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    Log.e("NOTIFICATIONSSTATUS", "IF STATEMENT");

                    // Custom rationale dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Notification Permission")
                            .setMessage("To allow media playback controls in your notifications, we need permission to send you notifications. Please grant this permission for the best experience.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Request the permission directly after the rationale dialog is accepted
                                requestPermissionsFromDialog();
                            })
//                            .setNegativeButton("Deny", (dialog, which) -> {
//                                // Handle denial gracefully
//                                Toast.makeText(this, "Permission denied. Notifications will be disabled.", Toast.LENGTH_SHORT).show();
//                            })
                            .create()
                            .show();
                } else {
                    // If no rationale is needed, directly request permission
                    Log.e("NOTIFICATIONSSTATUS", "ELSE STATEMENT");
                    requestPermissionsFromDialog();
                }
            } else {
                Log.e("NOTIFICATIONSSTATUS", "Permission already granted");
                // Permission is already granted, you can proceed with notifications
                enableNotifications();
            }
        } else {
            Log.w("NOTIFICATIONSSTATUS", "Notification permission not required on this Android version.");
        }
    }

    // This method handles the actual request to trigger the permission
    private void requestPermissionsFromDialog() {
        // Only now request the permission, this will trigger the system's standard permission dialog
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_CODE
        );
    }



    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("NOTIFICATIONSSTATUS", "Permission granted");
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
                enableNotifications();
            } else {
                Log.e("NOTIFICATIONSSTATUS", "Permission denied");

                // Check if user has denied permanently
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    Log.e("NOTIFICATIONSSTATUS", "Permission denied permanently");

                    checkIfShowSettingsDialog();
                } else {
                    Toast.makeText(this, "Permission denied. Notifications will be disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    // Show a dialog directing the user to app settings
    private void showSettingsDialog() {
        // Create a SharedPreferences editor to store the "Don't Show Again" flag
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_permission_denied, null);
        CheckBox dontShowAgainCheckbox = dialogView.findViewById(R.id.dontShowAgainCheckbox);

        // Create the dialog
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setView(dialogView)  // Set the custom layout
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // If the user presses Cancel, show a toast
                    Toast.makeText(this, "Permission not granted. Notifications remain disabled.", Toast.LENGTH_SHORT).show();
                })
                .setOnDismissListener(dialog -> {
                    // If "Don't show again" is checked, save the preference
                    if (dontShowAgainCheckbox.isChecked()) {
                        editor.putBoolean("DontShowSettingsDialog", true);
                        editor.apply();
                    }
                })
                .create()
                .show();
    }

    private void checkIfShowSettingsDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean shouldShowDialog = !sharedPreferences.getBoolean("DontShowSettingsDialog", false);

        if (shouldShowDialog) {
            showSettingsDialog();
        }
    }


    // Enable notifications after permission is granted
    private void enableNotifications() {
        // Add logic to enable notifications here
        Log.i("NOTIFICATIONSSTATUS", "Notifications enabled successfully");
    }


}
