package ict.ihu.gr.loopify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.ImageSongMapper;

public class PlaylistFragment extends Fragment {
    private RecyclerView songListRecycler;
    private ImageView selectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        // Get the arguments passed from the previous fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            int imageResource = bundle.getInt("imageResource");

            // Get a list of songs from ImageSongMapper (now it's List<String>)
            List<String> songs = ImageSongMapper.getSongsForImage(imageResource);

            // Set the selected image
            selectedImage = root.findViewById(R.id.selected_image);
            selectedImage.setImageResource(imageResource);

            // Set up RecyclerView for song list
            songListRecycler = root.findViewById(R.id.song_list_recycler);
            songListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

            // Create the adapter with the list of song names (List<String>)
            PlaylistAdapter adapter = new PlaylistAdapter(songs);
            songListRecycler.setAdapter(adapter);

            // Handle back button
            Button backButton = root.findViewById(R.id.back_button);
            backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        return root; // Return the root view
    }
}
