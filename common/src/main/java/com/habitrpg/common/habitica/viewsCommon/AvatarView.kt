package com.habitrpg.common.habitica.viewsCommon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import coil.dispose
import coil.load
import com.habitrpg.common.habitica.BuildConfig
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.extensionsCommon.DataBindingUtils
import com.habitrpg.common.habitica.extensionsCommon.dpToPx
import com.habitrpg.common.habitica.helpers.AppConfigManager
import com.habitrpg.shared.habitica.models.Avatar
import java.util.Date
import java.util.EnumMap
import java.util.concurrent.atomic.AtomicInteger

class AvatarView : FrameLayout {
    private var showBackground = true
    private var showMount = true
    private var showPet = true
    private var showSleeping = true
    private var hasBackground: Boolean = false
    private var preview: Map<LayerType, String>? = null
    private var hasMount: Boolean = false
    private var hasPet: Boolean = false
    private val imageViewHolder = mutableListOf<ImageView>()
    private var avatar: Avatar? = null
    private var avatarRectF: RectF? = null
    private val avatarMatrix = Matrix()
    private val numberLayersInProcess = AtomicInteger(0)
    private var avatarImageConsumer: ((Bitmap?) -> Unit)? = null
    private var avatarBitmap: Bitmap? = null
    private var avatarCanvas: Canvas? = null
    private var currentLayers: Map<LayerType, String>? = null

    var configManager: AppConfigManager? = null

    private val layerMap: Map<LayerType, String>
        get() {
            val avatar = this.avatar ?: return emptyMap()
            return getLayerMap(avatar, true)
        }

    private var spriteSubstitutions: Map<String, Map<String, String>> = HashMap()
        get() {
            if (Date().time - (lastSubstitutionCheck?.time ?: 0) > 180000) {
                field = (configManager ?: AppConfigManager()).spriteSubstitutions()
                Log.i("AvatarView", field.toString())
                lastSubstitutionCheck = Date()
            }
            return field
        }
    private var lastSubstitutionCheck: Date? = null

    private val originalRect: Rect
        get() =
            if (showMount || showPet) {
                FULL_HERO_RECT
            } else if (showBackground) {
                COMPACT_HERO_RECT
            } else {
                HERO_ONLY_RECT
            }

