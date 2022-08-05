package com.yvtechnologies.pdfreader.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.format.Formatter;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class Utils {
    public static File RootDirectoryPDFReader = new File(Environment.getExternalStorageDirectory() + "/PDF Reader");

    public static void createFileFolder() {
        if (!RootDirectoryPDFReader.exists()) {
            RootDirectoryPDFReader.mkdirs();
        }
    }


    public static boolean isAppInstalled(Context context, String str) {
        try {
            context.getPackageManager().getApplicationInfo(str, 0);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static String getFileLastModifiedTime(File f) {
        Date lastModeified =new Date(f.lastModified());
        String stringDate = DateFormat.getDateInstance().format(lastModeified);
        return stringDate;
    }

    public static String getFileSize(File f,Context context){
        return Formatter.formatShortFileSize(context,f.length());
    }


}
