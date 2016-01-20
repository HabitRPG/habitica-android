package com.habitrpg.android.habitica.userpicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserPicture {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 147;
    private static final int COMPACT_WIDTH = 114;
    private static final int COMPACT_HEIGHT = 114;

    private HabitRPGUser user;
    private ImageView imageView;
    private UserPictureRunnable runnable;
    private Context context;
    private AtomicInteger numOfTasks = new AtomicInteger(0);

    private boolean hasBackground, hasPetMount;
    private boolean userHasBackground, userHasPet, userHasMount;

    private String currentCacheFileName;

    final List<Bitmap> layers = new ArrayList<>();
    final List<SpriteTarget> targets = new ArrayList<>();

    public UserPicture(Context context) {
        this.context = context;
        this.hasBackground = true;
        this.hasPetMount = true;
    }

    public UserPicture(Context context, boolean hasBackground, boolean hasPetMount) {
        this.context = context;
        this.hasBackground = hasBackground;
        this.hasPetMount = hasPetMount;
    }

    public void setUser(HabitRPGUser user) {
        this.user = user;
    }

    public void removeTask() {
        this.numOfTasks.decrementAndGet();
    }

    public void allTasksComplete() {
        if (this.numOfTasks.get() == 0) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;

            Bitmap res = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas myCanvas = new Canvas(res);
            Integer layerNumber = 0;
            for (Bitmap layerBitmap : this.layers) {
                if (layerBitmap != null) {
                    modifyCanvas(layerBitmap, myCanvas, layerNumber);
                }
                layerNumber++;
            }
            if (!this.hasPetMount) {
                res = Bitmap.createBitmap(res, 25, 0, COMPACT_WIDTH, COMPACT_HEIGHT);
            }
            BitmapUtils.saveToFile(currentCacheFileName, res);
            if (this.imageView != null) {
                final Bitmap image = res;
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(image);
                    }
                });
            } else {
                this.runnable.run(res);
            }
        }
    }

    public void setPictureOn(final ImageView imageView) {
        if (this.user == null) {
            return;
        }
        clearRunning();
        this.imageView = imageView;
        List<String> layerNames = UserPicture.this.getLayerNames();

        final Bitmap cache = UserPicture.this.getCachedImage(layerNames);

        // yes => load image to bitmap
        if (cache != null) {
            imageView.setImageBitmap(cache);
            return;
        }

        // no => generate it
        generateImage(layerNames);
    }

    public void setPictureWithRunnable(UserPictureRunnable runnable) {
        if (this.user == null) {
            return;
        }
        this.clearRunning();
        this.runnable = runnable;
        List<String> layerNames = this.getLayerNames();

        Bitmap cache = this.getCachedImage(layerNames);

        // yes => load image to bitmap
        if(cache != null){
            runnable.run(cache);
            return;
        }

        // no => generate it
        generateImage(layerNames);
    }

    private void clearRunning() {
        for (SpriteTarget target : this.targets) {
            Picasso.with(this.context).cancelRequest(target);
        }
        this.targets.clear();
        this.numOfTasks.set(0);
        this.layers.clear();
        this.imageView = null;
        this.runnable = null;
    }

    private List<String> getLayerNames() {
        List<String> layerNames = this.user.getAvatarLayerNames();

        String mountName = this.user.getItems().getCurrentMount();

        if (mountName != null && !mountName.isEmpty() && hasPetMount) {
            layerNames.add(0, "Mount_Body_" + mountName);
            layerNames.add("Mount_Head_" + mountName);
            this.userHasMount = true;
        } else {
            this.userHasMount = false;
        }

        String petName = this.user.getItems().getCurrentPet();

        if (petName != null && !petName.isEmpty() && hasPetMount) {
            layerNames.add("Pet-" + petName);
            this.userHasPet = true;
        } else {
            this.userHasPet = false;
        }

        String backgroundName = this.user.getPreferences().getBackground();

        if (backgroundName != null && !backgroundName.isEmpty() && hasBackground) {
            layerNames.add(0, "background_" + backgroundName);
            this.userHasBackground = true;
        } else {
            this.userHasBackground = false;
        }
        return layerNames;
    }

    private Bitmap getCachedImage(List<String> layerNames) {
        // get layer hash value
        String fullLayerString = String.valueOf(hasBackground).concat(String.valueOf(hasPetMount));

        for (String l : layerNames) {
            fullLayerString = fullLayerString.concat(l);
        }

        String layersHash = generateHashCode(fullLayerString);
        currentCacheFileName = layersHash.concat(".png");

        // does it already exist?
        return BitmapUtils.loadFromFile(currentCacheFileName);
    }

    private void generateImage(List<String> layerNames) {
        Integer layerNumber = 0;
        this.numOfTasks.set(layerNames.size());
        this.layers.clear();
        Picasso picasso = Picasso.with(this.context);
        for (String layer : layerNames) {
            this.layers.add(null);
            SpriteTarget target = new SpriteTarget(layerNumber, layer);
            this.targets.add(target);
            picasso.load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + layer + ".png").into(target);
            layerNumber = layerNumber + 1;
        }
    }

    private static String generateHashCode(String value) {

        MessageDigest md = null;
        byte[] digest = new byte[0];
        try {
            md = MessageDigest.getInstance("MD5");

            md.update(value.getBytes());
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return bytesToHex(digest);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void modifyCanvas(Bitmap img, Canvas canvas, Integer layerNumber) {
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        Integer xOffset = 25;
        Integer yOffset = 18;

        if (this.hasBackground && this.userHasBackground && layerNumber == 0) {
            xOffset = 0;
            yOffset = 0;
        }

        if (this.hasPetMount) {
            if (this.userHasMount) {
                if (((this.userHasBackground && layerNumber > 1) ||
                        (!this.userHasBackground && layerNumber > 0)) &&
                        ((this.userHasPet && layerNumber < (this.layers.size()-2)) ||
                        (!this.userHasPet && layerNumber < this.layers.size()-1))) {
                    yOffset = 0;
                }
            }
        }

        if (this.hasPetMount && this.userHasPet && layerNumber == this.layers.size()-1) {
            xOffset = 0;
            yOffset = 43;
        }

        canvas.drawBitmap(img, new Rect(0, 0, img.getWidth(), img.getHeight()),
                new Rect(xOffset, yOffset, img.getWidth() + xOffset, img.getHeight() + yOffset), paint);
    }

    private class SpriteTarget implements Target {

        private Integer layerNumber;
        private String layer;

        public SpriteTarget(Integer layerNumber, String layer) {
            this.layerNumber = layerNumber;
            this.layer = layer;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            UserPicture.this.layers.set(this.layerNumber, bitmap);
            UserPicture.this.removeTask();
            UserPicture.this.allTasksComplete();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.w("SpriteTarget", layer + " not on S3");

            UserPicture.this.removeTask();
            UserPicture.this.allTasksComplete();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

}




