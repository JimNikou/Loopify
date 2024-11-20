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

import java.util.HashMap;
import java.util.Map;

public class TrackHandler {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final static String TAG = "firestore";

    public void TrackHandler(){}

    public void addSongToLiked(String track) {
        DocumentReference docRef = db.collection(user.getUid()).document("LikedSongs");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> data = document.getData();

                    int nextSn = 1; // Default to sn1 if no songs exist
                    if (data != null) {
                        for (String key : data.keySet()) {
                            if (key.startsWith("sn")) {
                                try {
                                    // Safely parse the number part of the key
                                    int snNumber = Integer.parseInt(key.substring(2));
                                    if (snNumber >= nextSn) {
                                        nextSn = snNumber + 1; // Increment the highest sn number
                                    }
                                } catch (NumberFormatException e) {
                                    Log.w(TAG, "Invalid sn key: " + key, e);
                                }
                            }
                        }
                    }

                    String snKey = "sn" + nextSn;
                    Map<String, Object> newSong = new HashMap<>();
                    newSong.put(snKey, track);

                    db.collection(user.getUid()).document("LikedSongs")
                            .set(newSong, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Song successfully added with key: " + snKey);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding song", e);
                                }
                            });
                } else {
                    Log.w(TAG, "Error fetching liked songs", task.getException());
                }
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
                        // Use a single-element array to hold the sn key to remove, making it "effectively final"
                        final String[] snKeyToRemove = {null};

                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            if (track.equals(entry.getValue())) {
                                snKeyToRemove[0] = entry.getKey();
                                break;
                            }
                        }

                        if (snKeyToRemove[0] != null) {
                            // Delete the specific sn field from Firestore
                            docRef.update(snKeyToRemove[0], FieldValue.delete())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Song successfully removed with key: " + snKeyToRemove[0]);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error removing song", e);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Song not found in liked songs.");
                        }
                    } else {
                        Log.d(TAG, "No liked songs found to remove.");
                    }
                } else {
                    Log.w(TAG, "Error fetching liked songs", task.getException());
                }
            }
        });
    }




    public void getAllLikedSongs(){
        DocumentReference docRef = db.collection(user.getUid()).document("LikedSongs");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
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
