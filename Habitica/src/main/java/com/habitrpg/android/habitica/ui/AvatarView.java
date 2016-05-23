package com.habitrpg.android.habitica.ui;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.facebook.imagepipeline.image.ImageInfo;
import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AvatarView extends View {
    public static final String IMAGE_URI_ROOT = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/";
    private static final String TAG = "AvatarView";
    private static final Map<String, String> sFilenameMap;
    private static final Rect sFullHeroRect = new Rect(0, 0, 140, 147);
    private static final Rect sCompactHeroRect = new Rect(0, 0, 114, 114);
    private static final Rect sHeroOnlyRect = new Rect(0, 0, 90, 90);

    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("head_special_1", "ContributorOnly-Equip-CrystalHelmet.gif");
        tempMap.put("armor_special_1", "ContributorOnly-Equip-CrystalArmor.gif");
        tempMap.put("weapon_special_critical", "weapon_special_critical.gif");
        tempMap.put("Pet-Wolf-Cerberus", "Pet-Wolf-Cerberus.gif");
        sFilenameMap = Collections.unmodifiableMap(tempMap);
    }

    private boolean mShowBackground = true;
    private boolean mShowMount = true;
    private boolean mShowPet = true;
    private boolean mHasBackground;
    private boolean mHasMount;
    private boolean mHasPet;
    private boolean mIsOrphan;
    private MultiDraweeHolder<GenericDraweeHierarchy> mMultiDraweeHolder = new MultiDraweeHolder<>();
    private HabitRPGUser mUser;
    private RectF mAvatarRectF;
    private Matrix mMatrix = new Matrix();
    private AtomicInteger mNumberLayersInProcess = new AtomicInteger(0);
    private Consumer<Bitmap> mAvatarImageConsumer;
    private Bitmap mAvatarBitmap;
    private Canvas mAvatarCanvas;

    public AvatarView(Context context) {
        super(context);
        init(null, 0);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public AvatarView(Context context, boolean showBackground, boolean showMount, boolean showPet) {
        super(context);

        mShowBackground = showBackground;
        mShowMount = showMount;
        mShowPet = showPet;
        mIsOrphan = true;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AvatarView, defStyle, 0);

        try {
            mShowBackground = a.getBoolean(R.styleable.AvatarView_showBackground, true);
            mShowMount = a.getBoolean(R.styleable.AvatarView_showMount, true);
            mShowPet = a.getBoolean(R.styleable.AvatarView_showPet, true);
        } finally {
            a.recycle();
        }
    }

    private void showLayers(@NonNull Map<LayerType, String> layerMap) {
        if (mMultiDraweeHolder.size() > 0) return;
        int i = 0;

        mNumberLayersInProcess.set(layerMap.size());

        for (Map.Entry<LayerType, String> entry : layerMap.entrySet()) {
            final LayerType layerKey = entry.getKey();
            final String layerName = entry.getValue();
            final int layerNumber = i++;

            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                    .setFadeDuration(0)
                    .build();

            DraweeHolder<GenericDraweeHierarchy> draweeHolder = DraweeHolder.create(hierarchy, getContext());
            draweeHolder.getTopLevelDrawable().setCallback(this);
            mMultiDraweeHolder.add(draweeHolder);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(IMAGE_URI_ROOT + getFileName(layerName))
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFinalImageSet(
                                String id,
                                ImageInfo imageInfo,
                                Animatable anim) {
                            if (imageInfo != null) {
                                mMultiDraweeHolder.get(layerNumber).getTopLevelDrawable().setBounds(getLayerBounds(layerKey, layerName, imageInfo));
                                onLayerComplete();
                            }
                        }

                        @Override
                        public void onFailure(String id, Throwable throwable) {
                            Log.e(TAG, "Error loading layer: " + layerName, throwable);
                            onLayerComplete();
                        }
                    })
                    .setAutoPlayAnimations(!mIsOrphan)
                    .build();
            draweeHolder.setController(controller);
        }

        if (mIsOrphan) mMultiDraweeHolder.onAttach();
    }

    private Map<LayerType, String> getLayerMap() {
        assert mUser != null;
        return getLayerMap(mUser, true);
    }

    private Map<LayerType, String> getLayerMap(@NonNull HabitRPGUser user, boolean resetHasAttributes) {
        EnumMap<LayerType, String> layerMap = user.getAvatarLayerMap();

        if (resetHasAttributes) mHasBackground = mHasMount = mHasPet = false;

        String mountName = user.getItems().getCurrentMount();
        if (mShowMount && !TextUtils.isEmpty(mountName)) {
            layerMap.put(LayerType.MOUNT_BODY, "Mount_Body_" + mountName);
            layerMap.put(LayerType.MOUNT_HEAD, "Mount_Head_" + mountName);
            if (resetHasAttributes) mHasMount = true;
        }

        String petName = user.getItems().getCurrentPet();
        if (mShowPet && !TextUtils.isEmpty(petName)) {
            layerMap.put(LayerType.PET, "Pet-" + petName);
            if (resetHasAttributes) mHasPet = true;
        }

        String backgroundName = user.getPreferences().getBackground();
        if (mShowBackground && !TextUtils.isEmpty(backgroundName)) {
            layerMap.put(LayerType.BACKGROUND, "background_" + backgroundName);
            if (resetHasAttributes) mHasBackground = true;
        }
        return layerMap;
    }

    private Rect getLayerBounds(@NonNull LayerType layerType, @NonNull String layerName, @NonNull ImageInfo layerImageInfo) {
        PointF offset = null;
        Rect bounds = new Rect(0, 0, layerImageInfo.getWidth(), layerImageInfo.getHeight());
        RectF boundsF = new RectF(bounds);

        // lookup layer specific offset
        switch (layerName) {
            case "weapon_special_critical":
                if (mShowMount || mShowPet) {
                    // full hero box
                    if (mHasMount) {
                        offset = new PointF(13.0f, 12.0f);
                    } else if (mHasPet) {
                        offset = new PointF(13.0f, 24.5f + 12.0f);
                    } else {
                        offset = new PointF(13.0f, 28.0f + 12.0f);
                    }
                } else if (mShowBackground) {
                    // compact hero box
                    offset = new PointF(-12.0f, 18.0f + 12.0f);
                } else {
                    // hero only box
                    offset = new PointF(-12.0f, 12.0f);
                }
                break;
            default:
                break;
        }

        // otherwise lookup default layer type based offset
        if (offset == null) {
            switch (layerType) {
                case BACKGROUND:
                    if (!(mShowMount || mShowPet)) {
                        offset = new PointF(-25.0f, 0.0f); // compact hero box
                    }
                    break;
                case MOUNT_BODY:
                case MOUNT_HEAD:
                    offset = new PointF(25.0f, 18.0f); // full hero box
                    break;
                case CHAIR:
                case BACK:
                case SKIN:
                case SHIRT:
                case ARMOR:
                case BODY:
                case HEAD_0:
                case HAIR_BASE:
                case HAIR_BANGS:
                case HAIR_MUSTACHE:
                case HAIR_BEARD:
                case EYEWEAR:
                case HEAD:
                case HEAD_ACCESSORY:
                case HAIR_FLOWER:
                case SHIELD:
                case WEAPON:
                case ZZZ:
                    if (mShowMount || mShowPet) {
                        // full hero box
                        if (mHasMount) {
                            offset = new PointF(25.0f, 0);
                        } else if (mHasPet) {
                            offset = new PointF(25.0f, 24.5f);
                        } else {
                            offset = new PointF(25.0f, 28.0f);
                        }
                    } else if (mShowBackground) {
                        // compact hero box
                        offset = new PointF(0.0f, 18.0f);
                    }
                    break;
                case PET:
                    offset = new PointF(0, sFullHeroRect.height() - layerImageInfo.getHeight());
                    break;
                default:
                    break;
            }
        }

        if (offset != null) {
            Matrix translateMatrix = new Matrix();
            translateMatrix.setTranslate(offset.x, offset.y);
            translateMatrix.mapRect(boundsF);
        }

        // resize bounds to fit and keep original aspect ratio
        mMatrix.mapRect(boundsF);
        boundsF.round(bounds);

        return bounds;
    }

    private String getFileName(@NonNull String imageName) {
        if (sFilenameMap.containsKey(imageName)) {
            return sFilenameMap.get(imageName);
        }

        return imageName + ".png";
    }

    private void onLayerComplete() {
        if (mNumberLayersInProcess.decrementAndGet() == 0) {
            if (mAvatarImageConsumer != null) {
                mAvatarImageConsumer.accept(getAvatarImage());
            }
        }
    }

    public void onAvatarImageReady(@NonNull Consumer<Bitmap> consumer) {
        mAvatarImageConsumer = consumer;
        if (mMultiDraweeHolder.size() > 0 && mNumberLayersInProcess.get() == 0) {
            mAvatarImageConsumer.accept(getAvatarImage());
        } else {
            initAvatarRectMatrix();
            showLayers(getLayerMap());
        }
    }

    public void setUser(@NonNull HabitRPGUser user) {
        HabitRPGUser oldUser = mUser;
        mUser = user;

        if (oldUser != null) {
            Map<LayerType, String> currentLayerMap = getLayerMap(oldUser, false);
            Map<LayerType, String> newLayerMap = getLayerMap(user, false);
            if (!currentLayerMap.equals(newLayerMap)) {
                mMultiDraweeHolder.clear();
                mNumberLayersInProcess.set(0);
            }
        }
        invalidate();
    }

    private Rect getOriginalRect() {
        return (mShowMount || mShowPet) ? sFullHeroRect : ((mShowBackground) ? sCompactHeroRect : sHeroOnlyRect);
    }

    private Bitmap getAvatarImage() {
        assert mUser != null;
        assert mAvatarRectF != null;
        Rect canvasRect = new Rect();
        mAvatarRectF.round(canvasRect);
        mAvatarBitmap = Bitmap.createBitmap(canvasRect.width(), canvasRect.height(), Bitmap.Config.ARGB_8888);
        mAvatarCanvas = new Canvas(mAvatarBitmap);
        draw(mAvatarCanvas);

        return mAvatarBitmap;
    }

    private void initAvatarRectMatrix() {
        if (mAvatarRectF == null) {
            Rect srcRect = getOriginalRect();

            if (mIsOrphan) {
                mAvatarRectF = new RectF(srcRect);

                // change scale to not be 1:1
                // a quick fix as fresco AnimatedDrawable/ScaleTypeDrawable
                // will not translate matrix properly
                mMatrix.setScale(1.2f, 1.2f);
                mMatrix.mapRect(mAvatarRectF);
            } else {
                // full hero box when showMount and showPet is enabled (140w * 147h)
                // compact hero box when only showBackground is enabled (114w * 114h)
                // hero only box when all show settings disabled (90w * 90h)
                mAvatarRectF = new RectF(0, 0, getWidth(), getHeight());
                mMatrix.setRectToRect(new RectF(srcRect), mAvatarRectF, Matrix.ScaleToFit.START); // TODO support other ScaleToFit
                mAvatarRectF = new RectF(srcRect);
                mMatrix.mapRect(mAvatarRectF);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initAvatarRectMatrix();

        // draw only when user is set
        if (mUser == null) return;

        // request image layers if not yet processed
        if (mMultiDraweeHolder.size() == 0) {
            showLayers(getLayerMap());
        }

        // manually call onAttach/onDetach if view is without parent as they will never be called otherwise
        if (mIsOrphan) mMultiDraweeHolder.onAttach();
        mMultiDraweeHolder.draw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMultiDraweeHolder.onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        mMultiDraweeHolder.onDetach();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMultiDraweeHolder.onAttach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        mMultiDraweeHolder.onAttach();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mMultiDraweeHolder.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return mMultiDraweeHolder.verifyDrawable(who) || super.verifyDrawable(who);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        invalidate();
        if (mAvatarCanvas != null) draw(mAvatarCanvas);
    }

    public enum LayerType {
        BACKGROUND(0),
        MOUNT_BODY(1),
        CHAIR(2),
        BACK(3),
        SKIN(4),
        SHIRT(5),
        ARMOR(6),
        BODY(7),
        HEAD_0(8),
        HAIR_BASE(9),
        HAIR_BANGS(10),
        HAIR_MUSTACHE(11),
        HAIR_BEARD(12),
        EYEWEAR(13),
        HEAD(14),
        HEAD_ACCESSORY(15),
        HAIR_FLOWER(16),
        SHIELD(17),
        WEAPON(18),
        MOUNT_HEAD(19),
        ZZZ(20),
        PET(21);

        final int order;

        LayerType(int order) {
            this.order = order;
        }
    }

    public interface Consumer<T> {
        void accept(T t);
    }
}
