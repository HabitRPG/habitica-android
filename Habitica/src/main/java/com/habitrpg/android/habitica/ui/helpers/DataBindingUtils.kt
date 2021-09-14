package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.AppConfigManager
import java.util.*
import kotlin.collections.HashMap

fun ImageView.loadImage(imageName: String?, imageFormat: String? = null) {
    DataBindingUtils.loadImage(this, imageName, imageFormat)
}

object DataBindingUtils {

    fun loadImage(view: ImageView?, imageName: String) {
        loadImage(view, imageName, null)
    }

    fun loadImage(view: ImageView?, imageName: String?, imageFormat: String? = null) {
        if (view != null && imageName != null && view.visibility == View.VISIBLE) {
            val fullname = getFullFilename(imageName, imageFormat)
            if (view.tag == fullname) {
                return
            }
            view.tag = fullname
            view.load(BASE_IMAGE_URL + fullname)
        }
    }

    fun loadImage(context: Context, imageName: String, imageResult: (Drawable) -> Unit) {
        loadImage(context, imageName, null, imageResult)
    }

    fun loadImage(context: Context, imageName: String, imageFormat: String?, imageResult: (Drawable) -> Unit) {
        val request = ImageRequest.Builder(context)
            .data(BASE_IMAGE_URL + getFullFilename(imageName, imageFormat))
            .target {
                imageResult(it)
            }
            .build()
        context.imageLoader.enqueue(request)
    }

    fun getFullFilename(imageName: String, imageFormat: String? = null): String {
        val name = when {
            spriteSubstitutions.containsKey(imageName) -> spriteSubstitutions[imageName]
            FILENAME_MAP.containsKey(imageName) -> FILENAME_MAP[imageName]
            imageName.startsWith("handleless") -> "chair_$imageName"
            else -> imageName
        }
        return name + if (imageFormat == null && FILEFORMAT_MAP.containsKey(imageName)) {
            "." + FILEFORMAT_MAP[imageName]
        } else {
            ".${imageFormat ?: "png"}"
        }
    }

    fun setRoundedBackground(view: View, color: Int) {
        val drawable = ResourcesCompat.getDrawable(view.resources, R.drawable.layout_rounded_bg, null)
        drawable?.setTintWith(color, PorterDuff.Mode.MULTIPLY)
        view.background = drawable
    }

    class LayoutWeightAnimation(internal var view: View, internal var targetWeight: Float) : Animation() {
        private var initializeWeight: Float = 0.toFloat()

        private var layoutParams: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams

        init {
            initializeWeight = layoutParams.weight
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            layoutParams.weight = initializeWeight + (targetWeight - initializeWeight) * interpolatedTime

            view.requestLayout()
        }

        override fun willChangeBounds(): Boolean = true
    }

    const val BASE_IMAGE_URL = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/"
    private val FILEFORMAT_MAP: Map<String, String>
    private val FILENAME_MAP: Map<String, String>

    private var spriteSubstitutions: Map<String, String> = HashMap()
        get() {
            if (Date().time - (lastSubstitutionCheck?.time ?: 0) > 180000) {
                field = AppConfigManager(null).spriteSubstitutions()["generic"] ?: HashMap()
                lastSubstitutionCheck = Date()
            }
            return field
        }
    private var lastSubstitutionCheck: Date? = null

    init {
        val tempMap = HashMap<String, String>()
        tempMap["head_special_1"] = "gif"
        tempMap["broad_armor_special_1"] = "gif"
        tempMap["slim_armor_special_1"] = "gif"
        tempMap["head_special_0"] = "gif"
        tempMap["slim_armor_special_0"] = "gif"
        tempMap["broad_armor_special_0"] = "gif"
        tempMap["weapon_special_critical"] = "gif"
        tempMap["weapon_special_0"] = "gif"
        tempMap["shield_special_0"] = "gif"
        tempMap["Pet-Wolf-Cerberus"] = "gif"
        tempMap["armor_special_ks2019"] = "gif"
        tempMap["slim_armor_special_ks2019"] = "gif"
        tempMap["broad_armor_special_ks2019"] = "gif"
        tempMap["eyewear_special_ks2019"] = "gif"
        tempMap["head_special_ks2019"] = "gif"
        tempMap["shield_special_ks2019"] = "gif"
        tempMap["weapon_special_ks2019"] = "gif"
        tempMap["Pet-Gryphon-Gryphatrice"] = "gif"
        tempMap["Mount_Head_Gryphon-Gryphatrice"] = "gif"
        tempMap["Mount_Body_Gryphon-Gryphatrice"] = "gif"
        tempMap["background_clocktower"] = "gif"
        tempMap["background_airship"] = "gif"
        tempMap["background_steamworks"] = "gif"
        tempMap["Pet_HatchingPotion_Veggie"] = "gif"
        tempMap["Pet_HatchingPotion_Dessert"] = "gif"
        tempMap["Pet-HatchingPotion-Dessert"] = "gif"
        tempMap["quest_windup"] = "gif"
        tempMap["Pet-HatchingPotion_Windup"] = "gif"
        tempMap["Pet_HatchingPotion_Windup"] = "gif"
        tempMap["quest_solarSystem"] = "gif"
        FILEFORMAT_MAP = Collections.unmodifiableMap(tempMap)

        val tempNameMap = HashMap<String, String>()
        tempNameMap["head_special_1"] = "ContributorOnly-Equip-CrystalHelmet"
        tempNameMap["armor_special_1"] = "ContributorOnly-Equip-CrystalArmor"
        tempNameMap["head_special_0"] = "BackerOnly-Equip-ShadeHelmet"
        tempNameMap["armor_special_0"] = "BackerOnly-Equip-ShadeArmor"
        tempNameMap["shield_special_0"] = "BackerOnly-Shield-TormentedSkull"
        tempNameMap["weapon_special_0"] = "BackerOnly-Weapon-DarkSoulsBlade"
        tempNameMap["weapon_special_critical"] = "weapon_special_critical"
        tempNameMap["Pet-Wolf-Cerberus"] = "Pet-Wolf-Cerberus"
        FILENAME_MAP = Collections.unmodifiableMap(tempNameMap)
    }
}
