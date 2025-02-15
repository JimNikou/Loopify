package ict.ihu.gr.loopify;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackHandler {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final static String TAG = "firestore";

    public void TrackHandler(){}

    public void addSongToLiked(String trackName, String artistName) {
        DocumentReference docRef = db.collection(user.getUid()).document("LikedSongs");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                Map<String, Object> data = document.getData();

                int nextSn = 1; // Default to sn1 if no songs exist
                if (data != null) {
                    for (String key : data.keySet()) {
                        if (key.startsWith("sn")) {
                            try {
                                int snNumber = Integer.parseInt(key.substring(2));
                                if (snNumber >= nextSn) {
                                    nextSn = snNumber + 1;
                                }
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Invalid sn key: " + key, e);
                            }
                        }
                    }
                }

                String snKey = "sn" + nextSn;
                Map<String, String> songData = new HashMap<>();
                songData.put("name", trackName);
                songData.put("artist", artistName);

                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put(snKey, songData);

                docRef.set(updatedData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Song successfully added with key: " + snKey))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding song", e));
            } else {
                Log.w(TAG, "Error fetching liked songs", task.getException());
            }
        });
    }




    public void removeSongFromLiked(String track) {
        DocumentReference docRef = db.collection(user.getUid()).document("LikedSongs");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> data = document.getData();

                    if (data != null) {
                        // Iterate through the fields to find the matching song
                        String keyToRemove = null;
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            Map<String, Object> songData = (Map<String, Object>) entry.getValue();
                            if (songData != null && track.equals(songData.get("name"))) {
                                keyToRemove = entry.getKey(); // Get the key (e.g., "sn1") to remove
                                break;
                            }
                        }

                        if (keyToRemove != null) {
                            // Delete the specific key from Firestore
                            docRef.update(keyToRemove, FieldValue.delete())
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Song successfully removed: " + track))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error removing song", e));
                        } else {
                            Log.d(TAG, "Song not found in liked songs: " + track);
                        }
                    } else {
                        Log.d(TAG, "No liked songs found.");
                    }
                } else {
                    Log.w(TAG, "Error fetching liked songs", task.getException());
                }
            }
        });
    }


    public void getAllLikedSongs() {
        DocumentReference docRef = db.collection(user.getUid()).document("LikedSongs");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData(); // Retrieve the document data as a Map
                        if (data != null && !data.isEmpty()) {
                            List<String> likedSongsList = new ArrayList<>();

                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                Map<String, String> songDetails = (Map<String, String>) entry.getValue(); // Cast to expected structure
                                String songName = songDetails.getOrDefault("name", "Unknown");
                                String artistName = songDetails.getOrDefault("artist", "Unknown");
                                likedSongsList.add(songName + " - " + artistName);
                            }

                            Log.d(TAG, "Liked Songs: " + likedSongsList);
                        } else {
                            Log.d(TAG, "No songs found in LikedSongs.");
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    public interface LikedSongsCountCallback {
        void onCountRetrieved(int count);
        void onError(Exception e);
    }

    // Method to get the count of liked songs
    public void getLikedSongsCount(LikedSongsCountCallback callback) {
        DocumentReference docRef = db.collection(user.getUid()).document("LikedSongs");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int songCount = document.getData().size(); // Get the number of fields (songs)
                        Log.d(TAG, "Total liked songs: " + songCount);
                        callback.onCountRetrieved(songCount);
                    } else {
                        Log.d(TAG, "No liked songs found");
                        callback.onCountRetrieved(0);
                    }
                } else {
                    Log.d(TAG, "Failed to fetch liked songs", task.getException());
                    callback.onError(task.getException());
                }
            }
        });
    }

}
