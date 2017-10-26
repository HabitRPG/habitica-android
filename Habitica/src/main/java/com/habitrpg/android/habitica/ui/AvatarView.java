package com.habitrpg.android.habitica.ui;

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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.facebook.imagepipeline.image.ImageInfo;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.Avatar;
import com.habitrpg.android.habitica.models.AvatarPreferences;
import com.habitrpg.android.habitica.models.user.Buffs;
import com.habitrpg.android.habitica.models.user.Hair;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Preferences;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AvatarView extends View {
    public static final String IMAGE_URI_ROOT = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/";
    private static final String TAG = "AvatarView";
    private static final Map<String, String> FILENAME_MAP;
    private static final Rect FULL_HERO_RECT = new Rect(0, 0, 140, 147);
    private static final Rect COMPACT_HERO_RECT = new Rect(0, 0, 114, 114);
    private static final Rect HERO_ONLY_RECT = new Rect(0, 0, 90, 90);

    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("head_special_1", "ContributorOnly-Equip-CrystalHelmet.gif");
        tempMap.put("armor_special_1", "ContributorOnly-Equip-CrystalArmor.gif");
        tempMap.put("weapon_special_critical", "weapon_special_critical.gif");
        tempMap.put("Pet-Wolf-Cerberus", "Pet-Wolf-Cerberus.gif");
        FILENAME_MAP = Collections.unmodifiableMap(tempMap);
    }

    private boolean showBackground = true;
    private boolean showMount = true;
    private boolean showPet = true;
    private boolean showSleeping = true;
    private boolean hasBackground;
    private boolean hasMount;
    private boolean hasPet;
    private boolean isOrphan;
    private MultiDraweeHolder<GenericDraweeHierarchy> multiDraweeHolder = new MultiDraweeHolder<>();
    private Avatar avatar;
    private RectF avatarRectF;
    private Matrix matrix = new Matrix();
    private AtomicInteger numberLayersInProcess = new AtomicInteger(0);
    private Consumer<Bitmap> avatarImageConsumer;
    private Bitmap avatarBitmap;
    private Canvas avatarCanvas;
    private Map<LayerType, String> currentLayers;

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

        this.showBackground = showBackground;
        this.showMount = showMount;
        this.showPet = showPet;
        isOrphan = true;
    }

    public void configureView(boolean showBackground, boolean showMount, boolean showPet) {
        this.showBackground = showBackground;
        this.showMount = showMount;
        this.showPet = showPet;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AvatarView, defStyle, 0);

        try {
            showBackground = a.getBoolean(R.styleable.AvatarView_showBackground, true);
            showMount = a.getBoolean(R.styleable.AvatarView_showMount, true);
            showPet = a.getBoolean(R.styleable.AvatarView_showPet, true);
            showSleeping = a.getBoolean(R.styleable.AvatarView_showSleeping, true);
        } finally {
            a.recycle();
        }
    }

    private void showLayers(@NonNull Map<LayerType, String> layerMap) {
        if (multiDraweeHolder.size() > 0) return;
        int i = 0;

        currentLayers = layerMap;

        numberLayersInProcess.set(layerMap.size());

        for (Map.Entry<LayerType, String> entry : layerMap.entrySet()) {
            final LayerType layerKey = entry.getKey();
            final String layerName = entry.getValue();
            final int layerNumber = i++;

            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                    .setFadeDuration(0)
                    .build();

            DraweeHolder<GenericDraweeHierarchy> draweeHolder = DraweeHolder.create(hierarchy, getContext());
            draweeHolder.getTopLevelDrawable().setCallback(this);
            multiDraweeHolder.add(draweeHolder);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(IMAGE_URI_ROOT + getFileName(layerName))
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFinalImageSet(
                                String id,
                                ImageInfo imageInfo,
                                Animatable anim) {
                            if (imageInfo != null) {
                                if (multiDraweeHolder.size() > layerNumber) {
                                    multiDraweeHolder.get(layerNumber).getTopLevelDrawable().setBounds(getLayerBounds(layerKey, layerName, imageInfo));
                                }
                                onLayerComplete();
                            }
                        }

                        @Override
                        public void onFailure(String id, Throwable throwable) {
                            Log.e(TAG, "Error loading layer: " + layerName, throwable);
                            onLayerComplete();
                        }
                    })
                    .setAutoPlayAnimations(!isOrphan)
                    .build();
            draweeHolder.setController(controller);
        }

        if (isOrphan) multiDraweeHolder.onAttach();
    }

    private Map<LayerType, String> getLayerMap() {
        assert avatar != null;
        return getLayerMap(avatar, true);
    }

    private Map<LayerType, String> getLayerMap(@NonNull Avatar avatar, boolean resetHasAttributes) {
        EnumMap<LayerType, String> layerMap = getAvatarLayerMap(avatar);

        if (resetHasAttributes) hasBackground = hasMount = hasPet = false;

            String mountName = avatar.getCurrentMount();
            if (showMount && !TextUtils.isEmpty(mountName)) {
                layerMap.put(LayerType.MOUNT_BODY, "Mount_Body_" + mountName);
                layerMap.put(LayerType.MOUNT_HEAD, "Mount_Head_" + mountName);
                if (resetHasAttributes) hasMount = true;
            }

            String petName = avatar.getCurrentPet();
            if (showPet && !TextUtils.isEmpty(petName)) {
                layerMap.put(LayerType.PET, "Pet-" + petName);
                if (resetHasAttributes) hasPet = true;
            }

        String backgroundName = avatar.getBackground();
        if (showBackground && !TextUtils.isEmpty(backgroundName)) {
            layerMap.put(LayerType.BACKGROUND, "background_" + backgroundName);
            if (resetHasAttributes) hasBackground = true;
        }

        if (showSleeping && avatar.getSleep()) {
            layerMap.put(AvatarView.LayerType.ZZZ, "zzz");
        }

        return layerMap;
    }

    public EnumMap<AvatarView.LayerType, String> getAvatarLayerMap(Avatar avatar) {
        EnumMap<AvatarView.LayerType, String> layerMap = new EnumMap<>(AvatarView.LayerType.class);

        if (!avatar.isValid()) {
            return layerMap;
        }

        AvatarPreferences prefs = avatar.getPreferences();
        if (prefs == null) {
            return layerMap;
        }
        Outfit outfit;
        if (prefs.getCostume()) {
            outfit = avatar.getCostume();
        } else {
            outfit = avatar.getEquipped();
        }

        boolean hasVisualBuffs = false;

        if (avatar.getStats() != null && avatar.getStats().getBuffs() != null) {
            Buffs buffs = avatar.getStats().getBuffs();

            if (buffs.getSnowball()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "snowman");
                hasVisualBuffs = true;
            }

            if (buffs.getSeafoam()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "seafoam_star");
                hasVisualBuffs = true;
            }

            if (buffs.getShinySeed()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "avatar_floral_" + avatar.getStats().getHabitClass());
                hasVisualBuffs = true;
            }

            if (buffs.getSpookySparkles()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "ghost");
                hasVisualBuffs = true;
            }
        }

        if (!hasVisualBuffs) {
            if (!TextUtils.isEmpty(prefs.getChair())) {
                layerMap.put(AvatarView.LayerType.CHAIR, prefs.getChair());
            }

            if (outfit != null) {
                if (!TextUtils.isEmpty(outfit.getBack()) && !"back_base_0".equals(outfit.getBack())) {
                    layerMap.put(AvatarView.LayerType.BACK, outfit.getBack());
                }
                if (outfit.isAvailable(outfit.getArmor())) {
                    layerMap.put(AvatarView.LayerType.ARMOR, prefs.getSize() + "_" + outfit.getArmor());
                }
                if (outfit.isAvailable(outfit.getBody())) {
                    layerMap.put(AvatarView.LayerType.BODY, outfit.getBody());
                }
                if (outfit.isAvailable(outfit.getEyeWear())) {
                    layerMap.put(AvatarView.LayerType.EYEWEAR, outfit.getEyeWear());
                }
                if (outfit.isAvailable(outfit.getHead())) {
                    layerMap.put(AvatarView.LayerType.HEAD, outfit.getHead());
                }
                if (outfit.isAvailable(outfit.getHeadAccessory())) {
                    layerMap.put(AvatarView.LayerType.HEAD_ACCESSORY, outfit.getHeadAccessory());
                }
                if (outfit.isAvailable(outfit.getShield())) {
                    layerMap.put(AvatarView.LayerType.SHIELD, outfit.getShield());
                }
                if (outfit.isAvailable(outfit.getWeapon())) {
                    layerMap.put(AvatarView.LayerType.WEAPON, outfit.getWeapon());
                }
            }

            layerMap.put(AvatarView.LayerType.SKIN, "skin_" + prefs.getSkin() + ((prefs.getSleep()) ? "_sleep" : ""));
            layerMap.put(AvatarView.LayerType.SHIRT, prefs.getSize() + "_shirt_" + prefs.getShirt());
            layerMap.put(AvatarView.LayerType.HEAD_0, "head_0");

            Hair hair = prefs.getHair();
            if (hair != null) {
                String hairColor = hair.getColor();

                if (hair.isAvailable(hair.getBase())) {
                    layerMap.put(AvatarView.LayerType.HAIR_BASE, "hair_base_" + hair.getBase() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getBangs())) {
                    layerMap.put(AvatarView.LayerType.HAIR_BANGS, "hair_bangs_" + hair.getBangs() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getMustache())) {
                    layerMap.put(AvatarView.LayerType.HAIR_MUSTACHE, "hair_mustache_" + hair.getMustache() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getBeard())) {
                    layerMap.put(AvatarView.LayerType.HAIR_BEARD, "hair_beard_" + hair.getBeard() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getFlower())) {
                    layerMap.put(AvatarView.LayerType.HAIR_FLOWER, "hair_flower_" + hair.getFlower());
                }
            }
        } else {
            Hair hair = prefs.getHair();

            // Show flower all the time!
            if (hair != null && hair.isAvailable(hair.getFlower())) {
                layerMap.put(AvatarView.LayerType.HAIR_FLOWER, "hair_flower_" + hair.getFlower());
            }
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
                if (showMount || showPet) {
                    // full hero box
                    if (hasMount) {
                        offset = new PointF(13.0f, 12.0f);
                    } else if (hasPet) {
                        offset = new PointF(13.0f, 24.5f + 12.0f);
                    } else {
                        offset = new PointF(13.0f, 28.0f + 12.0f);
                    }
                } else if (showBackground) {
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
                    if (!(showMount || showPet)) {
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
                case VISUAL_BUFF:
                case HEAD:
                case HEAD_ACCESSORY:
                case HAIR_FLOWER:
                case SHIELD:
                case WEAPON:
                case ZZZ:
                    if (showMount || showPet) {
                        // full hero box
                        if (hasMount) {
                            offset = new PointF(25.0f, 0);
                        } else if (hasPet) {
                            offset = new PointF(25.0f, 24.5f);
                        } else {
                            offset = new PointF(25.0f, 28.0f);
                        }
                    } else if (showBackground) {
                        // compact hero box
                        offset = new PointF(0.0f, 18.0f);
                    }
                    break;
                case PET:
                    offset = new PointF(0, FULL_HERO_RECT.height() - layerImageInfo.getHeight());
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
        matrix.mapRect(boundsF);
        boundsF.round(bounds);

        return bounds;
    }

    private String getFileName(@NonNull String imageName) {
        if (FILENAME_MAP.containsKey(imageName)) {
            return FILENAME_MAP.get(imageName);
        }

        return imageName + ".png";
    }

    private void onLayerComplete() {
        if (numberLayersInProcess.decrementAndGet() == 0) {
            if (avatarImageConsumer != null) {
                avatarImageConsumer.accept(getAvatarImage());
            }
        }
    }

    public void onAvatarImageReady(@NonNull Consumer<Bitmap> consumer) {
        avatarImageConsumer = consumer;
        if (multiDraweeHolder.size() > 0 && numberLayersInProcess.get() == 0) {
            avatarImageConsumer.accept(getAvatarImage());
        } else {
            initAvatarRectMatrix();
            showLayers(getLayerMap());
        }
    }

    public void setAvatar(@NonNull Avatar avatar) {
        Avatar oldUser = this.avatar;
        this.avatar = avatar;

        if (oldUser != null) {
            Map<LayerType, String> newLayerMap = getLayerMap(avatar, false);

            boolean equals = currentLayers != null && currentLayers.equals(newLayerMap);

            if (!equals) {
                multiDraweeHolder.clear();
                numberLayersInProcess.set(0);
            }
        }
        invalidate();
    }



    private Rect getOriginalRect() {
        return (showMount || showPet) ? FULL_HERO_RECT : ((showBackground) ? COMPACT_HERO_RECT : HERO_ONLY_RECT);
    }

    private Bitmap getAvatarImage() {
        assert avatar != null;
        assert avatarRectF != null;
        Rect canvasRect = new Rect();
        avatarRectF.round(canvasRect);
        avatarBitmap = Bitmap.createBitmap(canvasRect.width(), canvasRect.height(), Bitmap.Config.ARGB_8888);
        avatarCanvas = new Canvas(avatarBitmap);
        draw(avatarCanvas);

        return avatarBitmap;
    }

    private void initAvatarRectMatrix() {
        if (avatarRectF == null) {
            Rect srcRect = getOriginalRect();

            if (isOrphan) {
                avatarRectF = new RectF(srcRect);

                // change scale to not be 1:1
                // a quick fix as fresco AnimatedDrawable/ScaleTypeDrawable
                // will not translate matrix properly
                matrix.setScale(1.2f, 1.2f);
                matrix.mapRect(avatarRectF);
            } else {
                // full hero box when showMount and showPet is enabled (140w * 147h)
                // compact hero box when only showBackground is enabled (114w * 114h)
                // hero only box when all show settings disabled (90w * 90h)
                avatarRectF = new RectF(0, 0, getWidth(), getHeight());
                matrix.setRectToRect(new RectF(srcRect), avatarRectF, Matrix.ScaleToFit.START); // TODO support other ScaleToFit
                avatarRectF = new RectF(srcRect);
                matrix.mapRect(avatarRectF);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initAvatarRectMatrix();

        // draw only when user is set
        if (avatar == null) return;

        // request image layers if not yet processed
        if (multiDraweeHolder.size() == 0) {
            showLayers(getLayerMap());
        }

        // manually call onAttach/onDetach if view is without parent as they will never be called otherwise
        if (isOrphan) multiDraweeHolder.onAttach();
        multiDraweeHolder.draw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        multiDraweeHolder.onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        multiDraweeHolder.onDetach();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        multiDraweeHolder.onAttach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        multiDraweeHolder.onAttach();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return multiDraweeHolder.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return multiDraweeHolder.verifyDrawable(who) || super.verifyDrawable(who);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        invalidate();
        if (avatarCanvas != null) draw(avatarCanvas);
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
        VISUAL_BUFF(14),
        HEAD(15),
        HEAD_ACCESSORY(16),
        HAIR_FLOWER(17),
        SHIELD(18),
        WEAPON(19),
        MOUNT_HEAD(20),
        ZZZ(21),
        PET(22);

        final int order;

        LayerType(int order) {
            this.order = order;
        }
    }

    public interface Consumer<T> {
        void accept(T t);
    }
}
