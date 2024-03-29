package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import uk.co.xrpdevs.flarenetmessenger.ui.contacts.ContactsFragment;

public class MainActivity extends AppCompatActivity {
    Fragment currentFragment;
    FragmentTransaction ft;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    FragmentManager fragmentManager;

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
        fragmentManager = getSupportFragmentManager();
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

        if (!prefs.contains("pin") || prefs.getInt("walletCount", 0) == 0) {
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

    //  public static byte[] BuildSelectApdu(string aid)
    //  {
    // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
    // return HexStringToByteArray("00A40400" + (aid.length() / 2) + aid);
    //   }
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "Discovered tag with intent " + intent);
        String MASTERCARD_AID = "A0000000041010";
// ISO-DEP command HEADER for selecting an AID.
// Format: [Class | Instruction | Parameter 1 | Parameter 2]
        String SELECT_APDU_HEADER = "00A40400";
// "OK" status word sent in response to SELECT AID command (0x9000)
        byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};

// Weak reference to prevent retain loop. mAccountCallback is responsible for exiting
// foreground mode before it becomes invalid (e.g. during onPause() or onStop()).
        // WeakReference<AccountCallback> mAccountCallback;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        IsoDep isoDep = IsoDep.get(tag);

        byte[] result;
        if (isoDep != null)
            try {
                isoDep.connect();
                for (int sfi = 1; sfi < 10; ++sfi) {
                    for (int record = 1; record < 10; ++record) {
                        byte[] dig = Utils.toByte("00B2000400");
                        byte[] bob = {0};
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        outputStream.write(bob);
                        outputStream.write(dig);

                        byte[] cmd = outputStream.toByteArray();
                        cmd[2] = (byte) (record & 0x0FF);
                        cmd[3] |= (byte) ((sfi << 3) & 0x0F8);

                        result = isoDep.transceive(cmd);

                        Log.d("card", "cmd: " + new BigInteger(cmd).toString(16));
                        if ((result != null) && (result.length >= 2)) {

                            Log.d("card", "res: " + new BigInteger(result).toString(16));
                            if ((result[result.length - 2] == (byte) 0x90) && (result[result.length - 1] == (byte) 0x00)) {
                                // file exists and contains data
                                byte[] data = Arrays.copyOf(result, result.length - 2);
                                Log.d("card", data.toString());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("card", "Error: " + e);
            }
        Log.d("onNewIntent", "Tag: " + tag.toString());
        Log.d("onNewIntent", "Extra: " + intent.getExtras().toString());
        String tagId = new BigInteger(tag.getId()).toString(16);
        Log.d("onNewIntent", "ID: " + tagId);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        Log.d("onNewIntent", "Raw: " + Arrays.toString(rawMsgs));
        //TagWrapper tagWrapper = new TagWrapper(tagId);
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!prefs.contains("pin") || prefs.getInt("walletCount", 0) == 0) {
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