package uk.co.xrpdevs.flarenetmessenger;

import android.accounts.Account;
        import android.content.AbstractThreadedSyncAdapter;
        import android.content.ContentProviderClient;
        import android.content.Context;
        import android.content.SyncResult;
        import android.os.Bundle;
import android.util.Log;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i("info", "Sync adapter created");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i("info", "Sync adapter called");
    //    ContactsManager.addContact(getContext(), new MyContact("sample", "sampleee", "34527"));
    }


}