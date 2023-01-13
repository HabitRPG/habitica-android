package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.CurrencyText
import com.habitrpg.android.habitica.ui.views.PixelArtView
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class BirthdayActivity : BaseActivity() {
    @Inject
    lateinit var userViewModel: MainUserViewModel

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var configManager: AppConfigManager

    private val isPurchasing = mutableStateOf(false)
    private val price = mutableStateOf("")
    private val hasGryphatrice = mutableStateOf(false)
    private var gryphatriceProductDetails: ProductDetails? = null

    override fun getLayoutResId(): Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val event = configManager.getBirthdayEvent()
        setContent {
            HabiticaTheme {
                BirthdayActivityView(
                    isPurchasing.value,
                    hasGryphatrice.value,
                    price.value,
                    event?.start ?: Date(),
                    event?.end ?: Date(),
                    {
                        gryphatriceProductDetails?.let {
                            isPurchasing.value = true
                            purchaseHandler.purchase(this, it)
                        }
                    },
                    {
                        lifecycleScope.launchCatching({
                            isPurchasing.value = false
                        }) {
                            isPurchasing.value = true
                            inventoryRepository.purchaseItem("pets", "Gryphatrice-Jubilant", 1)
                            userRepository.retrieveUser(false, true)
                            isPurchasing.value = false
                        }
                    },
                    {
                        lifecycleScope.launchCatching {
                            inventoryRepository.equip("pet", "Gryphatrice-Jubilant")
                        }
                    })
            }
        }

        lifecycleScope.launchCatching {
            inventoryRepository.getOwnedPets()
                .map { pets ->
                    pets.firstOrNull { it.key == "Gryphatrice-Jubilant" }
                }
                .collect {
                    hasGryphatrice.value = (it?.trained ?: 0) >= 5
                }
        }

        lifecycleScope.launchCatching {
            gryphatriceProductDetails = purchaseHandler.getGryphatriceSKU()
            price.value = gryphatriceProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
        }
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }
}

@Composable
fun BirthdayTitle(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(colorResource(id = R.color.brand_50))
        )
        Image(painterResource(id = R.drawable.birthday_textdeco_left), null)
        Text(
            text,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Image(painterResource(id = R.drawable.birthday_textdeco_right), null)
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(colorResource(id = R.color.brand_50))
        )
    }
}

