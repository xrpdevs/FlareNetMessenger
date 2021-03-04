package uk.co.xrpdevs.flarenetmessenger;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

public class PleaseWaitDialog extends android.app.DialogFragment {

    public String prompt;


    public Boolean cancelable;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(prompt);
        setCancelable(cancelable);
        return builder.create();
    }
}