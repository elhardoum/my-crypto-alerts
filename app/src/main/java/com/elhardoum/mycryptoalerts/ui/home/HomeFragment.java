package com.elhardoum.mycryptoalerts.ui.home;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.elhardoum.mycryptoalerts.MainActivity;
import com.elhardoum.mycryptoalerts.R;
import com.elhardoum.mycryptoalerts.databinding.FragmentHomeBinding;
import com.elhardoum.mycryptoalerts.viewmodels.Database;
import com.elhardoum.mycryptoalerts.viewmodels.Symbol;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    @SuppressLint("ResourceAsColor")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.layoutLoadingSpinner.setVisibility(View.VISIBLE);
        binding.layoutMainContent.setVisibility(View.GONE);

        Database.getSymbols(items ->
        {
            for ( Symbol item: items ) {
                final String itemId = item.getId();

                TableRow row = new TableRow(getActivity().getBaseContext());

                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 0);
                params.setMargins(0, 0, 0, 0);
                params.weight = 1;
                row.setLayoutParams(params);

                row.setPadding(0, 10, 0, 10);

                String[] cols = {
                        item.getSymbol().toUpperCase(Locale.ROOT),
                        item.getMovement().toString(),
                        item.getNotifications().toString()
                };

                for ( int i=0; i<cols.length; i++ ) {
                    TextView text = new TextView(getActivity().getBaseContext());

                    /*text.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));*/

                    text.setTextSize(15);
                    text.setText(cols[i]);
                    text.setTypeface(text.getTypeface(), Typeface.BOLD);
                    text.setTextColor(R.color.black);

                    if ( 1 == i ) {
                        if ( item.getMovement() < 0 ) {
                            text.setTextColor(Color.parseColor("#F44336"));
                        } else {
                            text.setTextColor(Color.parseColor("#4CAF50"));
                        }
                    }

                    row.addView(text);
                }

                ImageView edit = new ImageView(getActivity().getBaseContext());
                edit.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.edit_icon));
                edit.setAdjustViewBounds(true);
                edit.setMaxHeight(55);
                edit.setMaxWidth(55);
                row.addView(edit);

                ImageView delete = new ImageView(getActivity().getBaseContext());
                delete.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.delete_icon));
                delete.setAdjustViewBounds(true);
                delete.setMaxHeight(55);
                delete.setMaxWidth(55);
                row.addView(delete);

                edit.setOnClickListener(ref ->
                {
                    getActivity().runOnUiThread(() ->
                    {
                        MainActivity ac = ((MainActivity) getActivity());

                        Bundle bundle = new Bundle();
                        bundle.putString("editId", itemId);

                        ac.getNavController().navigate(R.id.nav_symbol, bundle);
                        ac.getSupportActionBar().setTitle("Edit Symbol");
                    });
                });

                delete.setOnClickListener(ref -> {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirm Deletion");
                    builder.setMessage("Are you sure you want to delete this symbol entry?");
                    builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Database.deleteSymbol(itemId, ok ->
                            {
                                getActivity().runOnUiThread(() ->
                                        binding.tableLayout.removeView(row));
                            });
                        }
                    });
                    builder.setNegativeButton("CANCEL", null);
                    builder.create().show();
                });

                binding.tableLayout.addView(row);
            }

            binding.layoutLoadingSpinner.setVisibility(View.GONE);
            binding.layoutMainContent.setVisibility(View.VISIBLE);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}