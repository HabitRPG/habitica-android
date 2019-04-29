package com.habitrpg.android.habitica.helpers;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.ui.AvatarView;

import net.glxn.qrgen.android.QRCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by keithholliday on 8/12/16.
 */
public class QrCodeManager {
    private static final String qrProfileUrl = "https://habitica.com/qr-code/user/";

    private final UserRepository userRepository;
    //@TODO: Allow users to set other content
    private String content;
    private String userId;
    private Context context;

    private ImageView qrCodeImageView;
    private Button qrCodeDownloadButton;
    private FrameLayout qrCodeWrapper;

    private String albumnName;
    private String fileName;
    private String saveMessage;

    public QrCodeManager(UserRepository userRepository, Context context) {
        this.context = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        String userId = prefs.getString(this.context.getString(R.string.SP_userID), "");

        this.albumnName = this.context.getString(R.string.qr_album_name);
        this.fileName = this.context.getString(R.string.qr_file_name);
        this.saveMessage = this.context.getString(R.string.qr_save_message);

        this.content = qrProfileUrl + userId;
        this.userId = userId;
        this.userRepository = userRepository;
    }

    public void setUpView(@Nullable LinearLayout qrLayout) {
        if (qrLayout == null) {
            return;
        }
        this.qrCodeImageView = qrLayout.findViewById(R.id.QRImageView);
        this.qrCodeDownloadButton = qrLayout.findViewById(R.id.QRDownloadButton);
        AvatarView avatarView = qrLayout.findViewById(R.id.avatarView);
        avatarView.configureView(false, false, false);
        this.qrCodeWrapper = qrLayout.findViewById(R.id.qrCodeWrapper);

        if (userRepository != null) {
            userRepository.getUser(userId).firstElement().subscribe(avatarView::setAvatar, RxErrorHandler.handleEmptyError());
        }

        this.displayQrCode();
        this.setDownloadQr();
    }

    private void displayQrCode() {
        if (qrCodeImageView == null) {
            return;
        }

        int qrCodeSize = (int) dipToPixels(400.0f);

        Bitmap myBitmap = QRCode.from(this.content)
                .withErrorCorrection(ErrorCorrectionLevel.H)
                .withColor(0xFF432874, 0xFFFFFFFF)
                .withSize(qrCodeSize, qrCodeSize)
                .bitmap();
        qrCodeImageView.setImageBitmap(myBitmap);
    }

    private float dipToPixels(float dipValue) {
        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    private void setDownloadQr() {
        if (qrCodeDownloadButton == null) {
            return;
        }

        qrCodeDownloadButton.setOnClickListener(view -> {
            File dir = getAlbumStorageDir(context, albumnName);
            dir.mkdirs();

            File pathToQRCode = new File(dir, fileName);
            try {
                pathToQRCode.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileOutputStream outputStream = new FileOutputStream(pathToQRCode);
                qrCodeWrapper.setDrawingCacheEnabled(true);
                Bitmap b = qrCodeWrapper.getDrawingCache();
                b.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);

                outputStream.close();

                Toast.makeText(context, saveMessage + pathToQRCode.getPath(),
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
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

    public void showDialogue() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.qr_dialogue);
        dialog.setTitle(R.string.qr_dialogue_title);

        LinearLayout qrLayout = dialog.findViewById(R.id.qrLayout);
        this.setUpView(qrLayout);
        Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);

        dialogButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
