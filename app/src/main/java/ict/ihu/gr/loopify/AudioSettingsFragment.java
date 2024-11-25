package ict.ihu.gr.loopify;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ict.ihu.gr.loopify.ui.settings.SettingsFragment;

public class AudioSettingsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AudioSettingsFragment() {
        // Required empty public constructor
    }

    public static AudioSettingsFragment newInstance(String param1, String param2) {
        AudioSettingsFragment fragment = new AudioSettingsFragment();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_settings, container, false);

        // Set up the back button
        Button backButton = view.findViewById(R.id.back_button_audio);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            SettingsFragment settingsFragment = (SettingsFragment) getParentFragment();
            if (settingsFragment != null) {
                settingsFragment.showNavigationView();
            }
        });

        // Initialize the equalizer components
        Switch equalizerToggle = view.findViewById(R.id.equalizer_toggle_switch);
        LinearLayout equalizerContainer = view.findViewById(R.id.equalizer_container);
        Button presetButton = view.findViewById(R.id.equalizer_preset_button);

        // Toggle visibility of the equalizer UI
        equalizerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            equalizerContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Populate the equalizer UI
        addEqualizerControls(equalizerContainer);

        // Define presets with corresponding gain values for each frequency band
        Map<String, int[]> presets = new HashMap<>();
        presets.put("Pop 🎵", new int[]{4, 3, 0, -2, -1});
        presets.put("Rock 🎸", new int[]{5, 4, 2, 1, -3});
        presets.put("Jazz 🎷", new int[]{3, 4, 2, 0, -1});
        presets.put("Electronic 🎧", new int[]{5, 3, 1, 0, -2});
        presets.put("Classical 🎻", new int[]{0, 1, 2, 3, 4});

        // Add a placeholder for custom presets
        final String customPresetKey = "Custom ✨";
        presets.put(customPresetKey, null); // Custom preset initially null

        // Handle preset selection
        presetButton.setOnClickListener(v -> {
            // Create a dialog with options for selecting presets
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.select_preset);

            String[] presetNames = presets.keySet().toArray(new String[0]);

            builder.setItems(presetNames, (dialog, which) -> {
                String selectedPreset = presetNames[which];
                if (customPresetKey.equals(selectedPreset)) {
                    // Save current settings as custom preset
                    int[] currentGains = getCurrentGainValues(equalizerContainer);
                    presets.put(customPresetKey, currentGains);
                }

                int[] gains = presets.get(selectedPreset);

                if (gains != null) {
                    // Apply preset values to the SeekBars
                    for (int i = 0; i < equalizerContainer.getChildCount(); i++) {
                        View bandView = equalizerContainer.getChildAt(i);
                        if (bandView instanceof LinearLayout) {
                            LinearLayout bandContainer = (LinearLayout) bandView;

                            SeekBar seekBar = (SeekBar) bandContainer.getChildAt(1); // SeekBar is the second child
                            TextView gainValueText = (TextView) bandContainer.getChildAt(2); // Gain TextView is the third child

                            seekBar.setProgress(gains[i] + 10); // Update SeekBar value
                            gainValueText.setText(getString(R.string.gain_format, gains[i])); // Update gain text
                        }
                    }

                    // Update the button text to reflect the selected preset
                    presetButton.setText(selectedPreset);
                }
            });

            builder.show();
        });

        return view;
    }

    /**
     * Adds sliders to the equalizer container representing frequency bands.
     * @param equalizerContainer The container layout for the equalizer controls.
     */
    private void addEqualizerControls(LinearLayout equalizerContainer) {
        int[] frequencies = {60, 230, 910, 3600, 14000}; // Frequency bands in Hz
        for (int frequency : frequencies) {
            // Create a container for each frequency band
            LinearLayout bandContainer = new LinearLayout(getContext());
            bandContainer.setOrientation(LinearLayout.VERTICAL);

            // Label for the frequency (static, does not change)
            TextView frequencyLabel = new TextView(getContext());
            frequencyLabel.setText(frequency + " Hz");
            frequencyLabel.setTextSize(16);
            frequencyLabel.setPadding(0, 8, 0, 4);
            bandContainer.addView(frequencyLabel);

            // SeekBar for the frequency band
            SeekBar seekBar = new SeekBar(getContext());
            seekBar.setMax(20); // Representing gain range -10 dB to +10 dB
            seekBar.setProgress(10); // Start at 0 dB (middle position)
            bandContainer.addView(seekBar);

            // Gain TextView for displaying the current gain value
            TextView gainValueText = new TextView(getContext());
            gainValueText.setText(getString(R.string.gain_format, 0)); // Initial value: 0 dB
            gainValueText.setTextSize(14);
            gainValueText.setPadding(0, 4, 0, 8);
            bandContainer.addView(gainValueText);

            // Set SeekBar listener to update gain value dynamically
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int gain = progress - 10; // Convert progress to dB (-10 to +10)
                    gainValueText.setText(getString(R.string.gain_format, gain)); // Update gain display
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Optional: Actions when the user starts interacting with the SeekBar
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Optional: Actions when the user stops interacting with the SeekBar
                }
            });

            // Add the band container to the equalizer layout
            equalizerContainer.addView(bandContainer);
        }
    }

    /**
     * Gets the current gain values from the SeekBars in the equalizer container.
     * @param equalizerContainer The container layout for the equalizer controls.
     * @return An array of the current gain values for each frequency band.
     */
    private int[] getCurrentGainValues(LinearLayout equalizerContainer) {
        int[] currentGains = new int[equalizerContainer.getChildCount()];
        for (int i = 0; i < equalizerContainer.getChildCount(); i++) {
            View bandView = equalizerContainer.getChildAt(i);
            if (bandView instanceof LinearLayout) {
                LinearLayout bandContainer = (LinearLayout) bandView;
                SeekBar seekBar = (SeekBar) bandContainer.getChildAt(1); // SeekBar is the second child
                currentGains[i] = seekBar.getProgress() - 10; // Convert to dB
            }
        }
        return currentGains;
    }
}


