package uk.co.xrpdevs.flarenetmessenger;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class Zipper
{
    Context context;
    private String password;
    private SharedPreferences prefs;
    private static final String EXTENSION = "zip";

    public Zipper(String password, Context context)
    {
        this.password = password;
        this.context = context;
        this.prefs = context.getSharedPreferences("fnm",0);
    }

    public void pack(String filePath) throws ZipException
    {
        ZipParameters zipParameters = new ZipParameters();
        //zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        //zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
        zipParameters.setEncryptFiles(true);
        zipParameters.setSourceExternalStream(true);
        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        zipParameters.setPassword(password);

        String fileName = "wallets.zip";

        try{
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri imageUri = resolver.insert(MediaStore.Downloads.getContentUri("external"), contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
            }


            myLog("Zipper", "fos:"+fos.toString());

            JSONArray wallets = new JSONArray();

            int wC = prefs.getInt("walletCount", 0);

            for(int i = 0 ; i< wC; i++){
                wallets.put(prefs.getString("wallet"+String.valueOf(i), ""));
            }

          //  byte[] walletBytes = wallets.toString().getBytes();

            myLog("Zipper", "wallets: "+wallets.toString());

            ZipOutputStream zout = new ZipOutputStream(fos, new ZipModel() );
            String[] filenames = new String[]{"1.txt"};
            for (int i = 0; i < filenames.length; i++) {
                zipParameters.setFileNameInZip(filenames[0]);
                zout.putNextEntry(null, zipParameters);
                zout.write(wallets.toString().getBytes());//data waiting for compressed...
                zout.closeEntry();
            }
            zout.finish();
            zout.close();

            myLog("zipBytes", "bytes: "+new String(wallets.toString().getBytes(), StandardCharsets.UTF_8));

            Objects.requireNonNull(fos).close();
        }catch (IOException e) {
            // Log Message
        }
    }

    public void unpack(String sourceZipFilePath, String extractedZipFilePath) throws ZipException
    {
        ZipFile zipFile = new ZipFile(sourceZipFilePath + "." + EXTENSION);

        if (zipFile.isEncrypted())
        {
            zipFile.setPassword(password);
        }

        zipFile.extractAll(extractedZipFilePath);
    }
}