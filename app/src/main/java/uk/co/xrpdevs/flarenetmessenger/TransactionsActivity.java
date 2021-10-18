package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TransactionsActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;


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
        myLog("TRANSACTIONS", "onCreateCalled");

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        BottomNavigationView navView = findViewById(R.id.nav_view);

        if (!prefs.contains("walletCount")) {
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        } else {
            setContentView(R.layout.activity_transactions);
        }

        setTitle("Transaction History");

    }
}
