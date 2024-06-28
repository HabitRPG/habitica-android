package com.habitrpg.android.habitica.ui.views.equipment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.theme.pixelArtBackground
import com.habitrpg.android.habitica.ui.views.PixelArtView
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.theme.caption2

@Composable
fun OverviewItem(
    text: String,
    iconName: String?,
    modifier: Modifier = Modifier,
    isTwoHanded: Boolean = false
) {
    val hasIcon =
        isTwoHanded || (
            iconName?.isNotBlank() == true && iconName != "shirt_" &&
                !iconName.endsWith(
                    "_none"
                ) && !iconName.endsWith("_base_0") && !iconName.endsWith("_")
            )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
        modifier
            .width(76.dp)
    ) {
        Box(
            Modifier
                .size(76.dp)
                .clip(MaterialTheme.shapes.small)
                .background(HabiticaTheme.colors.pixelArtBackground(hasIcon)),
            contentAlignment = Alignment.Center
        ) {
            if (isTwoHanded) {
                Image(painterResource(R.drawable.equipment_two_handed), null)
            } else if (hasIcon) {
                PixelArtView(
                    imageName = iconName,
                    modifier =
                    Modifier
                        .size(76.dp)
                )
            } else {
                Image(painterResource(R.drawable.empty_slot), null)
            }
        }
        Text(
            text,
            style = HabiticaTheme.typography.caption2,
            color = colorResource(R.color.text_secondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun EquipmentOverviewView(
    outfit: Outfit?,
    isUsingTwohanded: Boolean,
    onEquipmentTap: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier =
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colorResource(R.color.equipment_column_background))
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OverviewItem(
                stringResource(R.string.outfit_weapon),
                outfit?.weapon.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("weapon", outfit?.weapon)
                }
            )
            OverviewItem(
                stringResource(R.string.outfit_shield),
                outfit?.shield.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("shield", outfit?.shield)
                },
                isUsingTwohanded
            )
            OverviewItem(
                stringResource(R.string.outfit_head),
                outfit?.head.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("head", outfit?.head)
                }
            )
            OverviewItem(
                stringResource(R.string.outfit_armor),
                outfit?.armor.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("armor", outfit?.armor)
                }
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OverviewItem(
                stringResource(R.string.outfit_headAccessory),
                outfit?.headAccessory.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("headAccessory", outfit?.headAccessory)
                }
            )
            OverviewItem(
                stringResource(R.string.outfit_body),
                outfit?.body.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("body", outfit?.body)
                }
            )
            OverviewItem(
                stringResource(R.string.outfit_back),
                outfit?.back.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("back", outfit?.back)
                }
            )
            OverviewItem(
                stringResource(R.string.outfit_eyewear),
                outfit?.eyeWear.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onEquipmentTap("eyewear", outfit?.eyeWear)
                }
            )
        }
    }
}

@Composable
fun AvatarCustomizationOverviewView(
    preferences: Preferences?,
    outfit: Outfit?,
    onCustomizationTap: (String, String?) -> Unit,
    onAvatarEquipmentTap: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier =
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colorResource(R.color.equipment_column_background))
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OverviewItem(
                stringResource(R.string.avatar_shirt),
                preferences?.shirt.let { "icon_${preferences?.size}_shirt_$it" },
                modifier =
                Modifier.clickable {
                    onCustomizationTap("shirt", null)
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_skin),
                preferences?.skin.let { "icon_skin_$it" },
                modifier =
                Modifier.clickable {
                    onCustomizationTap("skin", null)
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_hair_color),
                if (preferences?.hair?.color != null && preferences.hair?.color != "") "icon_hair_bangs_1_" + preferences.hair?.color else "",
                modifier =
                Modifier.clickable {
                    onCustomizationTap("hair", "color")
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_hair_bangs),
                if (preferences?.hair?.bangs != null && preferences.hair?.bangs != 0) "icon_hair_bangs_" + preferences.hair?.bangs + "_" + preferences.hair?.color else "",
                modifier =
                Modifier.clickable {
                    onCustomizationTap("hair", "bangs")
                }
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OverviewItem(
                stringResource(R.string.avatar_style),
                if (preferences?.hair?.base != null && preferences.hair?.base != 0) "icon_hair_base_" + preferences.hair?.base + "_" + preferences.hair?.color else "",
                modifier =
                Modifier.clickable {
                    onCustomizationTap("hair", "base")
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_mustache),
                if (preferences?.hair?.mustache != null && preferences.hair?.mustache != 0) "icon_hair_mustache_" + preferences.hair?.mustache + "_" + preferences.hair?.color else "",
                modifier =
                Modifier.clickable {
                    onCustomizationTap("hair", "mustache")
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_beard),
                if (preferences?.hair?.beard != null && preferences.hair?.beard != 0) "icon_hair_beard_" + preferences.hair?.beard + "_" + preferences.hair?.color else "",
                modifier =
                Modifier.clickable {
                    onCustomizationTap("hair", "beard")
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_flower),
                if (preferences?.hair?.flower != null && preferences.hair?.flower != 0) "icon_hair_flower_" + preferences.hair?.flower else "",
                modifier =
                Modifier.clickable {
                    onCustomizationTap("hair", "flower")
                }
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            OverviewItem(
                stringResource(R.string.avatar_wheelchair),
                preferences?.chair?.let { if (it.startsWith("handleless")) "icon_chair_$it" else "icon_$it" },
                modifier =
                Modifier.clickable {
                    onCustomizationTap("chair", null)
                }
            )
            OverviewItem(
                stringResource(R.string.avatar_background),
                preferences?.background.let { "icon_background_$it" },
                modifier =
                Modifier.clickable {
                    onCustomizationTap("background", null)
                }
            )
            OverviewItem(
                stringResource(R.string.animal_ears),
                outfit?.headAccessory.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onAvatarEquipmentTap("headAccessory", "animal")
                }
            )
            OverviewItem(
                stringResource(R.string.animal_tail),
                outfit?.back.let { "shop_$it" },
                modifier =
                Modifier.clickable {
                    onAvatarEquipmentTap("back", "animal")
                }
            )
        }
    }
}

@Preview
@Composable
fun EquipmentOverviewItemPreview() {
    Column(Modifier.width(320.dp)) {
        Row(
            modifier = Modifier.background(colorResource(id = R.color.equipment_column_background)),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OverviewItem("Main-Hand", "shop_weapon_warrior_1")
            OverviewItem("Off-Hand", null, isTwoHanded = true)
            OverviewItem("Armor", null)
        }
        EquipmentOverviewView(null, false, { _, _ -> })
        AvatarCustomizationOverviewView(null, null, { _, _ -> }, { _, _ -> })
    }
}
