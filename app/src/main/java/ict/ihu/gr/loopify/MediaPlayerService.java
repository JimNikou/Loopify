package ict.ihu.gr.loopify;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {
    public ExoPlayerManager exoPlayerManager;
    private boolean isPlaying = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isTrackServing = false;
    private String currentTrack = ""; // To keep track of the currently requested track
    public ImageButton playPauseButton;

    @Override
    public void onCreate() {
        super.onCreate();
        exoPlayerManager = new ExoPlayerManager(this);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Validate intent and action
        if (intent == null || intent.getAction() == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Create and display the notification
        startForeground(1, getNotification());


        // Handle actions like PLAY, PAUSE, STOP
        String action = intent.getAction();
//        playPauseButton.setOnClickListener(v -> {
//            if (isPlaying){
//                intent.setAction("PAUSE");
//            }else{
//                intent.setAction("PLAY");
//            }
////            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button); // Ensure the play icon is shown when stopped
////            Toast.makeText(getContext(), "Song stopped.", Toast.LENGTH_SHORT).show();
//        });
        switch (action) {
            case "PLAY":
                String trackName = intent.getStringExtra("TRACK_NAME");
                if (trackName != null) {
                    Log.d("MediaPlayerService", "PLAY action received for track: " + trackName);

                    if (!trackName.equals(currentTrack)) {
                        currentTrack = trackName;
                        stopCurrentTrack();  // Stop any currently playing track
                        runStartTrackServe(currentTrack, exoPlayerManager);
                        // Broadcast an intent to show the MediaPlayer fragment
                        Intent broadcastIntent = new Intent("SHOW_MEDIA_PLAYER");
                        sendBroadcast(broadcastIntent);
                    }else{
                        exoPlayerManager.continuePlaying();
                        Log.d("MediaPlayerService", "Resumed playing: " + currentTrack);
                    }
                    isPlaying = true;
                } else {
                    Log.e("MediaPlayerService", "TRACK_NAME extra is missing in the PLAY action");
                }
                updateNotification();
                if (isPlaying) {
                    notifyCurrentTrack();
                }
                break;

            case "PAUSE":
                exoPlayerManager.pauseSong();
                isPlaying = false;
                Log.d("MediaPlayerService", "Playback paused");
                updateNotification();

                notifyCurrentTrack();
                break;

            case "STOP":
                Log.d("MediaPlayerService", "Stopping service");
                stopSelf();
                notifyCurrentTrack();
                return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    private void notifyCurrentTrack() {
        Intent trackInfoIntent = new Intent("CURRENT_TRACK_INFO");
        trackInfoIntent.putExtra("CURRENT_TRACK_NAME", currentTrack);
        trackInfoIntent.putExtra("IS_PLAYING", isPlaying);
        sendBroadcast(trackInfoIntent);
    }

    @Nullable
    public void changeIconOnPlayPause(){
        if (isPlaying){
            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_pause_button); // Ensure the play icon is shown when stopped
        }else{
            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button); // Ensure the play icon is shown when stopped
        }
    }

    private void stopCurrentTrack() {
        if (exoPlayerManager != null && isPlaying) {
            exoPlayerManager.stopSong(); // Ensure current song stops
            isPlaying = false;
        }
    }

    void runStartTrackServe(String track, ExoPlayerManager exo) {
        if (isTrackServing) {
            Log.d("ApiManager", "Track is already being served, ignoring further calls.");
            return;
        }

        isTrackServing = true;
        ApiManager apiManager = new ApiManager();
        checkTrackAvailability(track, apiManager, exo);
    }

    private void checkTrackAvailability(String track, ApiManager apiManager, ExoPlayerManager exo) {
        apiManager.fetchSongTitlesFromTxt("http://loopify.ddnsgeek.com:20080/downloads/downloaded_files.txt", response -> {
            if (response != null) {
                String matchedTitle = apiManager.findMatchingSong(track, response);
                if (matchedTitle != null) {
                    playTrack(matchedTitle, exo);
                } else {
                    startTrackDownload(track, apiManager, exo);
                }
            } else {
                Log.d("ApiManager", "Failed to fetch song titles.");
                isTrackServing = false;
            }
        });
    }

    private void startTrackDownload(String track, ApiManager apiManager, ExoPlayerManager exo) {
        apiManager.startTrackServe(track, jsonResponse -> {
            if (jsonResponse != null) {
                apiManager.fetchSongTitlesFromTxt("http://loopify.ddnsgeek.com:20080/downloads/downloaded_files.txt", response -> {
                    if (response != null) {
                        String matchedTitle = apiManager.findMatchingSong(track, response);
                        if (matchedTitle != null) {
                            playTrack(matchedTitle, exo);
                        } else {
                            Log.d("ApiManager", "Failed to locate the downloaded song.");
                        }
                    } else {
                        Log.d("ApiManager", "Failed to fetch song titles after download.");
                    }
                    isTrackServing = false; // Ensure flag is reset after completion
                });
            } else {
                Log.d("ApiManager", "Download response was null.");
                isTrackServing = false;
            }
        });
    }

    private void playTrack(String matchedTitle, ExoPlayerManager exo) {
        String completeUrl = "http://loopify.ddnsgeek.com:20080/downloads/" + matchedTitle.trim() + ".mp3";
        Log.d("ApiManager", "Playing track: " + completeUrl);

        mainHandler.post(() -> {
            exo.stopSong();  // Stop any currently playing song
            exo.playSong(completeUrl);  // Play the new song
            isPlaying = true;
            isTrackServing = false; // Reset isTrackServing when playback starts
        });
    }

    private void updateNotification() {
        Notification notification = getNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private Notification getNotification() {
        // Create the Play Intent with the current track details
        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction("PLAY");
        playIntent.putExtra("TRACK_NAME", currentTrack); // Add the track name
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create the Pause Intent
        Intent pauseIntent = new Intent(this, MediaPlayerService.class);
        pauseIntent.setAction("PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Determine the action based on the playback state
        NotificationCompat.Action action;
        if (isPlaying) {
            action = new NotificationCompat.Action(R.drawable.pause_svgrepo_com, "Pause", pausePendingIntent);
        } else {
            action = new NotificationCompat.Action(R.drawable.play_svgrepo_com, "Play", playPendingIntent);
        }

        // Build the notification
        return new NotificationCompat.Builder(this, "MEDIA_CHANNEL_ID")
                .setSmallIcon(R.drawable.music_note)
                .setContentTitle(currentTrack)
                .setContentText(isPlaying ? "Playing music..." : "Music paused")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                .addAction(action)
                .build();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayerManager != null) {
            exoPlayerManager.release();
        }
    }
}
