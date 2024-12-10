package ict.ihu.gr.loopify.ui.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ict.ihu.gr.loopify.R;
import ict.ihu.gr.loopify.databinding.FragmentNotificationBinding;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationViewModel galleryViewModel =
                new ViewModelProvider(this).get(NotificationViewModel.class);

        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Load the saved theme
        SharedPreferences preferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String theme = preferences.getString("app_theme", "default");


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
