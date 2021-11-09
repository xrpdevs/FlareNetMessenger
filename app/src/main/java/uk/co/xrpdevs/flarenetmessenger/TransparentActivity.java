package uk.co.xrpdevs.flarenetmessenger;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

public class TransparentActivity extends FragmentActivity {

    private final PleaseWaitDialog pwd = new PleaseWaitDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_transparent);

        pwd.titleText = "Authorise Transaction?";
        pwd.prompt = "A web3 enabled app is trying to send a transaction that may cost you money.\n\n" +
                "If you accept this transaction please enter your PIN";
        pwd.cancelable = true;
        pwd.show(this.getFragmentManager(), "serviceLaunched");


    }

}