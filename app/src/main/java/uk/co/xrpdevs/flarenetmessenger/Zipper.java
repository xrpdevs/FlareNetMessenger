package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.io.ByteStreams;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

public class Zipper {
    Context context;
    private final String password;
    private final SharedPreferences prefs;
    private static final String EXTENSION = "zip";

    public Zipper(String password, Context context) {
        this.password = password;
        this.context = context;
        this.prefs = context.getSharedPreferences("fnm", 0);
    }

    public void pack(String filePath) {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        String fileName = "wallets.zip";

        try {
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri zipUri = resolver.insert(MediaStore.Downloads.getContentUri("external"), contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(zipUri));
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File zipfile = new File(imagesDir, fileName);
                fos = new FileOutputStream(zipfile);
            }


            myLog("Zipper", "fos:" + fos.toString());

            JSONArray wallets = new JSONArray();

            int wC = prefs.getInt("walletCount", 0);

            for (int i = 1; i < (wC + 1); i++) {

                String s1 = prefs.getString("wallet" + i, "");
                try {
                    JSONObject jsonObject = new JSONObject(s1);
                    wallets.put(jsonObject);
                } catch (JSONException err) {
                    Log.d("Error", err.toString());
                }

            }

            ZipOutputStream zout = new ZipOutputStream(fos, password.toCharArray());
            String[] filenames = new String[]{"wallets.json"};
            // todo: create QR of each key as PNG or PDF and add to zip
            for (int i = 0; i < filenames.length; i++) {
                zipParameters.setFileNameInZip(filenames[0]);
                zout.putNextEntry(zipParameters);
                zout.write(wallets.toString(4).getBytes());//data waiting for compressed...
                zout.closeEntry();
            }

            zout.close();

            Objects.requireNonNull(fos).close();
        } catch (IOException | JSONException e) {
            // Log Message
        }
    }

    public JSONArray extractWithZipInputStream(Uri fileName, String password) throws IOException, JSONException {

        InputStream fis;
        fis = context.getContentResolver().openInputStream(fileName);

        byte[] bytes = ByteStreams.toByteArray(fis);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ZipInputStream innerZip = new ZipInputStream(bis, password.toCharArray());

        JSONArray feck = null;

        LocalFileHeader zipEntry = null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(innerZip));
        while ((zipEntry = innerZip.getNextEntry()) != null) {
            String entryName = zipEntry.getFileName();
            System.out.println(entryName);
            if (!zipEntry.isDirectory()) {

                /**
                 *  todo: only use wallets.json from the archive.
                 *      future version will also export private keys
                 *      as images of QR codes with wallet name.
                 */

                String line;

                String walletData = "";

                while ((line = reader.readLine()) != null) {
                    walletData = walletData + line;
                }

                myLog("ZIPFile", walletData);

                feck = new JSONArray(walletData);
            }
        }

        reader.close();
        innerZip.close();

        return feck;

    }
}