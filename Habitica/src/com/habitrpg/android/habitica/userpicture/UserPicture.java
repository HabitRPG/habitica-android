package com.habitrpg.android.habitica.userpicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class UserPicture {

    static Integer width = 140;
    static Integer height = 147;

    private HabitRPGUser user;
    private ImageView imageView;
    private Context context;
    public int numOfTasks = 0;

    private boolean hasBackground, hasMount, hasPet;

    List layers = new ArrayList();

    public UserPicture(HabitRPGUser user, Context context) {
        this.user = user;
        this.context = context;
    }

    public void allTasksComplete(){

        if(this.numOfTasks == 0){
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;

            Bitmap res = Bitmap.createBitmap(140, 147, Bitmap.Config.ARGB_8888);
            Canvas myCanvas = new Canvas(res);
            Integer layerNumber = 0;
            for (Object layer : this.layers) {
                if (layer.getClass() == Bitmap.class) {
                    Bitmap layerBitmap = (Bitmap) layer;
                    this.modifyCanvas(layerBitmap, myCanvas, layerNumber);
                }
                layerNumber++;
            }
            this.imageView.setImageBitmap(res);
        }

    }

    public void setPictureOn(ImageView imageView) {
        this.imageView = imageView;
        List<String> layerNames = this.user.getAvatarLayerNames();

        if (this.user.getItems().getCurrentMount() != null) {
            layerNames.add(0, "Mount_Body_" + this.user.getItems().getCurrentMount());
            layerNames.add("Mount_Head_" + this.user.getItems().getCurrentMount());
            this.hasMount = true;
        }

        if (this.user.getItems().getCurrentPet() != null) {
            layerNames.add("Pet-" + this.user.getItems().getCurrentPet());
            this.hasPet = true;
        }

        if (this.user.getPreferences().getBackground() != null) {
            layerNames.add(0, "background_" + this.user.getPreferences().getBackground());
            this.hasBackground = true;
        }

        Integer layerNumber = 0;
        this.numOfTasks = layerNames.size();
        for (String layer : layerNames) {
            layers.add(0);
            SpriteTarget target = new SpriteTarget(layerNumber);
            Picasso.with(this.context).load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/"+ layer +".png").into(target);
            layerNumber = layerNumber + 1;
        }
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

        public SpriteTarget(Integer layerNumber) {
            this.layerNumber = layerNumber;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            layers.set(this.layerNumber, bitmap);
            numOfTasks--;
            allTasksComplete();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            numOfTasks--;
            allTasksComplete();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

}