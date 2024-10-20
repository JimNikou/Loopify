package ict.ihu.gr.loopify.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import ict.ihu.gr.loopify.ExoPlayerManager;
import ict.ihu.gr.loopify.MediaPlayerManager;
import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private MediaPlayerManager mediaPlayerManager;
    private ExoPlayerManager exoPlayerManager;


    private Button playButton, stopButton, pauseButton, resetButton;
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Load the ImageCarouselFragment (playlist slider)
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ict.ihu.gr.loopify.ui.ImageCarouselFragment())
                    .commit();
        }

        // Call the setGreetingText method to update the greeting
        setGreetingText();
//        exoPlayerManager = new ExoPlayerManager(requireContext());
//        playButton = root.findViewById(R.id.playButton); //uncomment if you want to test the functionalities
//        stopButton = root.findViewById(R.id.stopButton);
//        pauseButton = root.findViewById(R.id.pauseButton);
//        resetButton = root.findViewById(R.id.resetButton);
//
//
//        playButton.setOnClickListener(v -> exoPlayerManager.playSong("https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/Ti mou zitas (Live).mp3?alt=media"));
//        pauseButton.setOnClickListener(v -> {if (exoPlayerManager != null) {exoPlayerManager.pauseSong();}});
//        stopButton.setOnClickListener(v -> exoPlayerManager.stopSong());
//        resetButton.setOnClickListener(v -> { if (exoPlayerManager != null) {exoPlayerManager.resetSong();}});



        return root;
    }

    private void setGreetingText() {
        // Get the TextView inside the black bar (greeting_text)
        TextView greetingText = binding.greetingText;

        // Get the current time in hours
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int currentHour = Integer.parseInt(sdf.format(new Date()));

        // Determine the appropriate greeting message
        if (currentHour >= 5 && currentHour < 12) {
            greetingText.setText("Good morning user");
        } else if (currentHour >= 12 && currentHour < 18) {
            greetingText.setText("Good afternoon user");
        } else if (currentHour >= 18 && currentHour <= 23) {
            greetingText.setText("Good evening user");
        } else {
            greetingText.setText("Good night user");
        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
