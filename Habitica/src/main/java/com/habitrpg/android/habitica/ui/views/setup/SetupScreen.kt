package com.habitrpg.android.habitica.ui.views.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.updateBounds
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.viewmodels.SetupViewModel
import com.habitrpg.android.habitica.ui.views.TypewriterText
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.ComposableAvatarView
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView

@Composable
fun SetupScreen(authViewModel: AuthenticationViewModel,
                viewModel: SetupViewModel = viewModel(),
                customizationRepository: SetupCustomizationRepository,
                onNextOnboardingStep: () -> Unit) {
    val username by authViewModel.username
    var currentStep by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    LaunchedEffect(currentStep) {
        if (currentStep == 2) {
            viewModel.saveSetup(context)
            viewModel.retrieveContent()
            onNextOnboardingStep()
        }
    }

    var selectedCustomizationCategory by remember { mutableStateOf("skin") }
    var selectedCustomizationSubcategory by remember { mutableStateOf("") }
    var selectedCustomizationSubcategoryIndex by remember { mutableIntStateOf(0) }

    val taskCategories = listOf(
        SetupViewModel.TYPE_WORK to stringResource(R.string.setup_group_work),
        SetupViewModel.TYPE_EXERCISE to stringResource(R.string.setup_group_exercise),
        SetupViewModel.TYPE_HEALTH to stringResource(R.string.setup_group_health),
        SetupViewModel.TYPE_SCHOOL to stringResource(R.string.setup_group_school),
        SetupViewModel.TYPE_TEAMS to stringResource(R.string.setup_group_teams),
        SetupViewModel.TYPE_CHORES to stringResource(R.string.setup_group_chores),
        SetupViewModel.TYPE_CREATIVITY to stringResource(R.string.setup_group_creativity),
    )
    val selectedTaskCategories by viewModel.selectedTaskCategories.collectAsState()

    val img = ImageBitmap.imageResource(R.drawable.border_pixelated)

    val bgColorTop = colorResource(R.color.brand_100)
    val bgColorMiddle = colorResource(R.color.brand_200)
    val bgColorBottom = colorResource(R.color.brand_300)
    val pixelImage = remember {
        ShaderBrush(
            ImageShader(
                img,
                TileMode.Repeated,
                TileMode.Repeated
            )
        )
    }

    var userChangeCounter by remember { mutableIntStateOf(0) }
    val user by viewModel.user.collectAsState()

    if (user == null) {
        val scope = rememberCoroutineScope()
        LifecycleStartEffect(Unit, LocalLifecycleOwner.current) {
            viewModel.initializeUser(authViewModel.user.value)
            scope.launchCatching {
                viewModel.initializeUser(authViewModel.retrieveUser())
            }
            onStopOrDispose { }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.brand_300))
        )
        return
    }

    val text = if (currentStep == 0) {
        stringResource(R.string.onboarding_step_avatar)
    } else {
        stringResource(R.string.onboarding_step_tasks)
    }
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(Color(0xFFC7E7FD))
                .padding(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
                )
                .padding(top = 48.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColorMiddle)
                    .weight(1f)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(186.dp)
                        .background(
                            ShaderBrush(
                                ImageShader(
                                    ImageBitmap.imageResource(R.drawable.stable_background_spring),
                                    TileMode.Repeated,
                                    TileMode.Repeated
                                )
                            )
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(18.dp)
                            .drawBehind {
                                drawRect(
                                    pixelImage,
                                    colorFilter = ColorFilter.tint(if (currentStep == 0) bgColorTop else bgColorMiddle)
                                )
                            }
                    )
                }

                AnimatedVisibility(currentStep == 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(bgColorTop)
                            .padding(top = 170.dp)
                            .padding(bottom = 26.dp)
                    ) {
                        CustomizationCategoryView(customizationRepository,
                            selectedCustomizationCategory,
                            selectedCustomizationSubcategory,
                            selectedCustomizationSubcategoryIndex,
                            viewModel.getActiveCustomization(selectedCustomizationCategory, selectedCustomizationSubcategory),
                            user, { category, index ->
                                selectedCustomizationSubcategory = category
                                selectedCustomizationSubcategoryIndex = index
                        }, {
                            viewModel.equipCustomization(it)
                                userChangeCounter++
                        })
                    }
                }
                AnimatedContent(currentStep, modifier = Modifier.weight(1f)) {
                    if (it == 0) {
                        CustomizationCategorySelector(
                            selectedCustomizationCategory,
                            { selectedCategory ->
                                selectedCustomizationCategory = selectedCategory
                                selectedCustomizationSubcategory = when (selectedCustomizationCategory) {
                                    SetupCustomizationRepository.CATEGORY_SKIN -> SetupCustomizationRepository.SUBCATEGORY_COLOR
                                    SetupCustomizationRepository.CATEGORY_HAIR -> SetupCustomizationRepository.SUBCATEGORY_COLOR
                                    SetupCustomizationRepository.CATEGORY_BODY -> SetupCustomizationRepository.SUBCATEGORY_SHIRT
                                    SetupCustomizationRepository.CATEGORY_EXTRAS -> SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR
                                    else -> ""
                                }
                                selectedCustomizationSubcategoryIndex = 0
                            }
                        )
                    } else {
                            OnboardingTaskSelector(
                                taskCategories,
                                selectedTaskCategories,
                                selectCategory = { category ->
                                    viewModel.selectTaskCategory(category)
                                },
                                modifier = Modifier.padding(top = 160.dp)
                            )
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(bgColorMiddle)
                    .drawBehind {
                        drawRect(
                            pixelImage,
                            colorFilter = ColorFilter.tint(bgColorBottom)
                        )
                    }
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(bgColorBottom)
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(
                        bottom = WindowInsets.systemBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { iteration ->
                        val color =
                            (if (currentStep == iteration) Color.White else Color.Black).copy(
                                alpha = 0.4f
                            )
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .rotate(45f)
                                .clip(RectangleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                        currentStep = if (currentStep == 0) {
                            1
                        } else {
                            2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.white),
                        contentColor = colorResource(R.color.gray_50)
                    ),
                    shape = HabiticaTheme.shapes.large,
                    contentPadding = PaddingValues(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 18.dp)
                ) {
                    Text(
                        stringResource(if (currentStep == 0) R.string.next_button else R.string.finish),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
                )
        ) {
            Text(
                "@${ user?.username ?: username }",
                color = colorResource(R.color.brand_600),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .border(
                        6.dp,
                        colorResource(R.color.brand_400),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(6.dp)
                    .background(
                        colorResource(R.color.brand_50),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .widthIn(min = 120.dp)
            )
            key(userChangeCounter) {
                ComposableAvatarView(
                    user, null, showBackground = false, showPet = false, showMount = false,
                    modifier = Modifier.size(120.dp).padding(top = 20.dp, end = 12.dp)
                )
            }
        }

        SpeechBubble(
            text, { Text("Justin") },
            npc = {
                Image(painterResource(R.drawable.justin_textbox), null)
            }, modifier = Modifier
                    .padding(horizontal = 36.dp)
                    .padding(top = 170.dp)
                    .padding(
                        top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
                    )
        )
        AnimatedVisibility(
            currentStep != 0,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
                )
        ) {
            Button(
                {
                    currentStep = 0
                },
                colors = ButtonDefaults.textButtonColors(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Image(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorResource(R.color.brand_50)),
                )
            }
        }
        AnimatedVisibility(
            currentStep == 2,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColorBottom)
            ) {
                AnimatedVisibility(currentStep == 2,
                    enter = fadeIn(tween(300, delayMillis = 400))) {
                    HabiticaCircularProgressView(indicatorSize = 75.dp)
                }
            }
        }
    }
}

