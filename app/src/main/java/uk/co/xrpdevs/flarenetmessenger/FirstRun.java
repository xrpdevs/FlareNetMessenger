package uk.co.xrpdevs.flarenetmessenger;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

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
myLog("FirstRUN", "OnCreate()");
setPIN("0");
//       checkState();


    }

    public void checkState(){
        if (!prefs.contains("pinCode")){
            content.setText(R.string.firstRun_noPin);

            cButton.setOnClickListener(v -> {

                myLog("setPIN", setPIN("0"));
            });

        } else {
            content.setText(R.string.firstRun_noWallet);
            cButton.setOnClickListener(v -> {

                FirstWallet();
            });
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
        switch (pin_progress) {
            case 0:
                pinDialog = new PinCodeDialogFragment().newInstance(this, "Set New PIN Code", null);
                pinDialog.show(manager, "1"); // we can probably use tag: to indicate progress...

                pin_progress++;
                return pin;

            case 1:
                pin1 = pin;
                pinDialog = new PinCodeDialogFragment().newInstance(this, "Verify PIN Code", null);
                pinDialog.show(manager, "1");

                pin_progress++;
                return pin;
            case 2:
                pin2 = pin;
                myLog("PINS", "Pin1: " + pin1 + ", Pin2: " + pin2);
                if (pin1.equals(pin2)) {
                    pin_progress = 3;
                    pEdit.putString("pinCode", pin1);
                    pEdit.commit();
                    checkState();
                    return pin1;
                } else {
                    showDialog("Pin codes did not match\nPlease try again", true);
                    pin1 = "abcd";
                    pin2 = "efgh";
                    pin_progress = 0;
                    //setPIN("0");
                    return "";
                }
            default:
                return pin1;
        }
    }


    @Override
    public void onResult(String pinCode, String tag) {
        pinDialog.dismiss();
        setPIN(pinCode);
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