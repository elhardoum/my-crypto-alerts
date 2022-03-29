package com.elhardoum.mycryptoalerts.ui.symbol;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.elhardoum.mycryptoalerts.MainActivity;
import com.elhardoum.mycryptoalerts.R;
import com.elhardoum.mycryptoalerts.databinding.FragmentSymbolBinding;
import com.elhardoum.mycryptoalerts.viewmodels.Database;
import com.elhardoum.mycryptoalerts.viewmodels.Symbol;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class SymbolFragment extends Fragment {

    private SymbolViewModel symbolViewModel;
    private FragmentSymbolBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        symbolViewModel = new ViewModelProvider(this).get(SymbolViewModel.class);

        binding = FragmentSymbolBinding.inflate(inflater, container, false);

        binding.layoutLoadingSpinner.setVisibility(View.VISIBLE);
        binding.layoutMainContent.setVisibility(View.GONE);

        View root = binding.getRoot();

        String editId = getArguments() != null ? getArguments().get("editId").toString() : "";
        final Symbol[] editSymbol = new Symbol[1];

        if ( editId.length() > 0 ) {
            Database.getSymbol(editId, data ->
            {
                editSymbol[0] = new Symbol(
                        data.getId(),
                        data.getCoinId(),
                        data.getSymbol(),
                        data.getMovement(),
                        data.getNotifications()
                );

                getActivity().runOnUiThread(() ->
                {
                    if ( 0 == editSymbol[0].getSymbol().length() ) {
                        ((MainActivity) getActivity()).getNavController()
                                .navigate(R.id.nav_home);
                        return;
                    }

                    binding.textSymbol.setText(editSymbol[0].getSymbol());
                    binding.textMovement.setText(editSymbol[0].getMovement().toString());
                });
            });
        }

        Database.getSupportedCrypto(dict ->
        {
            getActivity().runOnUiThread(() ->
            {
                binding.layoutLoadingSpinner.setVisibility(View.GONE);
                binding.layoutMainContent.setVisibility(View.VISIBLE);

                if ( 0 == dict.size() ) {
                    Snackbar.make(root,
                            "Crypto list could not be retrieved. Please try again.",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                binding.submitButton.setEnabled(true);
                binding.submitButton.setOnClickListener(ref -> {
                    EditText symbol = binding.textSymbol;
                    EditText movement = binding.textMovement;

                    String symbolText = symbol.getText().toString().toLowerCase(Locale.ROOT).trim();
                    String mvmtText = movement.getText().toString().trim();

                    if ( 0 == symbolText.length() ) {
                        symbol.requestFocus();
                        return;
                    }

                    String symId = "", symSym = "";

                    for ( String id: dict.keySet() ) {
                        String sym = dict.get(id).get("symbol");

                        if ( sym.toLowerCase(Locale.ROOT).equals(symbolText) ) {
                            symId = id;
                            symSym = sym;
                            break;
                        }
                    }

                    if ( 0 == symId.length() ) {
                        symbol.requestFocus();
                        Snackbar.make(root,
                                "Symbol not found. Please try again or click the help icon in the toolbar for more information.",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    Double mvmt = 0d;

                    try { mvmt = Double.parseDouble(mvmtText); } catch(Exception e) {}

                    if ( mvmt == 0 ) {
                        movement.requestFocus();
                        return;
                    }

                    symbol.clearFocus();
                    movement.clearFocus();

                    String id = "";
                    int notifications = 0;

                    if ( editSymbol[0] != null && editSymbol[0].getSymbol().length() > 0 ) {
                        id = editSymbol[0].getId();
                        notifications = editSymbol[0].getNotifications();
                    }

                    Database.setSymbol(id, symId, symSym, mvmt, notifications, ok ->
                    {
                        getActivity().runOnUiThread(() ->
                        {
                            if ( ! ok ) {
                                Snackbar.make(root,
                                        "Error occurred, please try again.",
                                        Snackbar.LENGTH_LONG).show();
                                return;
                            }

                            ((MainActivity) getActivity()).getNavController()
                                    .navigate(R.id.nav_home);
                        });
                    });
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

        MenuItem info = menu.findItem(R.id.info_item);
        info.setVisible(true);
        info.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Supported Symbols");
                builder.setMessage("Please find your target coin at https://www.coingecko.com/en/coins/all and input its symbol here (e.g BTC, ETH).");
                builder.setPositiveButton("OPEN IN BROWSER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.coingecko.com/en/coins/all"));
                        startActivity(browserIntent);
                    }
                });
                builder.setNegativeButton("OK", null);
                builder.create().show();
                return false;
            }
        });

        super.onPrepareOptionsMenu(menu);
    }
}