@Composable
fun OnboardingTaskSelector(
    taskCategories: List<Pair<String, String>>,
    selectedCategories: Set<String>,
    selectCategory: (String) -> Unit,
    modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        contentPadding = PaddingValues(horizontal = 19.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        items(taskCategories) { category ->
            val isSelected = selectedCategories.contains(category.first)
            val transition = updateTransition(isSelected)
            val borderColor by transition.animateColor { if (it) colorResource(R.color.brand_400) else Color.Transparent }
            val borderWidth by transition.animateDp({
                tween(300)
            }) { if (it) 4.dp else 0.dp }
            val m = Modifier
                .border(borderWidth, borderColor, CircleShape)
            Text(
                category.second,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) colorResource(R.color.brand_200) else colorResource(R.color.white),
                modifier = m
                    .fillMaxWidth()
                    .background(if (isSelected) colorResource(R.color.white) else colorResource(R.color.brand_100), RoundedCornerShape(30.dp))
                    .clickable {
                        selectCategory(category.first)
                    }
                    .padding(20.dp)
            )
        }
    }
}

@Composable
fun CustomizationSubcategorySelector(
    selectedCategory: String,
    selectedTabIndex: Int,
    selectTab: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unselected = Color.White.copy(0.5f)
    AnimatedContent(selectedCategory, modifier = modifier) {
        ProvideTextStyle(
            TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        ) {
            TabRow(
                selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                divider = {},
                indicator = { positions ->
                    if (selectedTabIndex < positions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            Modifier.tabIndicatorOffset(positions[selectedTabIndex]),
                            width = 60.dp,
                            height = 2.dp,
                            color = colorResource(R.color.brand_400)
                        )
                    }
                }) {
                when (it) {
                    SetupCustomizationRepository.CATEGORY_BODY -> {
                        Tab(
                            selectedTabIndex == 0,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    0,
                                    SetupCustomizationRepository.SUBCATEGORY_SHIRT
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_shirt).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    SetupCustomizationRepository.CATEGORY_HAIR -> {
                        Tab(
                            selectedTabIndex == 0,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    0,
                                    SetupCustomizationRepository.SUBCATEGORY_COLOR
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_hair_color).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Tab(
                            selectedTabIndex == 1,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    1,
                                    SetupCustomizationRepository.SUBCATEGORY_BANGS
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_hair_bangs).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Tab(
                            selectedTabIndex == 3,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    2,
                                    SetupCustomizationRepository.SUBCATEGORY_PONYTAIL
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_hair_ponytail).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    SetupCustomizationRepository.CATEGORY_SKIN -> {
                        Tab(
                            selectedTabIndex == 0,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    0,
                                    SetupCustomizationRepository.SUBCATEGORY_COLOR
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_skin_color).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    SetupCustomizationRepository.CATEGORY_EXTRAS -> {
                        Tab(
                            selectedTabIndex == 0,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    0,
                                    SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_wheelchair).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Tab(
                            selectedTabIndex == 1,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    1,
                                    SetupCustomizationRepository.SUBCATEGORY_FLOWER
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_flower).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Tab(
                            selectedTabIndex == 2,
                            unselectedContentColor = unselected,
                            onClick = {
                                selectTab(
                                    2,
                                    SetupCustomizationRepository.SUBCATEGORY_GLASSES
                                )
                            }) {
                            Text(
                                stringResource(R.string.avatar_glasses).uppercase(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomizationCategoryView(customizationRepository: SetupCustomizationRepository,
                              selectedCategory: String,
                              selectedSubcategory: String,
                                selectedSubcategoryTabIndex: Int,
                              selectedItem: String,
                              user: User?,
                              selectSubcategory: (String, Int) -> Unit,
                              selectCustomization: (SetupCustomization) -> Unit,
                              modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
        CustomizationSubcategorySelector(selectedCategory, selectedSubcategoryTabIndex,{ index, tab ->
            selectSubcategory(tab, index)
        })
        AnimatedContent(
            selectedCategory,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 400)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 400)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }) { category ->
            val scrollState = rememberScrollState()
            AnimatedContent(
                selectedSubcategory,
                transitionSpec = {
                    slideIntoContainer(
                        if (targetState > initialState) {
                            AnimatedContentTransitionScope.SlideDirection.End
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        },
                        tween(400, 300)
                    )
                        .togetherWith(
                            slideOutOfContainer(
                                if (targetState > initialState) {
                                    AnimatedContentTransitionScope.SlideDirection.End
                                } else {
                                    AnimatedContentTransitionScope.SlideDirection.Start
                                }, tween(400, easing = LinearOutSlowInEasing)
                            )
                        )
                },
            ) { it ->
                val items = if (user != null) {
                    customizationRepository.getCustomizations(category, it, user)
                } else {
                    emptyList()
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .horizontalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.width(20.dp))
                    for (item in items) {
                        val transition = updateTransition(item.key == selectedItem.replace("chair_", ""))
                        val borderColor by transition.animateColor { if (it) colorResource(R.color.brand_400) else Color.Transparent }
                        val borderWidth by transition.animateDp({
                            tween(300)
                        }) { if (it) 4.dp else 0.dp }
                        val m = Modifier
                            .size(68.dp)
                            .border(borderWidth, borderColor, CircleShape)
                            .padding(4.dp)
                            .background(Color.White, CircleShape)
                            .clickable {
                                selectCustomization(item)
                            }
                        if (item.drawableId != null && item.drawableId != 0) {
                            Image(
                                painterResource(item.drawableId ?: R.drawable.creator_blank_face),
                                contentDescription = null,
                                contentScale = ContentScale.None,
                                modifier = m
                            )
                        } else if (item.colorId != null && item.colorId != 0) {
                            val color = colorResource(item.colorId ?: R.color.brand_400)
                            Canvas(modifier = m, onDraw = {
                                drawCircle(color = color)
                            })
                        } else {
                            Image(
                                painterResource(R.drawable.notification_close),
                                contentDescription = null,
                                contentScale = ContentScale.None,
                                modifier = m
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }
    }
}

@Composable
fun CustomizationCategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 31.dp)
    ) {
        val categories = listOf(
            Pair(SetupCustomizationRepository.CATEGORY_SKIN, Pair(stringResource(R.string.avatar_skin), R.drawable.icon_skin)),
            Pair(SetupCustomizationRepository.CATEGORY_HAIR, Pair(stringResource(R.string.avatar_hair), R.drawable.icon_hair)),
            Pair(SetupCustomizationRepository.CATEGORY_BODY, Pair(stringResource(R.string.avatar_body), R.drawable.icon_body)),
            Pair(
                SetupCustomizationRepository.CATEGORY_EXTRAS,
                Pair(stringResource(R.string.avatar_extras), R.drawable.icon_extras)
            )
        )
        categories.forEach { category ->
            val isSelected = selectedCategory == category.first
            val color = if (isSelected) Color.White else Color.White.copy(0.5f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (!isSelected) {
                            onCategorySelected(category.first)
                        }
                    }
                    .padding(8.dp)) {
                Image(
                    painterResource(category.second.second), contentDescription = null,
                    colorFilter = ColorFilter.tint(color)
                )
                Text(
                    category.second.first.uppercase(),
                    color = if (isSelected) Color.White else colorResource(R.color.brand_500),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.3.sp,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
        }
    }
}

@Composable
fun NamePlate(npcName: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val namePlateBackground = ContextCompat.getDrawable(
        LocalContext.current,
        R.drawable.name_plate
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
        .drawBehind {
            namePlateBackground?.updateBounds(0, 0, size.width.toInt(), size.height.toInt())
            namePlateBackground?.draw(drawContext.canvas.nativeCanvas)
        }
        .padding(horizontal = 28.dp)
        .heightIn(28.dp)
    ) {
        ProvideTextStyle(TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)) {
            npcName()
        }
    }
}

@Composable
@Preview
fun NameplatePreview() {
    NamePlate({
        Text("Justin")
    })
}

@Composable
fun SpeechBubble(
    text: String,
    npcName: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    npc: @Composable (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        npc?.let { npc ->
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(end = 30.dp)
            ) {
                npc()
            }
        }
        TypewriterText(
            text, fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 21.sp,
            color = colorResource(R.color.yellow_1),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp)
                .border(
                    width = 4.dp,
                    colorResource(R.color.yellow_10),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(24.dp)
        )
        NamePlate(npcName, modifier = Modifier
            .padding(start = 28.dp, top = 30.dp))
    }
}

@Composable
@Preview(device = "spec:width=600dp,height=300dp,dpi=320,orientation=portrait")
fun SpeechBubblePreview() {
    SpeechBubble("Hello World",
        npcName = { Text("Justin") },
        npc = {
            Image(painterResource(R.drawable.justin_textbox), null)
    }, modifier = Modifier.fillMaxWidth())
}
