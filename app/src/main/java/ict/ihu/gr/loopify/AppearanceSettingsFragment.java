package ict.ihu.gr.loopify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ict.ihu.gr.loopify.ui.settings.SettingsFragment;


public class AppearanceSettingsFragment extends Fragment {

    private static final String APP_SETTINGS = "app_settings";
    private static final String APP_THEME_KEY = "app_theme";

    private RadioGroup radioGroup;
    private RadioButton radioTheme1, radioTheme2, radioTheme3, radioTheme4, radioTheme5;

    public AppearanceSettingsFragment() {
        // Required empty public constructor
    }

    public static AppearanceSettingsFragment newInstance() {
        return new AppearanceSettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appearance_settings, container, false);

        // Set up the back button
        Button backButton = view.findViewById(R.id.back_button_appearance);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop this fragment off the back stack to go back to SettingsFragment
                getParentFragmentManager().popBackStack();

                // Re-show the NavigationView by accessing the parent SettingsFragment
                SettingsFragment settingsFragment = (SettingsFragment) getParentFragment();
                if (settingsFragment != null) {
                    settingsFragment.showNavigationView();  // Show the NavigationView
                }
            }
        });

        // Set up the radio group
        radioGroup = view.findViewById(R.id.theme_radio_group);

        // Load saved theme and set the corresponding radio button checked
        SharedPreferences preferences = requireContext().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        String savedTheme = preferences.getString(APP_THEME_KEY, "default");
        switch (savedTheme) {
            case "aquamarine":
                radioGroup.check(R.id.radio_theme_aquamarine);
                break;
            case "beige":
                radioGroup.check(R.id.radio_theme_beige);
                break;
            case "gold":
                radioGroup.check(R.id.radio_theme_gold);
                break;
            case "ink":
                radioGroup.check(R.id.radio_theme_ink);
                break;
            default:
                radioGroup.check(R.id.radio_theme_default);
        }

        // Set listener for theme changes
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String theme = "default"; // Default theme
            if (checkedId == R.id.radio_theme_aquamarine) {
                theme = "aquamarine";
            } else if (checkedId == R.id.radio_theme_beige) {
                theme = "beige";
            } else if (checkedId == R.id.radio_theme_gold) {
                theme = "gold";
            } else if (checkedId == R.id.radio_theme_ink) {
                theme = "ink";
            }

            // Save the selected theme
            saveTheme(theme);

            // Restart the activity to apply changes
            restartApp();
        });


        return view;
    }

    private void saveTheme(String theme) {
        SharedPreferences preferences = requireContext().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(APP_THEME_KEY, theme);
        editor.apply();
    }

    private void restartApp() {
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        requireActivity().overridePendingTransition(0, 0); // Optional: Remove activity transition animations
        startActivity(intent);
    }

}
