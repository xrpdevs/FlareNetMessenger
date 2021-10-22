package uk.co.xrpdevs.flarenetmessenger;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

class MyRecyclerView extends RecyclerView.Adapter<MyRecyclerView.ViewHolder> {

    private ArrayList<HashMap<String, String>> localDataSet;

    FragmentManager fm;
    PleaseWaitDialog d;
    String myAddress;
    String chain_type;
    Context ctx = FlareNetMessenger.getContext();
    HashMap<String, String> wAddrs = null;
    ViewHolder vh;


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends ContextMenuRecyclerView.ViewHolder implements View.OnClickListener {
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


            d.titleText = "Transaction Info";
            d.prompt = "1234";
            d.setCancelable(true);
            d.cancelable = true;
            d.show(fm, "oin k");
        }

        //@Override
        //public boolean onLongClick(View view) {

        //     view.showContextMenu(view.getPivotX(),view.getPivotY());
        //    return true;
        //}
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     * @param _fm
     */
    public MyRecyclerView(ArrayList<HashMap<String, String>> dataSet, String _myAddress, FragmentManager _fm) throws JSONException {
        localDataSet = dataSet;
        this.localDataSet = dataSet;
        fm = _fm;
        myAddress = TransactionsActivity.myAddress;
        d = new PleaseWaitDialog();
        wAddrs = Utils.walletAddressesToWalletNamesOrContactsToHashMap(ctx);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_transactions, viewGroup, false);
        //view.setOnLongClickListener();
        ViewHolder vi = new ViewHolder(view);
        //  vi.itemView.getCon
        vh = vi;
        return vi;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        // viewHolder.setContactName(localDataSet[position]);
        //  Log.d("View contents:", viewHolder.getItemId()+" "+viewHolder.getTextView().getText().toString());

        int pos = viewHolder.getBindingAdapterPosition();
        Log.d("MENU: pos", pos + " itemid " + viewHolder.itemView.getId());
        HashMap<String, String> items = localDataSet.get(pos);


        if (!items.get("destination").equals(myAddress)) { // outgoing funds
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#30CC3030"));

        } else { // incoming funds
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#3030CC30"));
            localDataSet.get(pos).put("isrx", "true");
            items.put("isrx", "true");
        }

        items.put("o_acco", items.get("account"));
        localDataSet.get(pos).put("o_acco", items.get("account"));
        items.put("o_dest", items.get("destination"));
        localDataSet.get(pos).put("o_dest", items.get("destination"));


        String account = wAddrs.getOrDefault(items.get("account"), items.get("account"));
        String destina = wAddrs.getOrDefault(items.get("destination"), items.get("destination"));


        if (account.equals(items.get("account"))) {
            String tmp = account;
            account = ContactsManager.findContactByAddress(ctx, account);
            wAddrs.put(tmp, account + " ");
        }
        if (destina.equals(items.get("destination"))) {
            String tmp = destina;
            destina = ContactsManager.findContactByAddress(ctx, destina);
            wAddrs.put(tmp, destina + " ");
        }


        viewHolder.getTextView().setText(account);
        viewHolder.getTextView2().setText(items.get("amount"));
        viewHolder.getTextView3().setText(destina);

        String finalAccount = account;
        String finalDestina = destina;
        //viewHolder.itemView.setContextClickable(true);
        TransactionsActivity.thepos = pos;
        //viewHolder.itemView.setOnLongClickListener(viewHolder);
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu(v.getPivotX(), v.getPivotY());

                //setPosition(viewHolder.getPosition());
                return false;
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PleaseWaitDialog d = new PleaseWaitDialog();
                d.titleText = "Transaction Info";
                d.prompt =
                        "Dest: " + finalDestina + "\n" +
                                "From: " + finalAccount + "\n" +
                                "Amnt: " + items.get("amount") + "\n" +
                                "Fee : " + items.get("fee") + "\n" +
                                "Hash: " + items.get("hash");
                if (items.get("memos").contains("memoData")) {
                    try {
                        JSONArray memos = new JSONArray(items.get("memos"));

                        for (int m = 0; m < memos.length(); m++) {
                            Log.d("MEMO", memos.getString(m));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String mt = items.get("memos");
                    mt = mt.substring(mt.indexOf("memoData") + 9, mt.indexOf("}"));
                    mt = hexToAscii(mt);
                    d.prompt += "\nMemo: " + mt;
                }
                d.setCancelable(true);
                d.cancelable = true;
                d.show(fm, "oin k");
                Log.d("waddrs", wAddrs.toString());
            }
        });

    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
