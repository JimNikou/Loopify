// ImageCarouselFragment.java
package ict.ihu.gr.loopify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ict.ihu.gr.loopify.ui.ImageCarouselAdapter;
import ict.ihu.gr.loopify.R;

public class ImageCarouselFragment extends Fragment implements ImageCarouselAdapter.OnImageClickListener {

    private int[] imageResources = {
            R.drawable.hiphop_photo,
            R.drawable.house_photo,
            R.drawable.pop_photo,
            R.drawable.jazz_photo,
            R.drawable.rap_photo,
            R.drawable.rock_photo,
            R.drawable.techno_photo
    };

    private String[] artistNames = {
            "Artist 1", "Artist 2", "Artist 3",
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_carousel, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageResources, artistNames, this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onImageClick(int position, int imageResource) {
        Intent intent = new Intent(getActivity(), ict.ihu.gr.loopify.ui.PlaylistActivity.class);
        intent.putExtra("imageResource", imageResource);
        startActivity(intent);
    }
}
