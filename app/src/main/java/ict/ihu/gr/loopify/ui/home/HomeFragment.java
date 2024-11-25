package ict.ihu.gr.loopify.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
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
        View root = binding.getRoot();  // Get the root view for this fragment

        // Load the saved theme
        SharedPreferences preferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String theme = preferences.getString("app_theme", "default");

        // Set the background based on the theme
        int backgroundResource;
        switch (theme) {
            case "aquamarine":
                backgroundResource = R.drawable.background_aquamarine;
                break;
            case "beige":
                backgroundResource = R.drawable.background_beige;
                break;
            case "gold":
                backgroundResource = R.drawable.background_gold;
                break;
            case "ink":
                backgroundResource = R.drawable.background_ink;
                break;
            default:
                backgroundResource = R.drawable.background;
        }

        // Set the background resource to the root view or any specific view you want
        root.setBackgroundResource(backgroundResource);  // Use root view here






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

    @SuppressLint("SetTextI18n")
    private void setGreetingText() {
        // Get the TextView inside the black bar (greeting_text)
        TextView greetingText = binding.greetingText;
        Typeface customFont = ResourcesCompat.getFont(getContext(), R.font.pptelegraf_ultrabold);

        // Get the current time in hours
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int currentHour = Integer.parseInt(sdf.format(new Date()));

        // Get the user's name (replace "Giannis" with dynamic data if available)
        String userName = "Giannis";

        // Determine the appropriate greeting message
        String greeting;
        if (currentHour >= 5 && currentHour < 12) {
            greeting = getString(R.string.greeting_morning, userName);
        } else if (currentHour >= 12 && currentHour < 18) {
            greeting = getString(R.string.greeting_afternoon, userName);
        } else if (currentHour >= 18 && currentHour <= 23) {
            greeting = getString(R.string.greeting_evening, userName);
        } else {
            greeting = getString(R.string.greeting_night, userName);
        }

        // Apply the greeting message
        greetingText.setText(greeting);
        greetingText.setTextSize(20);
        greetingText.setTypeface(customFont);
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
