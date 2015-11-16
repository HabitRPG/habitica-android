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

    static Integer width = 140;
    static Integer height = 147;
    static Integer compactWidth = 90;
    static Integer compactHeight = 90;

    private HabitRPGUser user;
    private ImageView imageView;
    private UserPictureRunnable runnable;
    private Context context;
    private AtomicInteger numOfTasks = new AtomicInteger(0);

    private boolean hasBackground, hasPetMount;

    private String currentCacheFileName;

    List layers = new ArrayList();

    public UserPicture(HabitRPGUser user, Context context) {
        this.user = user;
        this.context = context;
        this.hasBackground = true;
        this.hasPetMount = true;
    }

    public UserPicture(HabitRPGUser user, Context context, boolean hasBackground, boolean hasPetMount) {
        this.user = user;
        this.context = context;
        this.hasBackground = hasBackground;
        this.hasPetMount = hasPetMount;
    }

    public void removeTask(){
        numOfTasks.decrementAndGet();
    }

    public void allTasksComplete(){
        if(this.numOfTasks.get() == 0){
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;

            Bitmap res = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas myCanvas = new Canvas(res);
            Integer layerNumber = 0;
            for (Object layer : this.layers) {
                if (layer.getClass() == Bitmap.class) {
                    Bitmap layerBitmap = (Bitmap) layer;
                    this.modifyCanvas(layerBitmap, myCanvas, layerNumber);
                }
                layerNumber++;
            }
            if (!this.hasPetMount) {
                res = Bitmap.createBitmap(res, 25, 18, compactWidth, compactHeight);
            }
            BitmapUtils.saveToFile(currentCacheFileName, res);
            if (this.imageView != null) {
                this.imageView.setImageBitmap(res);
            } else {
                this.runnable.run(res);
            }
        }
    }

    public void setPictureOn(ImageView imageView) {
        this.imageView = imageView;

        List<String> layerNames = this.getLayerNames();

        Bitmap cache = this.getCachedImage(layerNames);

        // yes => load image to bitmap
        if(cache != null){
            imageView.setImageBitmap(cache);
            return;
        }

        // no => generate it
        generateImage(layerNames);
    }

    public void setPictureWithRunnable(UserPictureRunnable runnable) {
        this.runnable = runnable;
        List<String> layerNames = this.getLayerNames();

        Bitmap cache = this.getCachedImage(layerNames);

        // yes => load image to bitmap
        if(cache != null){
            runnable.run(cache);
            return;
        }

        generateImage(layerNames);
    }

    private List<String> getLayerNames() {
        List<String> layerNames = this.user.getAvatarLayerNames();

        String mountName = this.user.getItems().getCurrentMount();

        if (mountName != null && !mountName.isEmpty() && hasPetMount) {
            layerNames.add(0, "Mount_Body_" + mountName);
            layerNames.add("Mount_Head_" + mountName);
        }

        String petName = this.user.getItems().getCurrentPet();

        if (petName != null && !petName.isEmpty() && hasPetMount) {
            layerNames.add("Pet-" + petName);
            this.hasPetMount = true;
        }

        String backgroundName = this.user.getPreferences().getBackground();

        if (backgroundName != null && !backgroundName.isEmpty() && hasBackground) {
            layerNames.add(0, "background_" + backgroundName);
            this.hasBackground = true;
        }

        return layerNames;
    }

    private Bitmap getCachedImage(List<String> layerNames) {
        // get layer hash value
        String fullLayerString = "";

        for(String l : layerNames){
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
        for (String layer : layerNames) {
            layers.add(0);
            SpriteTarget target = new SpriteTarget(layerNumber, layer);
            Picasso.with(this.context).load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/"+ layer +".png").into(target);
            layerNumber = layerNumber + 1;
        }
    }

    private static String generateHashCode(String value){

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
        for ( int j = 0; j < bytes.length; j++ ) {
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

        if (this.hasBackground && layerNumber == 0) {
            xOffset = 0;
            yOffset = 0;
        }

        if (this.hasPetMount && !((this.hasBackground && layerNumber == 1) ||
                                (!this.hasBackground && layerNumber == 0) ||
                                (this.hasPetMount && layerNumber == this.layers.size()-2) ||
                                (!this.hasPetMount && layerNumber == this.layers.size()-1)
        )) {
            yOffset = 0;
        }

        if (this.hasPetMount && layerNumber == this.layers.size()-1) {
            xOffset = 0;
            yOffset = 43;
        }

        canvas.drawBitmap(img, new Rect(0, 0, img.getWidth(), img.getHeight()),
                new Rect(xOffset, yOffset, img.getWidth()+xOffset, img.getHeight()+yOffset), paint);
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
            layers.set(this.layerNumber, bitmap);
            removeTask();
            allTasksComplete();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.w("SpriteTarget", layer + " not on S3");

            removeTask();
            allTasksComplete();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

}




