package com.habitrpg.android.habitica.helpers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by phillip on 21.07.17.
 */

public class TiledBitmapPostProcessor extends BasePostprocessor {

    private final Resources resources;

    public TiledBitmapPostProcessor(Resources resources) {
        super();
        this.resources = resources;
    }

    @Override
    public void process(Bitmap bitmap) {
        BitmapDrawable TileMe = new BitmapDrawable(resources, bitmap);
        TileMe.setTileModeX(Shader.TileMode.REPEAT);
        TileMe.setTileModeY(Shader.TileMode.REPEAT);
        super.process(TileMe.getBitmap());
    }
}
