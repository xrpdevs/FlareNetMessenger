package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsManager {
    private static final String MIMETYPE = "vnd.android.cursor.item/com.sample.profile";

    public static String updatePubKey(Context context, String walletAddress, String pubkey){
        ContentResolver resolver = context.getContentResolver();
        String retval ="";
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                .withSelection(Data.DATA3+" LIKE ?", new String[]{walletAddress})
                .withValue(Data.MIMETYPE, MIMETYPE)
                .withValue(Data.DATA4, pubkey)
                .build());
        try {
            myLog("DATABASE", ops.toString());
            ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            myLog("TEST", "RAWCONTACT UPDATE (count = " + results.length + ") " + Arrays.toString(results));


         //   int contactId = Integer.parseInt(results[results.length-1].uri.getLastPathSegment());

//            final String[] projection = new String[] { ContactsContract.RawContacts.CONTACT_ID };
//            final Cursor cursor = resolver.query(results[results.length-1].uri, projection, null, null, null);
//            cursor.moveToNext();
//            long contactId = cursor.getLong(0);


            retval = "1";//String.valueOf(contactId);
            // myLog("TEST", "Generated RAWContact ID: "+contactId);
//            cursor.close();
            if (results.length == 0)
                ;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return retval;

    }

    public static String getPubKey(Context context, String walletAddress){
        String filter = " ( " + ContactsContract.Data.DATA3 + "=? AND data5 =? ) ";
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Data.DATA4};
        String[] filterParams = new String[]{walletAddress, "Coston"};
        String pubkey = null;
        Cursor cursor = context.getContentResolver().query(uri, projection, filter, filterParams, null);
        if (cursor != null) {
            while (cursor.moveToFirst()) {
                @SuppressLint("Range") String pubKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4));
                myLog("temp", DatabaseUtils.dumpCurrentRowToString(cursor));
                return pubKey;
            }
            //cursor.close();
        } else {
            myLog("temp", "Cursor = null");
            return pubkey;
        }
        return pubkey;
    }


    public static String addContact(Context context, MyContact contact, String blockChainID) {
        ContentResolver resolver = context.getContentResolver();
        //  resolver.delete(RawContacts.CONTENT_URI, RawContacts.ACCOUNT_TYPE + " = ?", new String[] { AccountGeneral.ACCOUNT_TYPE });

        String retval = "0";

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        int rcID = getRawContactId(context, contact.id);

        myLog("TEST", "contact vals: " + contact.tag);
        myLog("TEST", "contact vals: " + contact.id);
        myLog("TEST", "contact vals: " + contact.XRPAddr);
        myLog("TEST", "contact vals: " + contact.displayname);

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI, true))
                .withValue(RawContacts.ACCOUNT_NAME, blockChainID)
                .withValue(RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                //.withValue(RawContacts.SOURCE_ID, 12345)
                //.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED)
                .build());

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(Settings.CONTENT_URI, true))
                .withValue(RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME)
                .withValue(RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                .withValue(Settings.UNGROUPED_VISIBLE, 1)
                .build());

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, contact.displayname)
                .build());

     /*   ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, rcID)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "+447871922227")
                .build());
*/

  /*      ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, "sample@email.com")
                .build());
*/

        ops.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, MIMETYPE)
                .withValue(Data.DATA1, contact.displayname)    // probably we dont need this..
                .withValue(Data.DATA2, contact.tag)            // for addresses with "tags"
                .withValue(Data.DATA3, contact.XRPAddr)        // wallet address
                .withValue(Data.DATA5, blockChainID)           // for making sure we only display relevant contacts.
                .build());
        try {
            ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            myLog("TEST", "RAWCONTACT Add " + Arrays.toString(results));

            int contactId = Integer.parseInt(results[results.length-1].uri.getLastPathSegment());

//            final String[] projection = new String[] { ContactsContract.RawContacts.CONTACT_ID };
//            final Cursor cursor = resolver.query(results[results.length-1].uri, projection, null, null, null);
//            cursor.moveToNext();
//            long contactId = cursor.getLong(0);
            retval = String.valueOf(contactId);
            myLog("TEST", "Generated RAWContact ID: " + contactId);
//            cursor.close();
            if (results.length == 0)
                ;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    public static int getRawContactId(Context context, int contactId) {
        myLog("TEST", "Contact Id: " + contactId);
        String[] projection = new String[]{ContactsContract.RawContacts._ID};
        String selection = ContactsContract.RawContacts.CONTACT_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(contactId)};
        Cursor c = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            @SuppressLint("Range") int rawContactId = c.getInt(c.getColumnIndex(ContactsContract.RawContacts._ID));
            myLog("TEST", "Contact Id: " + contactId + " Raw Contact Id: " + rawContactId);
            return rawContactId;
        } else return 0;
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
        return uri;
    }

    public static List<MyContact> getMyContacts() {
        return null;
    }

    public static void deleteContact(long rawid, ContentResolver contentResolver) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        Uri uri = ContactsContract.RawContacts.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(
                        ContactsContract.CALLER_IS_SYNCADAPTER,
                        "true")
                .build();
        ops.add(ContentProviderOperation
                .newDelete(uri)
                .withSelection(
                        ContactsContract.RawContacts._ID + " = ?",
                        new String[]{Long.toString(rawid)})
                .build());

        try {
            ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY,
                    ops);
            myLog("CPR", contentProviderResults.toString());
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static String deleteRawContactID(Context context, long rcID) {

        ContentResolver resolver = context.getContentResolver();
        deleteContact(rcID, resolver);
//        int deleted = resolver.delete(
        //              RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(
        //                     ContactsContract.CALLER_IS_SYNCADAPTER, "true"
        //                     ).build(),
        //             RawContacts._ID + " = ?",
        //             new String[] {String.valueOf(rcID)});
        //     myLog("Delete Contact where "+RawContacts._ID+"="+rcID+": ", String.valueOf(deleted));
        return String.valueOf(rcID);
    }

    public static String deleteAllAppContacts(Context context){
        ContentResolver resolver = context.getContentResolver();

        int deleted = resolver.delete(
                RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(
                        ContactsContract.CALLER_IS_SYNCADAPTER, "true").build(),
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?",
                new String[] {AccountGeneral.ACCOUNT_TYPE});
        return String.valueOf(deleted);
    }

    public static void updateMyContact(Context context, String name) {
        int id = -1;
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[] { Data.RAW_CONTACT_ID, Data.DISPLAY_NAME, Data.MIMETYPE, Data.CONTACT_ID },
                StructuredName.DISPLAY_NAME + "= ?",
                new String[] {name}, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                Log.i("info",cursor.getString(0));
                Log.i("info",cursor.getString(1));
                Log.i("info",cursor.getString(2));
                Log.i("info",cursor.getString(3));

            } while (cursor.moveToNext());
        }
        if (id != -1) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValue(Data.RAW_CONTACT_ID, id)
                    .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                    .withValue(Email.DATA, "sample")
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValue(Data.RAW_CONTACT_ID, id)
                    .withValue(Data.MIMETYPE, MIMETYPE)
                    .withValue(Data.DATA1, "profile")
                    .withValue(Data.DATA2, "profile")
                    .withValue(Data.DATA3, "profile")
                    .build());

            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i("info", "id not found");
        }


    }

    @SuppressLint("Range")
    public static String findContactByAddress(Context context, String address) {
        // Build the Entity URI.
        Cursor cursor;
        String ACCOUNT_TYPE = "uk.co.xrpdevs.flarenetmessenger";
        String filter = "( " + ContactsContract.RawContacts.ACCOUNT_TYPE + " =? AND " + ContactsContract.Data.DATA3 + " = ?) ";
        filter = ContactsContract.Data.DATA3 + " =? ";
        // String[] filter = new String[] {"data3 =?", "data5 =?"};
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.DATA3,
                ContactsContract.Data.DATA5};
        // projection = null;
        String[] filterParams = new String[]{
                /*ACCOUNT_TYPE,*/ address};
        cursor = FlareNetMessenger.getContext().getContentResolver().query(
                uri, projection,
                filter,
                filterParams,
                null);
        String name = address;

        int dataIdx = cursor.getColumnIndex(ContactsContract.Contacts.Entity.DISPLAY_NAME);
        Log.d("SQL DATAIDX", dataIdx + " Records: " + cursor.getCount() + "Qu: " + address);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String data3 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA3));

                    myLog("tempbacons", name + " " + data3);
                    // cName.setText(name);

                } catch (Exception e) {
                    Log.d("CURSOR EXCEPTION", "" + e);
                }
            }
            cursor.close();
        } else {
            myLog("temp", "Cursor = null");
            name = address + " ";
        }
        return name;
    }

}
