package uk.co.xrpdevs.flarenetmessenger;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.util.HashMap;

public class dbHelper extends SQLiteOpenHelper {
    //public netTools nt = new netTools();
    SQLiteDatabase db = getReadableDatabase();
    //    public final SQLiteDatabase dbii = getReadableDatabase();
    private static final String DATABASE_NAME = "fnm";    // Database Name
    private static final String TABLE_WAL = "WAL";   // Table Name
    private static final String TABLE_BLK = "BLK";   // Table Name
    private static final String TABLE_TOK = "TOK";   // Table Name
    private static final String TABLE_MSG = "MSG";   // TOKENS
    private static final String TABLE_TRX = "TRX";   // TRANSACTIONS
    private static final int DATABASE_Version = 1;    // Database Version
    private static final String UID = "_id";            // Column I (Primary Key)
    private static final String NAME = "Name";        // Column II
    private static final String MyPASSWORD = "Password";    // Column III
    private static final String CREATE_WAL = "CREATE TABLE WAL ( ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NAME VARCHAR(64), BCID INTEGER, PUBKEY TEXT, ADDRESS TEXT, ALTADDRESS TEXT, PRIVKEY TEXT," +
            " LEDGER_LASTSEEN BIGINT, LEDGER_BALANCE VARCHAR(64));";
    private static final String CREATE_BLK = "CREATE TABLE BLK ( id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " INTID BIGINT KEY, NAME VARCHAR(64), TOKNAME VARCHAR(20), SYMBOL VARCHAR(20), RPC TEXT, TYPE VARCHAR(10)," +
            " CHAINID VARCHAR(20), ICON VARCHAR(20), TESTNET BOOLEAN);";
    private static final String CREATE_TOK = "CREATE TABLE TOK (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NAME VARCHAR(64), TOKNAME VARCHAR(20), BCID BIGINT, CONTRACT VARCHAR(255), TYPE BIGINT, " +
            "PRECISION INTEGER);";

    public static final String SMS_TABLE_NAME = "sms";
    public static final String SMS_COLUMN_ID = "_id";
    public static final String SMS_COLUMN_TS = "ts";
    public static final String SMS_COLUMN_TYPE = "type";
    public static final String SMS_COLUMN_BODY = "body";
    public static final String SMS_COLUMN_NUM = "num";
    public static final String SMS_COLUMN_VIA = "via";
    public static final String PM_TABLE_NAME = "pm";
    public static final String PM_COLUMN_ID = "_id";
    public static final String PM_COLUMN_TS = "ts";
    public static final String PM_COLUMN_TYPE = "type";
    public static final String PM_COLUMN_BODY = "body";
    public static final String PM_COLUMN_USER = "user";
    private static final int DATABASE_VERSION = 5;

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        Log.d("dbdbdb", context.getDatabasePath(dbHelper.DATABASE_NAME).toString());
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    boolean setupblk(JSONArray blk) throws JSONException {
        Cursor cursor;
        cursor = db.rawQuery("Delete from BLK WHERE 1;", null, null);
        cursor = db.rawQuery("DELETE FROM SQLITE_SEQUENCE WHERE name='BLK';", null, null);
        for (int i = 0; i < blk.length(); i++) {
            JSONObject entry = blk.getJSONObject(i);

            ContentValues contentValues = new ContentValues();
            //contentValues.put("id"     , null);
            contentValues.put("NAME", entry.optString("Name", ""));
            contentValues.put("INTID", String.valueOf(i + 1));
            contentValues.put("TOKNAME", entry.optString("NativeCurrency", ""));
            contentValues.put("SYMBOL", entry.optString("Symbol", ""));
            contentValues.put("RPC", entry.optString("RPC", ""));
            contentValues.put("CHAINID", entry.optString("ChainID", ""));
            contentValues.put("ICON", entry.optString("Icon", ""));
            contentValues.put("TESTNET", entry.optString("Testnet", ""));
            contentValues.put("TYPE", entry.optString("Type", ""));

            db.insert("BLK", null, contentValues);

        }
        return true;

    }

