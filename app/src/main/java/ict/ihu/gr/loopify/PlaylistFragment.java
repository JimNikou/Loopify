package ict.ihu.gr.loopify;

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
import ict.ihu.gr.loopify.R;

public class PlaylistFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        int imageResource = getArguments() != null ? getArguments().getInt("imageResource") : R.drawable.hiphop_photo;
        ImageView imageView = root.findViewById(R.id.selected_image);
        imageView.setImageResource(imageResource);

        String[] songs = {"Song 1", "Song 2", "Song 3"};
        String[] artists = {"Artist 1", "Artist 2", "Artist 3"};

        RecyclerView recyclerView = root.findViewById(R.id.playlist_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ict.ihu.gr.loopify.ui.PlaylistAdapter adapter = new ict.ihu.gr.loopify.ui.PlaylistAdapter(songs, artists);
        recyclerView.setAdapter(adapter);

        Button backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return root;
    }
}
