package uk.co.xrpdevs.flarenetmessenger;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class dbHelper extends SQLiteOpenHelper {

    SQLiteDatabase db = getReadableDatabase();

    private static final String DATABASE_NAME = "fnm";    // Database Name
    private static final String CREATE_WAL = "CREATE TABLE IF NOT EXISTS WAL ( ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NAME VARCHAR(64), BCID INTEGER, PUBKEY TEXT, ADDRESS TEXT, ALTADDRESS TEXT, PRIVKEY TEXT," +
            " LEDGER_LASTSEEN BIGINT, LEDGER_BALANCE VARCHAR(64));";
    private static final String CREATE_BLK = "CREATE TABLE IF NOT EXISTS BLK ( id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " INTID BIGINT KEY, NAME VARCHAR(64), TOKNAME VARCHAR(20), SYMBOL VARCHAR(20), RPC TEXT, TYPE VARCHAR(10)," +
            " CHAINID VARCHAR(20), ICON VARCHAR(20), TESTNET BOOLEAN);";
    private static final String CREATE_TOK = "CREATE TABLE IF NOT EXISTS TOK (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NAME VARCHAR(64), TOKNAME VARCHAR(20), BCID BIGINT, CONTRACT VARCHAR(255), TYPE BIGINT, " +
            "PRECISION INTEGER);";
    private static final String CREATE_TXN = "CREATE TABLE IF NOT EXISTS TRX (ID INTEGER PRIMARY KEY AUTOINCREMENT, ACCOUNT VARCHAR(128)," +
            " DESTINA VARCHAR(128), AMOUNT VARCHAR(32), FEE VARCHAR(32), BCID BIGINT, TID BIGINT," +
            " MEMO TEXT, SEQ BIGINT KEY, TXID TEXT);";


    private static final int DATABASE_VERSION = 10;

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    public static ArrayList<HashMap<String, String>> cursorToHashMapArray(Cursor c) {
        ArrayList<HashMap<String, String>> out = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < c.getColumnCount(); i++) {
                map.put(c.getColumnName(i), c.getString(i));
            }
            out.add(map);
        }
        if (out.isEmpty()) {
            return null;
        } else {
            return out;
        }
    }


    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public void addTransaction(String account, String dest, String amount, String fee, String bcid, String tid, String memo, String seq, String txid) {
        //SQLiteDatabase db = getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ACCOUNT", account);
        cv.put("DESTINA", dest);
        cv.put("AMOUNT", amount);
        cv.put("FEE", fee);
        cv.put("BCID", bcid);
        cv.put("TID", tid);
        cv.put("MEMO", memo);
        cv.put("SEQ", seq);
        cv.put("TXID", txid);
        db.insertOrThrow("TRX", null, cv);
    }

    void setupBLKTable(JSONArray blk) throws JSONException {

        // Log.e("dbhandle", db.toString());

        // chang to delete records below N (10,000?) so as to preserve user-made blockchain info
        // cursor = db.rawQuery("Delete from BLK WHERE 1;", null, null);
        // cursor = db.rawQuery("DELETE FROM SQLITE_SEQUENCE WHERE name='BLK';", null, null);
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

            db.insertOrThrow("BLK", null, contentValues);
        }

    }

    int addWallet(String name, int bcid, String pubkey, String privkey, String address, String altaddress, int ledger_lastseen, @Nullable String balance) {
        int wc = walletCount();
        ContentValues cv = new ContentValues();
        if (altaddress == null) {
            altaddress = "";
        }
        cv.put("NAME", name);
        cv.put("PUBKEY", pubkey);
        cv.put("PRIVKEY", privkey);
        cv.put("BCID", bcid);
        cv.put("ADDRESS", address);
        cv.put("ALTADDRESS", altaddress);
        cv.put("LEDGER_LASTSEEN", ledger_lastseen);
        cv.put("LEDGER_BALANCE", balance);

        db.insertOrThrow("WAL", null, cv);
        return wallet_last_id();
    }

    public Cursor getWalletDetails(String id) {
        String sql = "SELECT WAL.*, BLK.NAME as BCNAME,TOKNAME,SYMBOL,RPC,TYPE,CHAINID,ICON,TESTNET " +
                "FROM WAL INNER JOIN BLK ON WAL.BCID = BLK.INTID WHERE WAL.ID = " + id + ";";
        try {
            return db.rawQuery(sql, null);
        } catch (SQLiteException e) {
            return null;
        }
    }

    public Cursor getWallets() {
        String sql = "SELECT WAL.*, BLK.NAME as BCNAME,TOKNAME,SYMBOL,RPC,TYPE,CHAINID,ICON,TESTNET " +
                "FROM WAL INNER JOIN BLK ON WAL.BCID = BLK.INTID WHERE 1;";
        try {
            return db.rawQuery(sql, null);
        } catch (SQLiteException e) {
            return null;
        }
    }

    public int walletCount() {
        String sql = "SELECT COUNT(NAME) FROM WAL";
        try {
            Cursor a = db.rawQuery(sql, null);
            if (a.moveToNext()) {
                return a.getInt(0);
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public int wallet_last_id() {
        String sql = "SELECT ID FROM WAL where 1 order by ID DESC LIMIT 1";
        try {
            Cursor a = db.rawQuery(sql, null);
            if (a.moveToNext()) {
                return a.getInt(0);
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public Cursor getBlockChains() {
        String sql = "SELECT * FROM BLK WHERE 1";
        try {
            return db.rawQuery(sql, null);
        } catch (SQLiteException e) {
            return null;
        }
    }

    @SuppressLint("Recycle")
    public HashMap<String, String> getAddrNames(@Nullable String _bcid) {
        String sql;
        Cursor res;
        HashMap<String, String> _this = new HashMap<>();
        if (_bcid == null) _bcid = "0";
        int bcid = Integer.parseInt(_bcid);
        if (bcid == 0) {
            sql = "SELECT NAME, ADDRESS FROM WAL WHERE 1;";
        } else {
            sql = "SELECT NAME, ADDRESS FROM WAL WHERE BCID = ?";
        }
        try {
            res = db.rawQuery(sql, new String[]{_bcid});
            while (res.moveToNext()) {
                _this.put(res.getString(1), res.getString(0));
            }

        } catch (SQLiteException e) {
            return null;
        }
        return _this;
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        db = _db;
        db.execSQL(CREATE_WAL);
        db.execSQL(CREATE_BLK);
        db.execSQL(CREATE_TXN);
        db.execSQL(CREATE_TOK);
        InputStream is = null;
        try {
            is = FlareNetMessenger.getContext().getAssets().open("blockchains.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jo = new JSONArray(json);
            Log.e("JSON", jo.toString());
            setupBLKTable(jo);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

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
//        db.execSQL("DROP TABLE IF EXISTS WAL");
        db.execSQL("DROP TABLE IF EXISTS TRX");
        db.execSQL("DROP TABLE IF EXISTS MSG");
        onCreate(db);
    }

    public HashMap<String, String> getLastSeqs() {
        HashMap<String, String> map = new HashMap<>();
        Cursor c = db.rawQuery("SELECT DISTINCT(BCID),SEQ FROM TRX WHERE 1 ORDER BY SEQ DESC;", null);
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getString(1));
        }
        c.close();
        return map;
    }

    /*

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

    @SuppressLint("Range")
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

    @SuppressLint("Range")
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
*/
}
