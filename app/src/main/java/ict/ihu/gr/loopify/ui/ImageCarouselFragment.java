package ict.ihu.gr.loopify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ict.ihu.gr.loopify.R;

public class ImageCarouselFragment extends Fragment {

    private RecyclerView recyclerView;
    private int[] imageResources = { R.drawable.hiphop_photo, R.drawable.house_photo, R.drawable.pop_photo, R.drawable.rap_photo, R.drawable.rock_photo, R.drawable.techno_photo }; // Add your images here

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View root = inflater.inflate(R.layout.fragment_image_carousel, container, false);

        // Set up RecyclerView
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Set up Adapter
        ict.ihu.gr.loopify.ui.carousel.ImageCarouselAdapter adapter = new ict.ihu.gr.loopify.ui.carousel.ImageCarouselAdapter(imageResources);
        recyclerView.setAdapter(adapter);

        return root;
    }
}
