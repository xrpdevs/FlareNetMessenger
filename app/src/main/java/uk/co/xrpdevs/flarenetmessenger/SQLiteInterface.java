package uk.co.xrpdevs.flarenetmessenger;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteInterface {
    myDbHelper myhelper;

    public SQLiteInterface(Context context) {
        myhelper = new myDbHelper(context);
    }

 /*   public long insertData(String name, String pass)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.NAME, name);
        contentValues.put(myDbHelper.MyPASSWORD, pass);
        return dbb.insert(myDbHelper.TABLE_NAME, null , contentValues);
    }*/

 /*   public String getData()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] columns = {myDbHelper.UID,myDbHelper.NAME,myDbHelper.MyPASSWORD};
        StringBuffer buffer;
        try (Cursor cursor = db.query(myDbHelper.TABLE_NAME, columns, null, null, null, null, null)) {
            buffer = new StringBuffer();
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int cid = cursor.getInt(cursor.getColumnIndex(myDbHelper.UID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(myDbHelper.NAME));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(myDbHelper.MyPASSWORD));
                buffer.append(cid + "   " + name + "   " + password + " \n");
            }
        }
        return buffer.toString();
    }*/

  /*  public  int delete(String uname)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs ={uname};

        return db.delete(myDbHelper.TABLE_NAME ,myDbHelper.NAME+" = ?",whereArgs);
    }*/

/*    public int updateName(String oldName , String newName)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.NAME,newName);
        String[] whereArgs= {oldName};
        return db.update(myDbHelper.TABLE_NAME,contentValues, myDbHelper.NAME+" = ?",whereArgs );
    }*/

    static class myDbHelper extends SQLiteOpenHelper {
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
                " LEDGER_LASTSEEN BIGINT, LEDGER_BALANCE BIGINT);";
        private static final String CREATE_BLK = "CREATE TABLE BLK ( id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " NAME VARCHAR(64), TOKNAME VARCHAR(20), SYMBOL VARCHAR(20), RPC TEXT, TYPE VARCHAR(10)," +
                " CHAINID VARCHAR(20), ICON VARCHAR(20), TESTNET BOOLEAN);";
        private static final String CREATE_TOK = "CREATE TABLE TOK (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME VARCHAR(64), TOKNAME VARCHAR(20), BCID BIGINT, CONTRACT VARCHAR(255), TYPE BIGINT, " +
                "PRECISION INTEGER);";

        //  private static final String CREATE_TRX =
        //  private static final String CREATE_MSG =

        private static final String DROP_TABLES = "DROP TABLE IF EXISTS MSG; DROP TABLE IF EXISTS TRX;";
        private final Context context;

        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context = context;
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_WAL + CREATE_TOK + CREATE_BLK);
            } catch (Exception e) {
                Log.d(context.toString(), "" + e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Log.d(context.toString(), "OnUpgrade");
                db.execSQL(DROP_TABLES);
                onCreate(db);
            } catch (Exception e) {
                Log.d(context.toString(), "" + e);
            }
        }
    }
}

