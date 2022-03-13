package com.elhardoum.mycryptoalerts;

import android.os.Bundle;
import android.view.Menu;

import com.elhardoum.mycryptoalerts.viewmodels.Database;
import com.elhardoum.mycryptoalerts.viewmodels.Setting;
import com.elhardoum.mycryptoalerts.viewmodels.Quote;
import com.elhardoum.mycryptoalerts.viewmodels.Symbol;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.elhardoum.mycryptoalerts.databinding.ActivityMainBinding;

import java.util.Objects;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void testData() {
        //Setting test = new Setting("test");

        // all modifications to a realm must happen inside of a write block
        /*Database.getThread().executeTransaction(transactionRealm -> {
            Setting test = transactionRealm.where(Setting.class).equalTo("id", "test").findFirst();
            test.setValue("updated @ " + new java.util.Date());
        });*/
    }
}