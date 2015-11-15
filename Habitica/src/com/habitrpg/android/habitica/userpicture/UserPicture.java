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
    private Context context;
    private AtomicInteger numOfTasks = new AtomicInteger(0);

    private boolean hasBackground, hasMount, hasPet;

    private String currentCacheFileName;

    List layers = new ArrayList();

    public UserPicture(HabitRPGUser user, Context context) {
        this.user = user;
        this.context = context;
        this.hasBackground = true;
        this.hasPet = true;
        this.hasMount = true;
    }

    public UserPicture(HabitRPGUser user, Context context, boolean hasBackground, boolean hasPet, boolean hasMount) {
        this.user = user;
        this.context = context;
        this.hasBackground = hasBackground;
        this.hasPet = hasPet;
        this.hasMount = hasMount;
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
            if (!this.hasBackground && !this.hasPet) {
                res = Bitmap.createBitmap(res, 25, 18, compactWidth, compactHeight);
            }
            BitmapUtils.saveToFile(currentCacheFileName, res);
            this.imageView.setImageBitmap(res);
        }
    }

    public void setPictureOn(ImageView imageView) {
        this.imageView = imageView;
        List<String> layerNames = this.user.getAvatarLayerNames();

        String mountName = this.user.getItems().getCurrentMount();

        if (mountName != null && !mountName.isEmpty() && hasMount) {
            layerNames.add(0, "Mount_Body_" + mountName);
            layerNames.add("Mount_Head_" + mountName);
        }

        String petName = this.user.getItems().getCurrentPet();

        if (petName != null && !petName.isEmpty() && hasPet) {
            layerNames.add("Pet-" + petName);
            this.hasPet = true;
        }

        String backgroundName = this.user.getPreferences().getBackground();

        if (backgroundName != null && !backgroundName.isEmpty() && hasBackground) {
            layerNames.add(0, "background_" + backgroundName);
            this.hasBackground = true;
        }


        // get layer hash value
        String fullLayerString = "";

        for(String l : layerNames){
            fullLayerString = fullLayerString.concat(l);
        }

        String layersHash = generateHashCode(fullLayerString);
        currentCacheFileName = layersHash.concat(".png");

        // does it already exist?
        Bitmap cache = BitmapUtils.loadFromFile(currentCacheFileName);

        // yes => load image to bitmap
        if(cache != null){
            imageView.setImageBitmap(cache);
            return;
        }

        // no => generate it
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

        if (this.hasMount && !((this.hasBackground && layerNumber == 1) ||
                                (!this.hasBackground && layerNumber == 0) ||
                                (this.hasPet && layerNumber == this.layers.size()-2) ||
                                (!this.hasPet && layerNumber == this.layers.size()-1)
        )) {
            yOffset = 0;
        }

        if (this.hasPet && layerNumber == this.layers.size()-1) {
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




