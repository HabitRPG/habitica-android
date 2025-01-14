package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import javax.inject.Inject

@Suppress("StringLiteralDuplication")
class SetupCustomizationRepositoryImpl
@Inject
constructor(private val context: Context) : SetupCustomizationRepository {
    private val wheelchairs: List<SetupCustomization>
        get() =
            listOf(
                SetupCustomization.createWheelchair("none", 0),
                SetupCustomization.createWheelchair("black", R.drawable.creator_chair_black),
                SetupCustomization.createWheelchair("blue", R.drawable.creator_chair_blue),
                SetupCustomization.createWheelchair("green", R.drawable.creator_chair_green),
                SetupCustomization.createWheelchair("pink", R.drawable.creator_chair_pink),
                SetupCustomization.createWheelchair("red", R.drawable.creator_chair_red),
                SetupCustomization.createWheelchair("yellow", R.drawable.creator_chair_yellow)
            )

    private val glasses: List<SetupCustomization>
        get() =
            listOf(
                SetupCustomization.createGlasses("", R.drawable.creator_blank_face),
                SetupCustomization.createGlasses(
                    "eyewear_special_blackTopFrame",
                    R.drawable.creator_eyewear_special_blacktopframe
                ),
                SetupCustomization.createGlasses(
                    "eyewear_special_blueTopFrame",
                    R.drawable.creator_eyewear_special_bluetopframe
                ),
                SetupCustomization.createGlasses(
                    "eyewear_special_greenTopFrame",
                    R.drawable.creator_eyewear_special_greentopframe
                ),
                SetupCustomization.createGlasses(
                    "eyewear_special_pinkTopFrame",
                    R.drawable.creator_eyewear_special_pinktopframe
                ),
                SetupCustomization.createGlasses(
                    "eyewear_special_redTopFrame",
                    R.drawable.creator_eyewear_special_redtopframe
                ),
                SetupCustomization.createGlasses(
                    "eyewear_special_yellowTopFrame",
                    R.drawable.creator_eyewear_special_yellowtopframe
                ),
                SetupCustomization.createGlasses(
                    "eyewear_special_whiteTopFrame",
                    R.drawable.creator_eyewear_special_whitetopframe
                )
            )

    private val flowers: List<SetupCustomization>
        get() =
            listOf(
                SetupCustomization.createFlower("0", R.drawable.creator_blank_face),
                SetupCustomization.createFlower("1", R.drawable.creator_hair_flower_1),
                SetupCustomization.createFlower("2", R.drawable.creator_hair_flower_2),
                SetupCustomization.createFlower("3", R.drawable.creator_hair_flower_3),
                SetupCustomization.createFlower("4", R.drawable.creator_hair_flower_4),
                SetupCustomization.createFlower("5", R.drawable.creator_hair_flower_5),
                SetupCustomization.createFlower("6", R.drawable.creator_hair_flower_6)
            )

    private val hairColors: List<SetupCustomization>
        get() =
            listOf(
                SetupCustomization.createHairColor("white", R.color.hair_white),
                SetupCustomization.createHairColor("brown", R.color.hair_brown),
                SetupCustomization.createHairColor("blond", R.color.hair_blond),
                SetupCustomization.createHairColor("red", R.color.hair_red),
                SetupCustomization.createHairColor("black", R.color.hair_black)
            )

    private val sizes: List<SetupCustomization>
        get() =
            listOf(
                SetupCustomization.createSize(
                    "slim",
                    R.drawable.creator_slim_shirt_black,
                    context.getString(R.string.avatar_size_slim)
                ),
                SetupCustomization.createSize(
                    "broad",
                    R.drawable.creator_broad_shirt_black,
                    context.getString(R.string.avatar_size_broad)
                )
            )

    private val skins: List<SetupCustomization>
        get() =
            listOf(
                SetupCustomization.createSkin("ddc994", R.color.skin_ddc994),
                SetupCustomization.createSkin("f5a76e", R.color.skin_f5a76e),
                SetupCustomization.createSkin("ea8349", R.color.skin_ea8349),
                SetupCustomization.createSkin("c06534", R.color.skin_c06534),
                SetupCustomization.createSkin("98461a", R.color.skin_98461a),
                SetupCustomization.createSkin("915533", R.color.skin_915533),
                SetupCustomization.createSkin("c3e1dc", R.color.skin_c3e1dc),
                SetupCustomization.createSkin("6bd049", R.color.skin_6bd049)
            )

    override fun getCustomizations(
        type: String,
        user: User
    ): List<SetupCustomization> {
        return getCustomizations(type, null, user)
    }

    override fun getCustomizations(
        type: String,
        subtype: String?,
        user: User
    ): List<SetupCustomization> {
        return when (type) {
            SetupCustomizationRepository.CATEGORY_BODY -> {
                when (subtype) {
                    SetupCustomizationRepository.SUBCATEGORY_SIZE -> sizes
                    SetupCustomizationRepository.SUBCATEGORY_SHIRT ->
                        getShirts(
                            user.preferences?.size ?: "slim"
                        )

                    else -> emptyList()
                }
            }

            SetupCustomizationRepository.CATEGORY_SKIN -> skins
            SetupCustomizationRepository.CATEGORY_HAIR -> {
                when (subtype) {
                    SetupCustomizationRepository.SUBCATEGORY_BANGS ->
                        getBangs(
                            user.preferences?.hair?.color ?: ""
                        )

                    SetupCustomizationRepository.SUBCATEGORY_PONYTAIL ->
                        getHairBases(
                            user.preferences?.hair?.color ?: ""
                        )

                    SetupCustomizationRepository.SUBCATEGORY_COLOR -> hairColors
                    else -> emptyList()
                }
            }

            SetupCustomizationRepository.CATEGORY_EXTRAS -> {
                when (subtype) {
                    SetupCustomizationRepository.SUBCATEGORY_FLOWER -> flowers
                    SetupCustomizationRepository.SUBCATEGORY_GLASSES -> glasses
                    SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> wheelchairs
                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }

    private fun getHairBases(color: String): List<SetupCustomization> {
        return listOf(
            SetupCustomization.createHairPonytail("0", R.drawable.creator_blank_face),
            SetupCustomization.createHairPonytail("1", getResId("creator_hair_base_1_$color")),
            SetupCustomization.createHairPonytail("3", getResId("creator_hair_base_3_$color"))
        )
    }

    private fun getBangs(color: String): List<SetupCustomization> {
        return listOf(
            SetupCustomization.createHairBangs("0", R.drawable.creator_blank_face),
            SetupCustomization.createHairBangs("1", getResId("creator_hair_bangs_1_$color")),
            SetupCustomization.createHairBangs("2", getResId("creator_hair_bangs_2_$color")),
            SetupCustomization.createHairBangs("3", getResId("creator_hair_bangs_3_$color"))
        )
    }

    private fun getShirts(size: String): List<SetupCustomization> {
        return if (size == "broad") {
            listOf(
                SetupCustomization.createShirt("black", R.drawable.creator_broad_shirt_black),
                SetupCustomization.createShirt("blue", R.drawable.creator_broad_shirt_blue),
                SetupCustomization.createShirt("green", R.drawable.creator_broad_shirt_green),
                SetupCustomization.createShirt("pink", R.drawable.creator_broad_shirt_pink),
                SetupCustomization.createShirt("white", R.drawable.creator_broad_shirt_white),
                SetupCustomization.createShirt("yellow", R.drawable.creator_broad_shirt_yellow)
            )
        } else {
            listOf(
                SetupCustomization.createShirt("black", R.drawable.creator_slim_shirt_black),
                SetupCustomization.createShirt("blue", R.drawable.creator_slim_shirt_blue),
                SetupCustomization.createShirt("green", R.drawable.creator_slim_shirt_green),
                SetupCustomization.createShirt("pink", R.drawable.creator_slim_shirt_pink),
                SetupCustomization.createShirt("white", R.drawable.creator_slim_shirt_white),
                SetupCustomization.createShirt("yellow", R.drawable.creator_slim_shirt_yellow)
            )
        }
    }

    private fun getResId(resName: String): Int {
        return try {
            context.resources.getIdentifier(resName, "drawable", context.packageName)
        } catch (e: Exception) {
            -1
        }
    }
}
