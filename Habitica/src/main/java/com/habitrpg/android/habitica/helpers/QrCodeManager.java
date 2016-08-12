package com.habitrpg.android.habitica.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.habitrpg.android.habitica.R;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by keithholliday on 8/12/16.
 */
public class QrCodeManager {

    //@TODO: Allow users to set other content
    private String content;
    private Context context;

    private String albumnName;
    private String fileName;
    private String saveMessage;

    public QrCodeManager(Context context) {
        this.context = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        String userId = prefs.getString(this.context.getString(R.string.SP_userID), "");

        this.albumnName = this.context.getString(R.string.qr_album_name);
        this.fileName = this.context.getString(R.string.qr_file_name);
        this.saveMessage = this.context.getString(R.string.qr_save_message);

        this.content = userId;
    }

    public void displayQrCode(ImageView qrImageView) {
        if (qrImageView == null) {
            return;
        }

        Bitmap myBitmap = QRCode.from(this.content).bitmap();
        qrImageView.setImageBitmap(myBitmap);
    }

    public void downloadQr(Button qRDownloadButton) {
        if (qRDownloadButton == null) {
            return;
        }

        qRDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dir = getAlbumStorageDir(context, albumnName);
                dir.mkdirs();

                File pathToQRCode = new File(dir, fileName);
                try {
                    pathToQRCode.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream qrStream = QRCode.from(content).to(ImageType.JPG).stream();

                try {
                    FileOutputStream outputStream = new FileOutputStream(pathToQRCode);
                    qrStream.writeTo(outputStream);
                    outputStream.close();

                    Toast.makeText(context, saveMessage + pathToQRCode.getPath(),
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private File getAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName);
        file.mkdirs();
        return file;
    }
}