    boolean addWallet(String name, int bcid, String pubkey, String privkey, String address, @Nullable String altaddress, @Nullable int ledger_lastseen, @Nullable String balance) {
        ContentValues cv = new ContentValues();
        cv.put("NAME", name);
        cv.put("PUBKEY", pubkey);
        cv.put("PRIVKEY", privkey);
        cv.put("BCID", bcid);
        cv.put("ADDRESS", address);
        cv.put("ALTADDRESS", altaddress);
        cv.put("LEDGER_LASTSEEN", ledger_lastseen);
        cv.put("LEDGER_BALANCE", balance);
        try {
            db.insertOrThrow("WAL", null, cv);
            return true;
        } catch (Exception e) {
            Log.d("SQL", "Error inserting wallet to db. Exception: " + e);
            return false;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WAL);
        db.execSQL(CREATE_BLK);
        db.execSQL(CREATE_TOK);
    }

    boolean ifTableExists(String tableName) {

        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS BLK");
        db.execSQL("DROP TABLE IF EXISTS TOK");
        db.execSQL("DROP TABLE IF EXISTS WAL");
        db.execSQL("DROP TABLE IF EXISTS TXN");
        db.execSQL("DROP TABLE IF EXISTS MSG");
        onCreate(db);
    }


    public HashMap<String, Integer> unread_total() {
        //SQLiteDatabase dbh = getReadableDatabase();
        String sqla = "select sum(count) from unread where type = 'sms'";
        Log.d("DNSJNI", sqla);
        Integer sms, pms;
        try {
            Cursor res = db.rawQuery(sqla, null);
            if (res.moveToFirst()) {
                sms = res.getInt(0);
            } else {
                sms = 0;
            }
        } catch (SQLiteException e) {
            sms = 0;
        }
        String sqlb = "select sum(count) from unread where type = 'pm'";
        Log.d("DNSJNI", sqlb);
        try {
            Cursor ress = db.rawQuery(sqlb, null);
            if (ress.moveToFirst()) {
                pms = ress.getInt(0);
            } else {
                pms = 0;
            }
        } catch (SQLiteException e) {
            pms = 0;

        }
        HashMap<String, Integer> rs = new HashMap<>();
        rs.put("sms", sms);
        rs.put("pm", pms);
        return rs;
    }

    public int unread(String type, String user, int count) {
        //SQLiteDatabase dbh = getReadableDatabase();
        if (!ifTableExists("unread")) {
            db.execSQL("CREATE TABLE unread (" +
                    "_id INTEGER PRIMARY KEY, " +
                    "type  TEXT, " +
                    "user  TEXT," +
                    "count INTEGER);");

        }
        String sqla = "select count from unread where type = '" + type + "' and user = '" + user + "'";
        Log.d("DNSJNI", sqla);
        Cursor res = db.rawQuery(sqla, null);
        if (res.moveToFirst()) {
            int ret = 0;
            @SuppressLint("Range") int count2 = res.getInt(res.getColumnIndex("count"));
            if (count == 0) ret = count2;
            if (count == -1) {
                String sql = "UPDATE unread set count = '0' where user like '" + user + "' and type = '" + type + "'";
                db.execSQL(sql);
                Log.d("DNSJNI", sql);
                ret = 0;
            } else if (count > 0) {
                String sql = "UPDATE unread set count = '" + (count + count2) + "' where user like '" + user + "' and type = '" + type + "'";
                db.execSQL(sql);
                Log.d("DNSJNI", sql);
                ret = count + count2;
            }
            return ret;
        } else {
            if (count == -1) count = 0;
            ContentValues contentValues = new ContentValues();
            contentValues.put("type", type);
            contentValues.put("user", user);
            contentValues.put("count", count);
            db.insert("unread", null, contentValues);
            return (count);
        }
    }

    public int getLastSMS() {
        return getLast(SMS_TABLE_NAME);
    }

    public int getLastPM() {
        return getLast(PM_TABLE_NAME);
    }

