package com.habitrpg.android.habitica.userpicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    public static String getSavePath() {
        String path;
        if (hasSDCard()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = Environment.getDownloadCacheDirectory().getAbsolutePath();
        }
        return path + "/HabiticaImageCache";
    }

    public static Bitmap loadFromFile(String filename) {
        try {
            filename = getSavePath() + "/" + filename;

            File f = new File(filename);
            if (!f.exists()) {
                return null;
            }
            return BitmapFactory.decodeFile(filename);
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveToFile(String filename, Bitmap bmp) {
        try {
            createNomedia();
            filename = getSavePath() + "/" + filename;

            FileOutputStream out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    public static File saveToShareableFile(String directory, String filename, Bitmap bmp) {
        try {
            filename = directory + "/" + filename;

            FileOutputStream out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return new File(filename);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    private static boolean createNomedia() {
        //Returns true if .nomedia was created/already existed and false if not
        try {
            File cacheDir = new File(getSavePath());
            cacheDir.mkdirs();
            File nomediaFile = new File(getSavePath() + "/.nomedia");
            if (!nomediaFile.isFile()) return nomediaFile.createNewFile();
        } catch (IOException ignored) {
        }
        return false;
    }
}
