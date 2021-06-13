package uk.co.xrpdevs.flarenetmessenger;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import uk.co.xrpdevs.flarenetmessenger.ui.contacts.ContactsFragment;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class MainActivity extends AppCompatActivity {
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

        if (!Utils.isMyServiceRunning(MyService.class, this)) {
            try {
                Intent serviceIntent = new Intent(this, MyService.class);
                startService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        myLog("FRAG", "onCreateCalled");

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        //  EasyLock.checkPassword(this);
        if (!prefs.contains("walletCount")) {
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        } else {
            setContentView(R.layout.activity_main2);

        }

        if (!prefs.contains("pinCode") || prefs.getInt("walletCount", 0) == 0) {
            Intent firstRun = new Intent(this, FirstRun.class);
            startActivity(firstRun);
        } else {
            //    setContentView(R.layout.activity_main2);
            //    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            //            R.id.navigation_home, R.id.navigation_contacts, R.id.navigation_wallets, R.id.navigation_messages, R.id.navigation_tokens)
            //            .build();
            //    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            //    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            //    NavigationUI.setupWithNavController(navView, navController);
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
            if (incoming.getStringExtra("selectFragment").equals("home")) {
                myLog("FRAG", "Has selectFragment = " + incoming.getStringExtra("selectFragment"));

                navView.setSelectedItemId(R.id.navigation_home);
                currentFragment = new ContactsFragment();
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, currentFragment);
                ft.commit();
            }

        }

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!prefs.contains("pinCode") || prefs.getInt("walletCount", 0) == 0) {
            Intent firstRun = new Intent(this, FirstRun.class);
            startActivity(firstRun);
        } else {
            //    setContentView(R.layout.activity_main2);
            //    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            //            R.id.navigation_home, R.id.navigation_contacts, R.id.navigation_wallets, R.id.navigation_messages, R.id.navigation_tokens)
            //            .build();
            //    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            //    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            //    NavigationUI.setupWithNavController(navView, navController);

            BottomNavigationView navView = findViewById(R.id.nav_view);
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_contacts, R.id.navigation_wallets, R.id.navigation_messages, R.id.navigation_tokens)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
            if ((this.checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) ||
                    (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) ||
                    (this.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) !=
                            PackageManager.PERMISSION_GRANTED) ||
                    (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                            PackageManager.PERMISSION_GRANTED)
                //requires android.permission.READ_CONTACTS or android.permission.WRITE_CONTACTS
            ) {
                myLog("TEST", "No camera and storage permission");
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 50);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}