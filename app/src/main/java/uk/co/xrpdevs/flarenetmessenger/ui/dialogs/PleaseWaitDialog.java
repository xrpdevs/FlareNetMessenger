package uk.co.xrpdevs.flarenetmessenger.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import uk.co.xrpdevs.flarenetmessenger.R;

public class PleaseWaitDialog extends android.app.DialogFragment {

    public String prompt;

    public String titleText = "Alert";

    PleaseWaitDialog mC = this;

    public Boolean cancelable;

    public Boolean hasButtons;

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = null; //Context.LAYOUT_INFLATER_SERVICE));

            inflater = LayoutInflater.from(builder.getContext());
            //View view = new View(inflater.getContext());
           // view.setMinimumHeight(30);

        View view = inflater.inflate(R.layout.dialog_pwd, null);
        builder.setView(view);

        TextView title = new TextView(builder.getContext());
// You Can Customise your Title here
        title.setText(titleText);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        //  builder.set
        // if (hasButtons) {

        //  }

        builder.setMessage(prompt);
        setCancelable(cancelable);
        return builder.create();
    }
}