    private val avatarImage: Bitmap?
        get() {
            if (BuildConfig.DEBUG && (avatar == null || avatarRectF == null)) {
                error("Assertion failed")
            }
            val viewWidth = if (width > 0) width else (layoutParams?.width ?: 140)
            val viewHeight = if (height > 0) height else (layoutParams?.height ?: 147)
            val canvasRect = Rect(0, 0, if (viewWidth > 0) viewWidth else 140.dpToPx(context), if (viewHeight > 0) viewHeight else 147.dpToPx(context))
            if (canvasRect.isEmpty) return null
            avatarBitmap =
                Bitmap.createBitmap(
                    canvasRect.width(),
                    canvasRect.height(),
                    Bitmap.Config.ARGB_8888,
                )
            avatarBitmap?.let { avatarCanvas = Canvas(it) }
            imageViewHolder.forEach {
                val bitmap = (it.drawable as? BitmapDrawable)?.bitmap ?: return@forEach
                avatarCanvas?.drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    Rect(it.marginStart, it.marginTop, bitmap.width + it.marginStart, bitmap.height + it.marginTop),
                    null,
                )
            }

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
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int,
    ) {
        // Load attributes
        val a =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AvatarView,
                defStyle,
                0,
            )

        try {
            showBackground = a.getBoolean(R.styleable.AvatarView_showBackground, true)
            showMount = a.getBoolean(R.styleable.AvatarView_showMount, true)
            showPet = a.getBoolean(R.styleable.AvatarView_showPet, true)
            showSleeping = a.getBoolean(R.styleable.AvatarView_showSleeping, true)
        } finally {
            a.recycle()
        }

        setWillNotDraw(false)
    }

    private fun showLayers(layerMap: Map<LayerType, String>) {
        var i = 0

        currentLayers = layerMap

        numberLayersInProcess.set(layerMap.size)

        for (layerKey in LAYER_ORDER) {
            val layerName = layerMap[layerKey] ?: continue
            val layerNumber = i++

            val imageView =
                if (imageViewHolder.size <= layerNumber) {
                    val newImageView = ImageView(context)
                    newImageView.scaleType = ImageView.ScaleType.MATRIX
                    addView(newImageView)
                    imageViewHolder.add(newImageView)
                    newImageView
                } else {
                    imageViewHolder[layerNumber]
                }

            if (imageView.tag == layerName) {
                continue
            }
            imageView.tag = layerName
            imageView.dispose()
            imageView.setImageResource(0)

            imageView.load(
                DataBindingUtils.BASE_IMAGE_URL +
                    DataBindingUtils.getFullFilename(
                        layerName,
                    ),
            ) {
                allowHardware(false)
                target(
                    object : coil.target.Target {
                        override fun onError(error: Drawable?) {
                            super.onError(error)
                            imageView.setImageDrawable(error)
                            onLayerComplete()
                        }

                        override fun onSuccess(result: Drawable) {
                            result.isFilterBitmap = false
                            super.onSuccess(result)
                            imageView.setImageDrawable(result)
                            if (result is Animatable) {
                                result.start()
                            }
                            val bounds = getLayerBounds(layerKey, layerName, result)
                            imageView.imageMatrix = avatarMatrix
                            val layoutParams = imageView.layoutParams as? LayoutParams
                            layoutParams?.topMargin = bounds.top
                            layoutParams?.marginStart = bounds.left
                            layoutParams?.width = bounds.right
                            layoutParams?.height = bounds.bottom
                            imageView.layoutParams = layoutParams
                            onLayerComplete()
                        }
                    },
                )
            }
        }
        while (i < (imageViewHolder.size)) {
            imageViewHolder[i].dispose()
            imageViewHolder[i].setImageResource(0)
            imageViewHolder[i].tag = null
            i++
        }
    }

    private fun getLayerMap(
        avatar: Avatar,
        resetHasAttributes: Boolean,
    ): Map<LayerType, String> {
        val layerMap = getAvatarLayerMap(avatar, spriteSubstitutions)

        if (resetHasAttributes) {
            hasPet = false
            hasMount = false
            hasBackground = false
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
        if (!preview.isNullOrEmpty()) {
            for ((key, value) in preview ?: emptyMap()) {
                layerMap[key] = value
                if (resetHasAttributes && key == LayerType.BACKGROUND) hasBackground = true
            }
        }

        if (showSleeping && avatar.sleep) {
            layerMap[LayerType.ZZZ] = "zzz"
        }

        return layerMap
    }

    private fun substituteOrReturn(
        substitutions: Map<String, String>?,
        name: String,
    ): String {
        for (key in substitutions?.keys ?: arrayListOf()) {
            if (name.contains(key)) {
                return substitutions?.get(key) ?: name
            }
        }
        return name
    }

    @Suppress("ReturnCount")
    private fun getAvatarLayerMap(
        avatar: Avatar,
        substitutions: Map<String, Map<String, String>>,
    ): EnumMap<LayerType, String> {
        val layerMap = EnumMap<LayerType, String>(LayerType::class.java)

        if (!avatar.isValid()) {
            return layerMap
        }

        val prefs = avatar.preferences ?: return layerMap
        val outfit =
            if (prefs.costume) {
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
            if (prefs.chair?.isNotBlank() == true && prefs.chair != "chair_none") {
                layerMap[LayerType.CHAIR] = prefs.chair
            }

            if (outfit != null) {
                if (!TextUtils.isEmpty(outfit.back) && "back_base_0" != outfit.back) {
                    layerMap[LayerType.BACK] = outfit.back
                }
                if (outfit.isAvailable(outfit.armor)) {
                    layerMap[LayerType.ARMOR] = (prefs.size ?: "broad") + "_" + outfit.armor
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
            layerMap[LayerType.SHIRT] = (prefs.size ?: "broad") + "_shirt_" + prefs.shirt
            layerMap[LayerType.HEAD_0] = "head_0"

            if (hair != null) {
                val hairColor = hair.color
                if (hair.isAvailable(hair.bangs)) {
                    layerMap[LayerType.HAIR_BANGS] = "hair_bangs_" + hair.bangs + "_" + hairColor
                }
                if (hair.isAvailable(hair.base)) {
                    layerMap[LayerType.HAIR_BASE] = "hair_base_" + hair.base + "_" + hairColor
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

    private fun getLayerBounds(
        layerType: LayerType,
        layerName: String,
        drawable: Drawable,
    ): Rect {
        var offset: PointF? = null
        val bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val boundsF = RectF(bounds)

        // lookup layer specific offset
        when (layerName) {
            "weapon_special_critical" ->
                offset =
                    if (showMount || showPet) {
                        // full hero box
                        when {
                            hasMount -> PointF(13.0f, 12.0f)
                            hasPet -> PointF(13.0f, 24.5f + 12.0f)
                            else -> PointF(13.0f, 24.0f + 12.0f)
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
                LayerType.BACKGROUND ->
                    if (!(showMount || showPet)) {
                        offset = PointF(-25.0f, 0.0f) // compact hero box
                    }
                LayerType.MOUNT_BODY, LayerType.MOUNT_HEAD ->
                    offset =
                        PointF(24.0f, 18.0f) // full hero box
                LayerType.CHAIR, LayerType.BACK, LayerType.SKIN, LayerType.SHIRT, LayerType.ARMOR, LayerType.BODY, LayerType.HEAD_0, LayerType.HAIR_BASE, LayerType.HAIR_BANGS, LayerType.HAIR_MUSTACHE, LayerType.HAIR_BEARD, LayerType.EYEWEAR, LayerType.VISUAL_BUFF, LayerType.HEAD, LayerType.HEAD_ACCESSORY, LayerType.HAIR_FLOWER, LayerType.SHIELD, LayerType.WEAPON, LayerType.ZZZ ->
                    if (showMount || showPet) {
                        // full hero box
                        offset =
                            when {
                                hasMount ->
                                    if (layerMap[LayerType.MOUNT_HEAD]?.contains("Kangaroo") == true) {
                                        PointF(24.0f, 18f)
                                    } else {
                                        PointF(24.0f, 0f)
                                    }
                                hasPet -> PointF(24.0f, 24f)
                                else -> PointF(24.0f, 28.0f)
                            }
                    } else if (showBackground) {
                        // compact hero box
                        offset = PointF(0.0f, 18.0f)
                    }
                LayerType.PET ->
                    offset =
                        PointF(0f, (FULL_HERO_RECT.height() - bounds.height()).toFloat())
            }
        }

        if (offset != null) {
            when (layerName) {
                "head_special_0" -> offset = PointF(offset.x - 3, offset.y - 18)
                "weapon_special_0" -> offset = PointF(offset.x - 12, offset.y + 4)
                "weapon_special_1" -> offset = PointF(offset.x - 12, offset.y + 4)
                "weapon_special_critical" -> offset = PointF(offset.x - 12, offset.y + 4)
                "head_special_1" -> offset = PointF(offset.x, offset.y + 3)
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
            avatarImageConsumer?.invoke(avatarImage)
        }
    }

    fun onAvatarImageReady(consumer: ((Bitmap?) -> Unit)) {
        avatarImageConsumer = consumer
        if (imageViewHolder.size > 0 && numberLayersInProcess.get() == 0) {
            avatarImageConsumer?.invoke(avatarImage)
        } else {
            initAvatarRectMatrix()
            showLayers(layerMap)
        }
    }

    fun setAvatar(
        avatar: Avatar,
        preview: Map<LayerType, String>? = null,
    ) {
        val oldUser = this.avatar
        this.avatar = avatar
        preview?.let { this.preview = preview }

        var equals = false
        if (oldUser != null) {
            val newLayerMap = getLayerMap(avatar, false)

            equals = currentLayers == newLayerMap
        }
        if (!equals) {
            invalidate()
        }
    }

    private fun initAvatarRectMatrix() {
        if (avatarRectF == null) {
            val srcRect = originalRect
            // full hero box when showMount and showPet is enabled (140w * 147h)
            // compact hero box when only showBackground is enabled (114w * 114h)
            // hero only box when all show settings disabled (90w * 90h)
            val viewWidth = if (width > 0) width else (layoutParams?.width ?: 140)
            val viewHeight = if (height > 0) height else (layoutParams?.height ?: 147)
            val width = if (viewWidth > 0) viewWidth.toFloat() else 140.dpToPx(context).toFloat()
            val height = if (viewHeight > 0) viewHeight.toFloat() else 147.dpToPx(context).toFloat()
            avatarRectF = RectF(0f, 0f, width, height)
            avatarMatrix.setRectToRect(RectF(srcRect), avatarRectF, Matrix.ScaleToFit.START)
            avatarRectF = RectF(srcRect)
            avatarMatrix.mapRect(avatarRectF)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        initAvatarRectMatrix()

        // draw only when user is set
        if (avatar?.isValid() != true) return

        showLayers(layerMap)
    }

    override fun invalidateDrawable(drawable: Drawable) {
        invalidate()
        avatarCanvas?.let { draw(it) }
    }

    enum class LayerType {
        BACKGROUND,
        MOUNT_BODY,
        CHAIR,
        BACK,
        SKIN,
        SHIRT,
        ARMOR,
        HEAD_0,
        HAIR_BASE,
        HAIR_BANGS,
        HAIR_MUSTACHE,
        HAIR_BEARD,
        BODY,
        EYEWEAR,
        VISUAL_BUFF,
        HEAD,
        HEAD_ACCESSORY,
        HAIR_FLOWER,
        SHIELD,
        WEAPON,
        MOUNT_HEAD,
        ZZZ,
        PET,
    }

    companion object {
        private val FULL_HERO_RECT = Rect(0, 0, 140, 147)
        private val COMPACT_HERO_RECT = Rect(0, 0, 114, 114)
        private val HERO_ONLY_RECT = Rect(0, 0, 90, 90)

        private val LAYER_ORDER = listOf<LayerType>(
            LayerType.BACKGROUND,
            LayerType.MOUNT_BODY,
            LayerType.CHAIR,
            LayerType.BACK,
            LayerType.SKIN,
            LayerType.SHIRT,
            LayerType.ARMOR,
            LayerType.HEAD_0,
            LayerType.HAIR_BANGS,
            LayerType.HAIR_BASE,
            LayerType.HAIR_MUSTACHE,
            LayerType.HAIR_BEARD,
            LayerType.BODY,
            LayerType.EYEWEAR,
            LayerType.VISUAL_BUFF,
            LayerType.HEAD,
            LayerType.HEAD_ACCESSORY,
            LayerType.HAIR_FLOWER,
            LayerType.SHIELD,
            LayerType.WEAPON,
            LayerType.MOUNT_HEAD,
            LayerType.ZZZ,
            LayerType.PET,
        )
    }
}
