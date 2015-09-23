package com.habitrpg.android.habitica.userpicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtils {
    public static String getSavePath() {
        String path;
        if (hasSDCard()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = Environment.getDownloadCacheDirectory().getAbsolutePath();
        }
        return path+"/HabiticaImageCache";
    }

    public static Bitmap loadFromFile(String filename) {
        try {
            filename = getSavePath() +"/"+ filename;

            File f = new File(filename);
            if (!f.exists()) { return null; }
            Bitmap tmp = BitmapFactory.decodeFile(filename);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveToFile(String filename,Bitmap bmp) {
        try {
            File myDir = new File(getSavePath());
            boolean res = myDir.mkdirs();

            filename = getSavePath() +"/"+ filename;

            FileOutputStream out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch(Exception e) {}
    }

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }
}