@Composable
fun BirthdayActivityView(
    isPurchasing: Boolean,
    hasGryphatrice: Boolean,
    price: String,
    startDate: Date,
    endDate: Date,
    onPurchaseClick: () -> Unit,
    onGemPurchaseClick: () -> Unit,
    onEquipClick: () -> Unit
) {
    val activity = LocalContext.current as? Activity
    val dateFormat = SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    val textColor = Color.White
    val specialTextColor = colorResource(R.color.yellow_50)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .background(
                Brush.verticalGradient(
                    Pair(0.0f, colorResource(id = R.color.brand_300)),
                    Pair(1.0f, colorResource(id = R.color.brand_200))
                )
            )
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = {
                if (activity != null) {
                    activity.finish()
                    return@Button
                }
                MainNavigationController.navigateBack()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = textColor),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Image(
                painterResource(R.drawable.arrow_back),
                stringResource(R.string.action_back),
                colorFilter = ColorFilter.tint(
                    textColor
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            Image(
                painterResource(R.drawable.birthday_header),
                null,
                Modifier.padding(bottom = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.birthday_gifts), null)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {
                    Text(
                        stringResource(id = R.string.limited_event).toUpperCase(Locale.current),
                        fontSize = 12.sp,
                        color = specialTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(
                            R.string.x_to_y,
                            dateFormat.format(startDate),
                            dateFormat.format(endDate)
                        ),
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                // right image should be flipped
                Image(
                    painterResource(id = R.drawable.birthday_gifts),
                    null,
                    modifier = Modifier.scale(-1f, 1f)
                )
            }
            Text(
                stringResource(R.string.birthday_title_description),
                fontSize = 16.sp,
                color = specialTextColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp)
            )
            BirthdayTitle(stringResource(id = R.string.animated_gryphatrice_pet))
            Box(
                Modifier
                    .size(161.dp, 129.dp)
                    .padding(vertical = 20.dp)
                    .background(colorResource(R.color.brand_50), RoundedCornerShape(8.dp))
            ) {

            }
            Text(
                stringResource(R.string.limited_edition).toUpperCase(Locale.current),
                fontSize = 12.sp,
                color = specialTextColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.gryphatrice_description),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (hasGryphatrice) {
                Text(
                    stringResource(R.string.thanks_for_support),
                    fontSize = 12.sp,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
                HabiticaButton(
                    Color.White,
                    colorResource(R.color.brand_200),
                    {},
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(stringResource(R.string.equip))
                }
            } else if (isPurchasing) {
                CircularProgressIndicator()
            } else {
                Text(buildAnnotatedString {
                    append("Buy for ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(price)
                    }
                    append(" or ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("60 Gems")
                    }
                }, color = Color.White)
                HabiticaButton(
                    Color.White,
                    colorResource(R.color.brand_200),
                    {
                        onPurchaseClick()
                    },
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(stringResource(R.string.buy_for_x, ""))
                }
                HabiticaButton(
                    Color.White,
                    colorResource(R.color.brand_200),
                    {
                        onGemPurchaseClick()
                    },
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.buy_for))
                        CurrencyText(currency = "gems", value = 60)
                    }
                }
            }
            BirthdayTitle(stringResource(id = R.string.plenty_of_potions))
            Text(
                stringResource(R.string.plenty_of_potions_description),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            PotionGrid()
            HabiticaButton(
                Color.White,
                colorResource(R.color.brand_200),
                {
                    MainScope().launchCatching {
                        activity?.finish()
                        delay(500)
                        MainNavigationController.navigate(R.id.marketFragment)
                    }
                },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(stringResource(R.string.visit_the_market))
            }
            BirthdayTitle(stringResource(id = R.string.for_for_free))
            Text(
                stringResource(R.string.for_for_free_description),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(vertical = 20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FourFreeItem(
                        day = 1,
                        title = stringResource(R.string.a_party_robe),
                        imageName = "",
                        modifier = Modifier.weight(1f)
                    )
                    FourFreeItem(
                        day = 1,
                        title = stringResource(R.string.twenty_gems),
                        imageName = "",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FourFreeItem(
                        day = 5,
                        title = stringResource(R.string.birthday_set),
                        imageName = "",
                        modifier = Modifier.weight(1f)
                    )
                    FourFreeItem(
                        day = 10,
                        title = stringResource(R.string.background),
                        imageName = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 20.dp)
                .background(colorResource(R.color.brand_50))
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 60.dp)
        ) {
            Text(
                stringResource(R.string.limitations),
                fontSize = 16.sp,
                color = colorResource(R.color.brand_600),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.birthday_limitations),
                fontSize = 14.sp,
                color = colorResource(R.color.brand_600),
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PotionGrid() {
    val potions = listOf(
        "Porcelain",
        "Vampire",
        "Aquatic",
        "StainedGlass",
        "Celestial",
        "Glow",
        "AutumnLeaf",
        "SandSculpture",
        "Peppermint",
        "Shimmer"
    ).windowed(4, 4, true)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp)
    ) {
        for (potionGroup in potions) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (potion in potionGroup) {
                    Box(
                        Modifier
                            .size(68.dp)
                            .background(colorResource(R.color.brand_50), RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = DataBindingUtils.BASE_IMAGE_URL + DataBindingUtils.getFullFilename(
                                "Pet_HatchingPotion_$potion"
                            ), null, Modifier.size(68.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FourFreeItem(
    day: Int,
    title: String,
    imageName: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier
            .background(colorResource(R.color.brand_50), HabiticaTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.day_x, day).uppercase(),
            color = colorResource(R.color.yellow_50),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        PixelArtView(imageName,
            Modifier
                .size(121.dp, 84.dp)
                .background(colorResource(R.color.brand_100), HabiticaTheme.shapes.medium))
        Text(title, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun HabiticaButton(
    background: Color,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier
        .background(background, HabiticaTheme.shapes.medium)
        .clickable { onClick() }
        .fillMaxWidth()
        .padding(8.dp)) {
        ProvideTextStyle(
            value = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        ) {
            content()
        }
    }
}

@Preview(device = Devices.PIXEL_4)
@Composable
private fun Preview() {
    BirthdayActivityView(true, false, "", Date(), Date(), {

    }, {}, {})
}