    public int getLast(String table_name) {

        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT ts FROM " + table_name + " ORDER BY ts DESC LIMIT 1;", null);
        int LastSMS;
        if (res.moveToFirst()) {
            if (res == null) {
                LastSMS = 0;
            } else {
                if (res.isNull(res.getColumnIndex("ts"))) {
                    LastSMS = 0;
                } else {
                    LastSMS = res.getInt(res.getColumnIndex("ts"));
                }
            }
        } else {
            LastSMS = 0;

        }
        Log.d("smscseeker:getLast", "Table Name: " + table_name + " = " + LastSMS);
        return (LastSMS);
    }

    @SuppressLint("Range")
    public int getLastChat(String room_name) {

        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT ts FROM chat where room = '" + room_name + "' ORDER BY ts DESC LIMIT 1;", null);
        int LastSMS;
        if (res.moveToFirst()) {
            if (res == null) {
                LastSMS = 0;
            } else {
                if (res.isNull(res.getColumnIndex("ts"))) {
                    LastSMS = 0;
                } else {
                    LastSMS = res.getInt(res.getColumnIndex("ts"));
                }
            }
        } else {
            LastSMS = 0;

        }
        Log.d("DNSJNI", "Room Name lastTS: " + room_name + " = " + LastSMS);
        return (LastSMS);
    }

    public Cursor getChat(String roomName, int ts) {
        SQLiteDatabase dbh = getReadableDatabase();
        String query = "SELECT * FROM chat where room = '" + roomName + "' and ts > " + (ts - 1) + " limit 500;";
        Cursor res = null;
        try {
            res = dbh.rawQuery(query, null);

        } catch (SQLiteException e) {
            Log.e("My App", e.toString(), e);
        }

        return (res);
    }

    public void addChat(String roomName, String userName, String message) {
        SQLiteDatabase dbh = getReadableDatabase();
        //dbh.execSQL("insert into chat ");
        int ts = (int) Math.ceil(System.currentTimeMillis() / 1000);
        ContentValues contentValues = new ContentValues();
        contentValues.put("room", roomName);
        contentValues.put("user", userName);
        contentValues.put("msg", message);
        contentValues.put("ts", ts);
        dbh.insert("chat", null, contentValues);
    }

    public Cursor getSMSInbox() {
        // retrieve the SMS from the local database in a format suitable for listAdapter
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase dbh = getReadableDatabase();
        String query = "SELECT * FROM sms a INNER JOIN ( SELECT `num`, MAX(`ts`) AS MaxSentDate " +
                "FROM sms GROUP BY  `num` ) b ON  a.`num` = b.`num` AND a.`ts` = b.MaxSentDate " +
                "WHERE 1 ORDER BY a.`ts` DESC LIMIT 50;";
        Cursor res = null;
        try {
            res = dbh.rawQuery(query, null);

        } catch (SQLiteException e) {
            Log.e("My App", e.toString(), e);
        }

        return (res);
    }

    public Cursor getPMInbox() {
        SQLiteDatabase dbh = getReadableDatabase();
        String query = "SELECT a.user, ts, type, body, MaxSentDate FROM pm a INNER JOIN ( SELECT `user`, MAX(`ts`) AS MaxSentDate " +
                "FROM pm GROUP BY  `user` ) b ON  a.`user` = b.`user` AND a.`ts` = b.MaxSentDate " +
                "WHERE 1 ORDER BY a.`ts` DESC LIMIT 50;";
        Cursor res = null;
        try {
            res = dbh.rawQuery(query, null);
            Log.d("SQLITE", "Meh: " + res.getCount());

        } catch (SQLiteException e) {
            Log.e("My App", e.toString(), e);
        }
        return (res);
    }

    public Cursor getSMSThread(String threadid) {
        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT * FROM " + SMS_TABLE_NAME + " WHERE num = '" + threadid + "' ORDER BY TS DESC;", null);
        return (res);
        // // get the thread from local sqlite db
    }

    public String a() {
        return ("Hi");
    }

    public Cursor getPMThread(String threadid) {
        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT * FROM " + PM_TABLE_NAME + " WHERE user LIKE '" + threadid + "' ORDER BY TS DESC;", null);
        return (res);
        // // get the thread from local sqlite db
    }

