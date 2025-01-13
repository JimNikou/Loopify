package ict.ihu.gr.loopify;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link YourLibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class YourLibraryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public YourLibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment YourLibraryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static YourLibraryFragment newInstance(String param1, String param2) {
        YourLibraryFragment fragment = new YourLibraryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private Button dataButton;
    private TextView totalLikedSongs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_library, container, false);
        // Inflate the layout for this fragment

        totalLikedSongs = view.findViewById(R.id.songCount);
        TrackHandler trackHandler = new TrackHandler();
//        trackHandler.removeSongFromLiked("s3");
        trackHandler.getLikedSongsCount(new TrackHandler.LikedSongsCountCallback() { // get back the count of the liked songs
            @Override
            public void onCountRetrieved(int count) {
                Log.d("TrackHandler", "Number of liked songs: " + count);
                totalLikedSongs.setText(count + " Songs");
            }
            @Override
            public void onError(Exception e) {
                Log.d("TrackHandler", "Error fetching liked songs count", e);
            }
        });

        dataButton = view.findViewById(R.id.addData);

        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackHandler trackHandler = new TrackHandler();
//                trackHandler.addSongToLiked("Test song name"); // add a liked song to the database
//                trackHandler.removeSongFromLiked("Despacito");
                trackHandler.getAllLikedSongs(); // get back all songs that are stored in the database
            }
        });

        totalLikedSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use Intent to open another Activity
                Intent intent = new Intent(getActivity(), LikedSongsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}