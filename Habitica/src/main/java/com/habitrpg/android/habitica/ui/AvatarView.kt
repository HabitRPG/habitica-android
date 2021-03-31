package com.habitrpg.android.habitica.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.DraweeHolder
import com.facebook.drawee.view.MultiDraweeHolder
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.BasePostprocessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import io.reactivex.rxjava3.functions.Consumer
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class AvatarView : View {

    private var showBackground = true
    private var showMount = true
    private var showPet = true
    private var showSleeping = true
    private var hasBackground: Boolean = false
    private var hasMount: Boolean = false
    private var hasPet: Boolean = false
    private var isOrphan: Boolean = false
    private val multiDraweeHolder = MultiDraweeHolder<GenericDraweeHierarchy>()
    private var avatar: Avatar? = null
    private var avatarRectF: RectF? = null
    private val avatarMatrix = Matrix()
    private val numberLayersInProcess = AtomicInteger(0)
    private var avatarImageConsumer: Consumer<Bitmap?>? = null
    private var avatarBitmap: Bitmap? = null
    private var avatarCanvas: Canvas? = null
    private var currentLayers: Map<LayerType, String>? = null

    private val layerMap: Map<LayerType, String>
        get() {
            val avatar = this.avatar ?: return emptyMap()
            return getLayerMap(avatar, true)
        }

    private var spriteSubstitutions: Map<String, Map<String, String>> = HashMap()
        get() {
            if (Date().time - (lastSubstitutionCheck?.time ?: 0) > 180000) {
                field = AppConfigManager(null).spriteSubstitutions()
                lastSubstitutionCheck = Date()
            }
            return field
        }
    private var lastSubstitutionCheck: Date? = null

    private val originalRect: Rect
        get() = if (showMount || showPet) FULL_HERO_RECT else if (showBackground) COMPACT_HERO_RECT else HERO_ONLY_RECT

    private val avatarImage: Bitmap?
        get() {
            if (BuildConfig.DEBUG && (avatar == null || avatarRectF == null)) {
                error("Assertion failed")
            }
            val canvasRect = Rect()
            avatarRectF?.round(canvasRect)
            avatarBitmap = Bitmap.createBitmap(canvasRect.width(), canvasRect.height(), Bitmap.Config.ARGB_8888)
            avatarBitmap?.let { avatarCanvas = Canvas(it) }
            draw(avatarCanvas)

            return avatarBitmap
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    constructor(context: Context, showBackground: Boolean, showMount: Boolean, showPet: Boolean) : super(context) {

        this.showBackground = showBackground
        this.showMount = showMount
        this.showPet = showPet
        isOrphan = true
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.AvatarView, defStyle, 0)

        try {
            showBackground = a.getBoolean(R.styleable.AvatarView_showBackground, true)
            showMount = a.getBoolean(R.styleable.AvatarView_showMount, true)
            showPet = a.getBoolean(R.styleable.AvatarView_showPet, true)
            showSleeping = a.getBoolean(R.styleable.AvatarView_showSleeping, true)
        } finally {
            a.recycle()
        }
    }

    private fun showLayers(layerMap: Map<LayerType, String>) {
        if (multiDraweeHolder.size() > 0) return
        var i = 0

        currentLayers = layerMap

        numberLayersInProcess.set(layerMap.size)

        for ((layerKey, layerName) in layerMap) {
            val layerNumber = i++

            val hierarchy = GenericDraweeHierarchyBuilder(resources)
                    .setFadeDuration(0)
                    .build()

            val draweeHolder = DraweeHolder.create(hierarchy, context)
            draweeHolder.topLevelDrawable?.callback = this
            multiDraweeHolder.add(draweeHolder)

            val uri = Uri.parse(IMAGE_URI_ROOT + DataBindingUtils.getFullFilename(layerName, null))
            var request = ImageRequestBuilder.newBuilderWithSource(uri)
            postProcessors[layerKey]?.let {
                request = request.setPostprocessor(it())
            }

            val controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request.build())
                    .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                        override fun onFinalImageSet(
                                id: String?,
                                imageInfo: ImageInfo?,
                                anim: Animatable?) {
                            if (imageInfo != null) {
                                if (multiDraweeHolder.size() > layerNumber) {
                                    multiDraweeHolder.get(layerNumber).topLevelDrawable?.bounds = getLayerBounds(layerKey, layerName, imageInfo)
                                }
                                onLayerComplete()
                            }
                        }

                        override fun onFailure(id: String?, throwable: Throwable?) {
                            Log.e(TAG, "Error loading layer: $layerName", throwable)
                            onLayerComplete()
                        }
                    })
                    .setAutoPlayAnimations(!isOrphan)
                    .build()
            draweeHolder.controller = controller
        }

        if (isOrphan) multiDraweeHolder.onAttach()
    }

    private fun getLayerMap(avatar: Avatar, resetHasAttributes: Boolean): Map<LayerType, String> {
        val layerMap = getAvatarLayerMap(avatar, spriteSubstitutions)

        if (resetHasAttributes) {
            hasPet = false
            hasMount = hasPet
            hasBackground = hasMount
        }

        var mountName = avatar.currentMount
        if (showMount && mountName?.isNotEmpty() == true) {
            mountName = substituteOrReturn(spriteSubstitutions["mounts"], mountName)
            layerMap[LayerType.MOUNT_BODY] = "Mount_Body_$mountName"
            layerMap[LayerType.MOUNT_HEAD] = "Mount_Head_$mountName"
            if (resetHasAttributes) hasMount = true
        }

        var petName = avatar.currentPet
        if (showPet && petName?.isNotEmpty() == true) {
            petName = substituteOrReturn(spriteSubstitutions["pets"], petName)
            layerMap[LayerType.PET] = "Pet-$petName"
            if (resetHasAttributes) hasPet = true
        }

        var backgroundName = avatar.preferences?.background
        if (showBackground && backgroundName?.isNotEmpty() == true) {
            backgroundName = substituteOrReturn(spriteSubstitutions["backgrounds"], backgroundName)
            layerMap[LayerType.BACKGROUND] = "background_$backgroundName"
            if (resetHasAttributes) hasBackground = true
        }

        if (showSleeping && avatar.sleep) {
            layerMap[LayerType.ZZZ] = "zzz"
        }

        return layerMap
    }

    private fun substituteOrReturn(substitutions: Map<String, String>?, name: String): String {
        for (key in substitutions?.keys ?: arrayListOf()) {
            if (name.contains(key)) {
                return substitutions?.get(key) ?: name
            }
        }
        return name
    }

    @Suppress("ReturnCount")
    private fun getAvatarLayerMap(avatar: Avatar, substitutions: Map<String, Map<String, String>>): EnumMap<LayerType, String> {
        val layerMap = EnumMap<LayerType, String>(LayerType::class.java)

        if (!avatar.isValid()) {
            return layerMap
        }

        val prefs = avatar.preferences ?: return layerMap
        val outfit = if (prefs.costume) {
            avatar.costume
        } else {
            avatar.equipped
        }

        var hasVisualBuffs = false

        avatar.stats?.buffs?.let { buffs ->
            if (buffs.snowball == true) {
                layerMap[LayerType.VISUAL_BUFF] = "avatar_snowball_" + avatar.stats?.habitClass
                hasVisualBuffs = true
            }

            if (buffs.seafoam == true) {
                layerMap[LayerType.VISUAL_BUFF] = "seafoam_star"
                hasVisualBuffs = true
            }

            if (buffs.shinySeed == true) {
                layerMap[LayerType.VISUAL_BUFF] = "avatar_floral_" + avatar.stats?.habitClass
                hasVisualBuffs = true
            }

            if (buffs.spookySparkles == true) {
                layerMap[LayerType.VISUAL_BUFF] = "ghost"
                hasVisualBuffs = true
            }
        }

        val substitutedVisualBuff = substitutions["visualBuff"]?.get("full")
        if (substitutedVisualBuff != null) {
            layerMap[LayerType.VISUAL_BUFF] = substitutedVisualBuff
            hasVisualBuffs = true
        }

        val hair = prefs.hair
        if (!hasVisualBuffs) {
            if (!TextUtils.isEmpty(prefs.chair)) {
                layerMap[LayerType.CHAIR] = prefs.chair
            }

            if (outfit != null) {
                if (!TextUtils.isEmpty(outfit.back) && "back_base_0" != outfit.back) {
                    layerMap[LayerType.BACK] = outfit.back
                }
                if (outfit.isAvailable(outfit.armor)) {
                    layerMap[LayerType.ARMOR] = prefs.size + "_" + outfit.armor
                }
                if (outfit.isAvailable(outfit.body)) {
                    layerMap[LayerType.BODY] = outfit.body
                }
                if (outfit.isAvailable(outfit.eyeWear)) {
                    layerMap[LayerType.EYEWEAR] = outfit.eyeWear
                }
                if (outfit.isAvailable(outfit.head)) {
                    layerMap[LayerType.HEAD] = outfit.head
                }
                if (outfit.isAvailable(outfit.headAccessory)) {
                    layerMap[LayerType.HEAD_ACCESSORY] = outfit.headAccessory
                }
                if (outfit.isAvailable(outfit.shield)) {
                    layerMap[LayerType.SHIELD] = outfit.shield
                }
                if (outfit.isAvailable(outfit.weapon)) {
                    layerMap[LayerType.WEAPON] = outfit.weapon
                }
            }

            layerMap[LayerType.SKIN] = "skin_" + prefs.skin + if (prefs.sleep) "_sleep" else ""
            layerMap[LayerType.SHIRT] = prefs.size + "_shirt_" + prefs.shirt
            layerMap[LayerType.HEAD_0] = "head_0"

            if (hair != null) {
                val hairColor = hair.color

                if (hair.isAvailable(hair.base)) {
                    layerMap[LayerType.HAIR_BASE] = "hair_base_" + hair.base + "_" + hairColor
                }
                if (hair.isAvailable(hair.bangs)) {
                    layerMap[LayerType.HAIR_BANGS] = "hair_bangs_" + hair.bangs + "_" + hairColor
                }
                if (hair.isAvailable(hair.mustache)) {
                    layerMap[LayerType.HAIR_MUSTACHE] = "hair_mustache_" + hair.mustache + "_" + hairColor
                }
                if (hair.isAvailable(hair.beard)) {
                    layerMap[LayerType.HAIR_BEARD] = "hair_beard_" + hair.beard + "_" + hairColor
                }
            }
        }

        if (hair != null && hair.isAvailable(hair.flower)) {
            layerMap[LayerType.HAIR_FLOWER] = "hair_flower_" + hair.flower
        }

        return layerMap
    }

    private fun getLayerBounds(layerType: LayerType, layerName: String, layerImageInfo: ImageInfo): Rect {
        var offset: PointF? = null
        val bounds = Rect(0, 0, layerImageInfo.width, layerImageInfo.height)
        val boundsF = RectF(bounds)

        // lookup layer specific offset
        when (layerName) {
            "weapon_special_critical" -> offset = if (showMount || showPet) {
                // full hero box
                when {
                    hasMount -> PointF(13.0f, 12.0f)
                    hasPet -> PointF(13.0f, 24.5f + 12.0f)
                    else -> PointF(13.0f, 28.0f + 12.0f)
                }
            } else if (showBackground) {
                // compact hero box
                PointF(-12.0f, 18.0f + 12.0f)
            } else {
                // hero only box
                PointF(-12.0f, 12.0f)
            }
        }

        // otherwise lookup default layer type based offset
        if (offset == null) {
            when (layerType) {
                LayerType.BACKGROUND -> if (!(showMount || showPet)) {
                    offset = PointF(-25.0f, 0.0f) // compact hero box
                }
                LayerType.MOUNT_BODY, LayerType.MOUNT_HEAD -> offset = PointF(25.0f, 18.0f) // full hero box
                LayerType.CHAIR, LayerType.BACK, LayerType.SKIN, LayerType.SHIRT, LayerType.ARMOR, LayerType.BODY, LayerType.HEAD_0, LayerType.HAIR_BASE, LayerType.HAIR_BANGS, LayerType.HAIR_MUSTACHE, LayerType.HAIR_BEARD, LayerType.EYEWEAR, LayerType.VISUAL_BUFF, LayerType.HEAD, LayerType.HEAD_ACCESSORY, LayerType.HAIR_FLOWER, LayerType.SHIELD, LayerType.WEAPON, LayerType.ZZZ -> if (showMount || showPet) {
                    // full hero box
                    offset = when {
                        hasMount -> if (layerMap[LayerType.MOUNT_HEAD]?.contains("Kangaroo") == true) {
                            PointF(25.0f, 18f)
                        } else {
                            PointF(25.0f, 0f)
                        }
                        hasPet -> PointF(25.0f, 24.5f)
                        else -> PointF(25.0f, 28.0f)
                    }
                } else if (showBackground) {
                    // compact hero box
                    offset = PointF(0.0f, 18.0f)
                }
                LayerType.PET -> offset = PointF(0f, (FULL_HERO_RECT.height() - layerImageInfo.height).toFloat())
            }
        }


        if (offset != null) {
            when (layerName) {
                "head_special_0" -> offset = PointF(offset.x-3, offset.y-18)
                "weapon_special_0" -> offset = PointF(offset.x-12, offset.y+4)
                "weapon_special_1" -> offset = PointF(offset.x-12, offset.y+4)
                "weapon_special_critical" -> offset = PointF(offset.x-12, offset.y+4)
                "head_special_1" -> offset = PointF(offset.x, offset.y+3)
            }

            val translateMatrix = Matrix()
            translateMatrix.setTranslate(offset.x, offset.y)
            translateMatrix.mapRect(boundsF)
        }

        // resize bounds to fit and keep original aspect ratio
        avatarMatrix.mapRect(boundsF)
        boundsF.round(bounds)

        return bounds
    }



    private fun onLayerComplete() {
        if (numberLayersInProcess.decrementAndGet() == 0) {
            avatarImageConsumer?.accept(avatarImage)
        }
    }

    fun onAvatarImageReady(consumer: Consumer<Bitmap?>) {
        avatarImageConsumer = consumer
        if (multiDraweeHolder.size() > 0 && numberLayersInProcess.get() == 0) {
            avatarImageConsumer?.accept(avatarImage)
        } else {
            initAvatarRectMatrix()
            showLayers(layerMap)
        }
    }

    fun setAvatar(avatar: Avatar) {
        val oldUser = this.avatar
        this.avatar = avatar

        if (oldUser != null) {
            val newLayerMap = getLayerMap(avatar, false)

            val equals = currentLayers != null && currentLayers == newLayerMap

            if (!equals || postProcessors.isNotEmpty()) {
                multiDraweeHolder.clear()
                numberLayersInProcess.set(0)
            }
        }
        invalidate()
    }

    private fun initAvatarRectMatrix() {
        if (avatarRectF == null) {
            val srcRect = originalRect

            if (isOrphan) {
                avatarRectF = RectF(srcRect)

                // change scale to not be 1:1
                // a quick fix as fresco AnimatedDrawable/ScaleTypeDrawable
                // will not translate matrix properly
                avatarMatrix.setScale(1.2f, 1.2f)
                avatarMatrix.mapRect(avatarRectF)
            } else {
                // full hero box when showMount and showPet is enabled (140w * 147h)
                // compact hero box when only showBackground is enabled (114w * 114h)
                // hero only box when all show settings disabled (90w * 90h)
                avatarRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
                avatarMatrix.setRectToRect(RectF(srcRect), avatarRectF, Matrix.ScaleToFit.START) // TODO support other ScaleToFit
                avatarRectF = RectF(srcRect)
                avatarMatrix.mapRect(avatarRectF)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        initAvatarRectMatrix()

        // draw only when user is set
        if (avatar?.isValid() != true) return

        // request image layers if not yet processed
        if (multiDraweeHolder.size() == 0) {
            showLayers(layerMap)
        }

        // manually call onAttach/onDetach if view is without parent as they will never be called otherwise
        if (isOrphan) multiDraweeHolder.onAttach()
        multiDraweeHolder.draw(canvas)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        multiDraweeHolder.onDetach()
    }

    override fun onStartTemporaryDetach() {
        super.onStartTemporaryDetach()
        multiDraweeHolder.onDetach()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        multiDraweeHolder.onAttach()
    }

    override fun onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach()
        multiDraweeHolder.onAttach()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return multiDraweeHolder.verifyDrawable(who) || super.verifyDrawable(who)
    }

    override fun invalidateDrawable(drawable: Drawable) {
        invalidate()
        if (avatarCanvas != null) draw(avatarCanvas)
    }

    enum class LayerType {
        BACKGROUND,
        MOUNT_BODY,
        CHAIR,
        BACK,
        SKIN,
        SHIRT,
        ARMOR,
        BODY,
        HEAD_0,
        HAIR_BASE,
        HAIR_BANGS,
        HAIR_MUSTACHE,
        HAIR_BEARD,
        EYEWEAR,
        VISUAL_BUFF,
        HEAD,
        HEAD_ACCESSORY,
        HAIR_FLOWER,
        SHIELD,
        WEAPON,
        MOUNT_HEAD,
        ZZZ,
        PET
    }

    companion object {
        const val IMAGE_URI_ROOT = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/"
        private const val TAG = "AvatarView"
        private val FULL_HERO_RECT = Rect(0, 0, 140, 147)
        private val COMPACT_HERO_RECT = Rect(0, 0, 114, 114)
        private val HERO_ONLY_RECT = Rect(0, 0, 90, 90)

        val postProcessors: MutableMap<LayerType, (() -> BasePostprocessor?)> = mutableMapOf()
    }
}