    public boolean insertSMS(String num, String body, String type, int ts) {
        SQLiteDatabase dbh = getReadableDatabase();
        Log.d("insertSMS", num + " " + body + " " + type + " " + ts);
        ContentValues contentValues = new ContentValues();
        contentValues.put(SMS_COLUMN_NUM, num);
        contentValues.put(SMS_COLUMN_BODY, body);
        contentValues.put(SMS_COLUMN_TYPE, type);
        contentValues.put(SMS_COLUMN_TS, ts);
        dbh.insert(SMS_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertPM(String user, String body, String type, int ts) {
        SQLiteDatabase dbh = getReadableDatabase();
        Log.d("insertPM", user + " " + body + " " + type + " " + ts);
        ContentValues contentValues = new ContentValues();
        contentValues.put(PM_COLUMN_USER, user);
        contentValues.put(PM_COLUMN_BODY, body);
        contentValues.put(PM_COLUMN_TYPE, type);
        contentValues.put(PM_COLUMN_TS, ts);
        dbh.insert(PM_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updatePerson(Integer id, String name, String gender, int age) {
        //    SQLiteDatabase db = this.getWritableDatabase();
        //    ContentValues contentValues = new ContentValues();
        //    contentValues.put(PERSON_COLUMN_NAME, name);
        //    contentValues.put(PERSON_COLUMN_GENDER, gender);
        //    contentValues.put(PERSON_COLUMN_AGE, age);
        //    db.update(SMS_TABLE_NAME, contentValues, PERSON_COLUMN_ID + " = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Cursor getPerson(int id) {
        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT * FROM " + SMS_TABLE_NAME + " WHERE " +
                SMS_COLUMN_ID + "=?", new String[]{Integer.toString(id)});
        return res;
    }

    public void clear() {
        SQLiteDatabase dbh = getReadableDatabase();
        dbh.execSQL("DROP TABLE sms;");
        dbh.execSQL("DROP TABLE pm;");
        onUpgrade(dbh, 0, 1);

    }

    public Cursor getAllPersons() {
        SQLiteDatabase dbh = getReadableDatabase();
        Cursor res = dbh.rawQuery("SELECT * FROM " + SMS_TABLE_NAME, null);
        return res;
    }

    public String getContactName(Context context, String phoneNumber) {
        phoneNumber = phoneNumber.replace("+44", "0");
        if (isNumeric(phoneNumber) && phoneNumber.length() > 10) {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return phoneNumber;
            }
            String contactName = null;
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            } else {
                contactName = phoneNumber;
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        } else {
            return null;
        }
    }

    public Integer deleteItem(String type, Integer id) {
        SQLiteDatabase dbh = getReadableDatabase();
        return dbh.delete(type, "_id = ? ",
                new String[]{Integer.toString(id)});
    }

    public Integer deleteConv(String type, String id) {
        Log.d("DEBUGG", id);
        SQLiteDatabase dbh = getReadableDatabase();
        String[] idA = new String[]{id};
        if (type.equals("pm")) {
            String sql = "delete from pm where user like '" + id + "';";
            Log.d("debugg", sql);
            dbh.execSQL(sql);

            return 0; //dbh.delete(type, "num like '?' ", idA);
        } else if (type.equals("sms")) {
            return dbh.delete(type, "num like '?' ", idA);
        } else {
            return 0;
        }
    }

    //  public Integer deleteConv(String type, String contact) {
    //       SQLiteDatabase dbh = getReadableDatabase();
    // return dbh.delete(type, "_id = ? ",
    //   new String[]{Integer.toString(id)});
//    }

    public Integer getRemoteLastTS(String sid) {
        final String SERVER_IP = "104.168.170.80";
        final int SERVER_PORT = 59760;
        Socket bob = null;
        try {
            bob = new Socket(SERVER_IP, SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //outgoing stream redirect to socket
        OutputStream out = null;
        try {
            out = bob.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter output = new PrintWriter(out);
        output.println(sid + "!lastsms");
        output.flush();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(bob.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String st = null;
        try {
            st = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bob.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("smscseeker:lastremSMS", st);

        return (Integer.valueOf(st));

    }

}
