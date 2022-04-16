package com.elhardoum.mycryptoalerts.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.elhardoum.mycryptoalerts.R;
import com.elhardoum.mycryptoalerts.databinding.FragmentSettingsBinding;
import com.elhardoum.mycryptoalerts.viewmodels.Database;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Database.getSetting("notifications_threshold", data ->
        {            
            getActivity().runOnUiThread(() -> binding.textThreshold.setText(data));
        });

        binding.submitButton.setEnabled(true);
        binding.submitButton.setOnClickListener(ref -> {
            EditText threshold = binding.textThreshold;
            int thresholdValue = 15;

            try { thresholdValue = Integer.parseInt(threshold.getText().toString().trim()); } catch(Exception e) {}

            if ( thresholdValue < 15 ) {
                threshold.requestFocus();
                return;
            }

            threshold.clearFocus();

            Database.setSetting("notifications_threshold", "" + thresholdValue, ok ->
            {
                getActivity().runOnUiThread(() ->
                {
                    Snackbar.make(root,
                            ok ? "Changes saved successfully." : "Error occurred, please try again.",
                            Snackbar.LENGTH_LONG).show();
                });
            });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add_item).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}