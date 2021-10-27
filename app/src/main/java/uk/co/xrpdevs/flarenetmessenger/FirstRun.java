package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.FlareNetMessenger.dbH;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

public class FirstRun extends AppCompatActivity implements PinCodeDialogFragment.OnResultListener{
    PinCodeDialogFragment pinDialog;
    PleaseWaitDialog notify;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    int pin_progress = 0;
    Boolean pin_valid;
    String pin1 = "abcd", pin2 = "efgh";
    TextView content;
    Activity mThis;
    Button cButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);
        content =findViewById(R.id.firstRunContent);
                mThis = this;
        prefs = getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();


        cButton = findViewById(R.id.frContinue);
myLog("FirstRUN", prefs.toString());

      checkState();


    }

    public void checkState(){
        Cursor c = dbH.getWalletDetails(String.valueOf(1));

            prefs = getSharedPreferences("fnm", 0);
        if (!prefs.contains("pin")) {
            pin_progress=0;
            content.setText(R.string.firstRun_noPin);
            cButton.setOnClickListener(v -> {
                myLog("setPIN", setPIN("0"));
            });
        } else if(dbH.walletCount()==0) {
            //if (!prefs.contains("walletCount")) {
            content.setText(R.string.firstRun_noWallet);
            cButton.setOnClickListener(v -> {
                FirstWallet();
            });
        } else {
            finish();
        }
    }

    public void FirstWallet(){
        Intent i = new Intent(FirstRun.this, PKeyScanner.class);
        i.putExtra("firstrun", true);
        startActivity(i);
    }

    public String setPIN(String pin) {
        FragmentManager manager = getFragmentManager();
        myLog("PIN_Progress", "="+pin_progress);
        myLog("PINS", "Pin1: " + pin1 + ", Pin2: " + pin2);
 //       switch (pin_progress) {
   //         case 0:
                pinDialog = new PinCodeDialogFragment().newInstance(this, "Set New PIN Code", null);
                pinDialog.show(manager, "1"); // we can probably use tag: to indicate progress...
                pinDialog.confirm = true;

                pin_progress++;
                return pin;

            //case 1:
            //    pin1 = pin;
            //    pinDialog = new PinCodeDialogFragment().newInstance(this, "Verify PIN Code", null);
            //    pinDialog.show(manager, "1");

            //    pin_progress++;
            //    return pin;
      //      case 1:
        //        pin2 = pin;
          //      myLog("PINS", "Pin1: " + pin1 + ", Pin2: " + pin2);
                //if (pin1.equals(pin2)) {
                //    pin_progress = 3;
          //      pEdit.remove("pinHash");
          //      pEdit.remove("pinCode");
          //      pEdit.remove("_pin");
          //      pEdit.commit();

          //      pEdit.putString("pin", Utils.pinHash(pin));
          //      pEdit.commit();
          //      pEdit.apply();
          //      Log.d("PINZ", prefs.getAll().toString());
          //      checkState();
          //      return pin;
//                } else {
            //                  showDialog("Pin codes did not match\nPlease try again", true);
            //                pin1 = "abcd";
            //              pin2 = "efgh";
//                    pin_progress = 0;
            //setPIN("0");
//                    return "";
            //              }
//            default:
  //              return pin1;
    //    }
    }


    @Override
    public void onResult(String pinCode, String tag) {
        pinDialog.dismiss();
        pEdit.remove("pinHash");
        pEdit.remove("pinCode");
        pEdit.remove("_pin");
        pEdit.commit();

        pEdit.putString("pin", Utils.pinHash(pinCode));
        pEdit.commit();
        pEdit.apply();
        Log.d("PINZ", prefs.getAll().toString());
        checkState();
        //setPIN(pinCode);
    }

    private boolean showDialog(String prompt, Boolean cancelable) {
        FragmentManager manager = getFragmentManager();

        notify = new PleaseWaitDialog();
        notify.prompt = prompt;
        notify.cancelable = cancelable;
        notify.show(manager, "DialogActivity");
        return true;
    }
}