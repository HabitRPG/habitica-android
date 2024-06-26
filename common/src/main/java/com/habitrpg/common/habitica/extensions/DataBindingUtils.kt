package com.habitrpg.common.habitica.extensions

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.helpers.AppConfigManager
import com.habitrpg.common.habitica.views.PixelArtView
import java.util.Date

fun PixelArtView.loadImage(
    imageName: String?,
    imageFormat: String? = null
) {
    val shouldLoadImage = DataBindingUtils.existsAsImage(imageName)
    if (shouldLoadImage && imageName != null) {
        val fullname = DataBindingUtils.getFullFilename(imageName, imageFormat)
        if (tag == fullname) {
            return
        }
        tag = fullname
        setImageDrawable(null)
        bitmap = null
        DataBindingUtils.loadImage(
            context,
            imageName,
            imageFormat,
            imageResult = {
                if (tag == fullname) {
                    bitmap =
                        if (fullname.endsWith("gif")) {
                            setImageDrawable(it)
                            if (it is Animatable) {
                                it.start()
                            }
                            null
                        } else {
                            it.toBitmap()
                        }
                }
            },
            imageError = {
                tag = null
                setImageDrawable(null)
                bitmap = null
            }
        )
    } else {
        tag = null
        setImageDrawable(null)
        bitmap = null
    }
}

object DataBindingUtils {
    fun loadImage(
        context: Context,
        imageName: String,
        imageResult: (Drawable) -> Unit
    ) {
        loadImage(context, imageName, null, imageResult)
    }

    fun loadImage(
        context: Context,
        imageName: String,
        imageFormat: String?,
        imageResult: (Drawable) -> Unit,
        imageError: () -> Unit = { }
    ) {
        val request =
            ImageRequest.Builder(context)
                .data(BASE_IMAGE_URL + getFullFilename(imageName, imageFormat))
                .target(
                    onStart = { _ ->
                    },
                    onSuccess = {
                        imageResult(it)
                    },
                    onError = {
                        imageError()
                    }
                )
                .build()
        context.imageLoader.enqueue(request)
    }

    fun getFullFilename(
        imageName: String,
        imageFormat: String? = null
    ): String {
        val name =
            when {
                spriteSubstitutions.containsKey(imageName) -> spriteSubstitutions[imageName]
                FILENAME_MAP.containsKey(imageName) -> FILENAME_MAP[imageName]
                imageName.startsWith("handleless") -> "chair_$imageName"
                else -> imageName
            }
        return name +
            if (imageFormat == null && FILEFORMAT_MAP.containsKey(imageName)) {
                "." + FILEFORMAT_MAP[imageName]
            } else {
                ".${imageFormat ?: "png"}"
            }
    }

    fun setRoundedBackground(
        view: View,
        color: Int
    ) {
        val drawable = ResourcesCompat.getDrawable(view.resources, R.drawable.layout_rounded_bg, null)
        drawable?.setTintWith(color, PorterDuff.Mode.MULTIPLY)
        view.background = drawable
    }

    fun existsAsImage(imageName: String?): Boolean {
        if (imageName == null) {
            return false
        }
        return imageName != "shop_"
    }

    class LayoutWeightAnimation(internal var view: View, internal var targetWeight: Float) : Animation() {
        private var initializeWeight: Float = 0.toFloat()

        private var layoutParams: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams

        init {
            initializeWeight = layoutParams.weight
        }

        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation
        ) {
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
                val subs = configManager?.spriteSubstitutions()
                field = subs?.get("generic") ?: subs?.get("pets") ?: HashMap()
                lastSubstitutionCheck = Date()
            }
            return field
        }
    private var lastSubstitutionCheck: Date? = null

    var configManager: AppConfigManager? = null

    init {
        val tempMap = mutableMapOf<String, String>()
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
        tempMap["stable_Pet-Wolf-Cerberus"] = "gif"
        tempMap["armor_special_ks2019"] = "gif"
        tempMap["slim_armor_special_ks2019"] = "gif"
        tempMap["broad_armor_special_ks2019"] = "gif"
        tempMap["eyewear_special_ks2019"] = "gif"
        tempMap["head_special_ks2019"] = "gif"
        tempMap["shield_special_ks2019"] = "gif"
        tempMap["weapon_special_ks2019"] = "gif"
        tempMap["Pet-Gryphon-Gryphatrice"] = "gif"
        tempMap["stable_Pet-Gryphon-Gryphatrice"] = "gif"
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
        tempMap["quest_virtualpet"] = "gif"
        tempMap["Pet_HatchingPotion_VirtualPet"] = "gif"
        tempMap["Pet-Gryphatrice-Jubilant"] = "gif"
        tempMap["stable_Pet-Gryphatrice-Jubilant"] = "gif"
        tempMap["back_special_heroicAureole"] = "gif"
        tempMap["Pet_HatchingPotion_Fungi"] = "gif"
        FILEFORMAT_MAP = tempMap

        val tempNameMap = mutableMapOf<String, String>()
        tempNameMap["head_special_1"] = "ContributorOnly-Equip-CrystalHelmet"
        tempNameMap["armor_special_1"] = "ContributorOnly-Equip-CrystalArmor"
        tempNameMap["head_special_0"] = "BackerOnly-Equip-ShadeHelmet"
        tempNameMap["armor_special_0"] = "BackerOnly-Equip-ShadeArmor"
        tempNameMap["shield_special_0"] = "BackerOnly-Shield-TormentedSkull"
        tempNameMap["weapon_special_0"] = "BackerOnly-Weapon-DarkSoulsBlade"
        tempNameMap["weapon_special_critical"] = "weapon_special_critical"
        tempNameMap["Pet-Wolf-Cerberus"] = "BackerOnly-Pet-CerberusPup"
        FILENAME_MAP = tempNameMap
    }
}
