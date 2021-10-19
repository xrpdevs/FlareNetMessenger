package uk.co.xrpdevs.flarenetmessenger;

import android.app.FragmentManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

class MyRecyclerView extends RecyclerView.Adapter<MyRecyclerView.ViewHolder> {

    private ArrayList<HashMap<String, String>> localDataSet;

    FragmentManager fm;
    PleaseWaitDialog d;
    String myAddress;


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textView;
        private final TextView textView2;
        private final TextView textView3;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = view.findViewById(R.id.rv_t1);
            textView2 = view.findViewById(R.id.rv_t2);
            textView3 = view.findViewById(R.id.rv_t3);


            //view.setOnClickListener(this);

        }

        public void setContactName(String name) {
            textView.setText(name);
        }

        public TextView getTextView() {
            return textView; // we're only returning the first view
        }

        public TextView getTextView2() {
            return textView2; // we're only returning the first view
        }

        public TextView getTextView3() {
            return textView3; // we're only returning the first view
        }

        @Override
        public void onClick(View view) {

            PleaseWaitDialog d = new PleaseWaitDialog();
            d.titleText = "Transaction Info";
            d.prompt = "1234";
            d.setCancelable(true);
            d.cancelable = true;
            d.show(fm, "oin k");
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     * @param _fm
     */
    public MyRecyclerView(ArrayList<HashMap<String, String>> dataSet, String _myAddress, FragmentManager _fm) {
        localDataSet = dataSet;
        this.localDataSet = dataSet;
        fm = _fm;
        myAddress = _myAddress;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_transactions, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        // viewHolder.setContactName(localDataSet[position]);
        //  Log.d("View contents:", viewHolder.getItemId()+" "+viewHolder.getTextView().getText().toString());

        int pos = viewHolder.getAdapterPosition();

        HashMap<String, String> items = localDataSet.get(pos);

        if (!items.get("destination").equals(myAddress)) {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFFFCCCC"));

        } else {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFCCFFCC"));
        }

        viewHolder.getTextView().setText(items.get("account"));
        viewHolder.getTextView2().setText(items.get("amount"));
        viewHolder.getTextView3().setText(items.get("destination"));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PleaseWaitDialog d = new PleaseWaitDialog();
                d.titleText = "Transaction Info";
                d.prompt =
                        "Dest: " + items.get("destination") + "\n" +
                                "From: " + items.get("account") + "\n" +
                                "Amnt: " + items.get("amount") + "\n" +
                                "Fee : " + items.get("fee") + "\n" +
                                "Hash: " + items.get("hash");
                d.setCancelable(true);
                d.cancelable = true;
                d.show(fm, "oin k");
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
