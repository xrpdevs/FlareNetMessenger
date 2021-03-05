package uk.co.xrpdevs.flarenetmessenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import uk.co.xrpdevs.flarenetmessenger.ui.contacts.ContactsFragment;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class MainActivity2 extends AppCompatActivity {
    Fragment currentFragment;
    FragmentTransaction ft;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;

    public void setActionBarTitle(String title) {
        this.getActionBar().setTitle("title");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        myLog("FRAG", "onCreateCalled");
        setContentView(R.layout.activity_main2);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_contacts, R.id.navigation_wallets, R.id.navigation_messages)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        //  EasyLock.checkPassword(this);
        if(!prefs.contains("walletCount")){
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        }

        if(!prefs.contains("pinCode") || prefs.getInt("walletCount", 0) == 0){
            Intent firstRun = new Intent(this, FirstRun.class);
            startActivity(firstRun);
        }

        Intent incoming = getIntent();

        if (incoming.hasExtra("selectFragment")) {

            if (incoming.getStringExtra("selectFragment").equals("contacts")) {
                myLog("FRAG", "Has selectFragment = " + incoming.getStringExtra("selectFragment"));

                navView.setSelectedItemId(R.id.navigation_contacts);
                currentFragment = new ContactsFragment();
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, currentFragment);
                ft.commit();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}