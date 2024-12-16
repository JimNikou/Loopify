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

    // Variables to store metadata until both duration and track info are known
    private long cachedDuration = -1;
    private String cachedTitle = null;
    private String cachedArtist = null;

    // Handler and Runnable to update track position periodically
    private Handler positionHandler = new Handler(Looper.getMainLooper());
    private Runnable positionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayerManager != null && isPlaying) {
                long currentPosition = exoPlayerManager.getCurrentPosition();
                notifyPlaybackPosition(currentPosition);
            }
            positionHandler.postDelayed(this, 1000); // Update every 1 second
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        exoPlayerManager = new ExoPlayerManager(this);

        // Set a DurationListener so we know when the player is ready and duration is available
        exoPlayerManager.setDurationListener(durationStr -> {
            long durationMs = exoPlayerManager.getSongDuration();
            cachedDuration = durationMs;
            tryNotifyMetadata();
        });
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(1, getNotification());
        String action = intent.getAction();

        switch (action) {
            case "PLAY":
                String trackName = intent.getStringExtra("TRACK_NAME");
                if (trackName == null || trackName.isEmpty()) {
                    if (currentTrack != null && !currentTrack.isEmpty()) {
                        trackName = currentTrack;
                    } else {
                        Log.e("MediaPlayerService", "TRACK_NAME extra is missing and currentTrack is empty. Cannot play.");
                        break;
                    }
                }
                Log.d("MediaPlayerService", "PLAY action received for track: " + trackName);

                if (!trackName.equals(currentTrack)) {
                    currentTrack = trackName;
                    stopCurrentTrack();
                    runStartTrackServe(currentTrack, exoPlayerManager);
                    Intent broadcastIntent = new Intent("SHOW_MEDIA_PLAYER");
                    sendBroadcast(broadcastIntent);
                } else {
                    exoPlayerManager.continuePlaying();
                    Log.d("MediaPlayerService", "Resumed playing: " + currentTrack);
                }
                isPlaying = true;
                updateNotification();
                notifyCurrentTrack();
                break;

            case "PAUSE":
                exoPlayerManager.pauseSong();
                isPlaying = false;
                Log.d("MediaPlayerService", "Playback paused");
                updateNotification();
                notifyCurrentTrack();
                break;

            case "SEEK":
                int newPosition = intent.getIntExtra("NEW_POSITION", 0);
                exoPlayerManager.seekTo(newPosition);
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

    private void notifyMetadata(long duration, String title, String artist) {
        Intent metadataIntent = new Intent("CURRENT_TRACK_METADATA");
        metadataIntent.putExtra("DURATION", duration);
        metadataIntent.putExtra("TITLE", title);
        metadataIntent.putExtra("ARTIST", artist);
        sendBroadcast(metadataIntent);
    }

    private void notifyPlaybackPosition(long position) {
        Intent positionIntent = new Intent("CURRENT_TRACK_POSITION");
        positionIntent.putExtra("POSITION", position);
        sendBroadcast(positionIntent);
    }

    private void stopCurrentTrack() {
        if (exoPlayerManager != null && isPlaying) {
            exoPlayerManager.stopSong();
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
                    isTrackServing = false;
                });
            } else {
                Log.d("ApiManager", "Download response was null.");
                isTrackServing = false;
            }
        });
    }

    private boolean metadataNotified = false; // Added flag to ensure single metadata notification

    private void playTrack(String matchedTitle, ExoPlayerManager exo) {
        String completeUrl = "http://loopify.ddnsgeek.com:20080/downloads/" + matchedTitle.trim() + ".mp3";
        Log.d("ApiManager", "Playing track: " + completeUrl);

        String trackName = currentTrack;

        mainHandler.post(() -> {
            exo.stopSong();
            // Reset cached metadata variables and flag each time we start a new track
            cachedDuration = -1;
            cachedTitle = null;
            cachedArtist = null;
            metadataNotified = false;

            exo.playSong(completeUrl); // start playback
            isPlaying = true;
            isTrackServing = false;

            ApiManager apiManager = new ApiManager();

            // First, get the artist from the track name
            apiManager.fetchArtistFromTrack(trackName, artistJsonResponse -> {
                if (artistJsonResponse != null && !artistJsonResponse.isEmpty()) {
                    final String derivedArtistName = artistJsonResponse.trim(); // Make it final

                    // Now fetch detailed track info
                    apiManager.fetchTrackInfo(trackName, derivedArtistName, trackInfoJson -> {
                        if (metadataNotified) return;

                        String finalArtist = derivedArtistName; // This can be reassigned here because finalArtist is a new variable inside this lambda

                        if (trackInfoJson != null && trackInfoJson.contains("\"track\"")) {
                            try {
                                org.json.JSONObject jsonObject = new org.json.JSONObject(trackInfoJson);
                                org.json.JSONObject trackObj = jsonObject.getJSONObject("track");
                                String artistNameFromJson = trackObj.getJSONObject("artist").getString("name");

                                if (!isArtistNameValid(artistNameFromJson)) {
                                    artistNameFromJson = derivedArtistName;
                                }
                                finalArtist = artistNameFromJson;
                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                                // fallback if parsing fails
                                if (!isArtistNameValid(derivedArtistName)) {
                                    // Since derivedArtistName is final, we can't reassign it, but we can reassign finalArtist
                                    finalArtist = "Unknown Artist";
                                } else {
                                    finalArtist = derivedArtistName;
                                }
                            }
                        } else {
                            if (!isArtistNameValid(derivedArtistName)) {
                                finalArtist = "Unknown Artist";
                            }
                        }

                        cachedTitle = trackName;
                        cachedArtist = finalArtist;
                        tryNotifyMetadata();
                    });
                } else {
                    cachedTitle = trackName;
                    cachedArtist = "Unknown Artist";
                    tryNotifyMetadata();
                }
            });


            startPositionUpdates();
        });
    }

    private boolean isArtistNameValid(String artistName) {
        // If it looks like a URL or JSON, consider it invalid
        if (artistName == null) return false;
        String lower = artistName.toLowerCase();
        if (lower.contains("http") || lower.contains("{\"message\"")) {
            return false;
        }
        return true;
    }

    private void tryNotifyMetadata() {
        if (metadataNotified) return; // Already notified once
        if (cachedDuration > 0 && cachedTitle != null && cachedArtist != null) {
            notifyMetadata(cachedDuration, cachedTitle, cachedArtist);
            metadataNotified = true;
        }
    }


    private void startPositionUpdates() {
        positionHandler.post(positionUpdateRunnable);
    }

    private void updateNotification() {
        Notification notification = getNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private Notification getNotification() {
        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction("PLAY");
        if (currentTrack == null || currentTrack.isEmpty()) {
            currentTrack = "Default Track Name";
        }
        playIntent.putExtra("TRACK_NAME", currentTrack);

        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, MediaPlayerService.class);
        pauseIntent.setAction("PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action action = isPlaying ?
                new NotificationCompat.Action(R.drawable.pause_svgrepo_com, "Pause", pausePendingIntent) :
                new NotificationCompat.Action(R.drawable.play_svgrepo_com, "Play", playPendingIntent);

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
        positionHandler.removeCallbacks(positionUpdateRunnable);
    }
}
