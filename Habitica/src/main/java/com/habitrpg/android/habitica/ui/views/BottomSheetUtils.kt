package com.habitrpg.android.habitica.ui.views

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Extension for Activity
fun Activity.showAsBottomSheet(content: @Composable (() -> Unit) -> Unit) {
    val viewGroup = this.findViewById(android.R.id.content) as ViewGroup
    addContentToView(viewGroup, content)
}

// Extension for Fragment
fun Fragment.showAsBottomSheet(content: @Composable (() -> Unit) -> Unit) {
    val viewGroup = requireActivity().findViewById(android.R.id.content) as ViewGroup
    addContentToView(viewGroup, content)
}

// Helper method
private fun addContentToView(
    viewGroup: ViewGroup,
    content: @Composable (() -> Unit) -> Unit
) {
    viewGroup.addView(
        ComposeView(viewGroup.context).apply {
            setContent {
                HabiticaTheme {
                    BottomSheetWrapper(viewGroup, this, content)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BottomSheetWrapper(
    parent: ViewGroup,
    composeView: ComposeView,
    content: @Composable (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    var isSheetOpened by remember { mutableStateOf(false) }

    val systemUiController = rememberSystemUiController()
    val statusBarColor = colorResource(R.color.content_background)
    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(statusBarColor.copy(alpha = 0.3f), darkIcons = true)
        onDispose {}
    }

    val radius = 20.dp
    ModalBottomSheetLayout(
        sheetBackgroundColor = Color.Transparent,
        scrimColor = colorResource(R.color.content_background).copy(alpha = 0.5f),
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = radius, topEnd = radius),
        sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .background(
                        HabiticaTheme.colors.windowBackground,
                        RoundedCornerShape(topStart = radius, topEnd = radius)
                    )
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .background(colorResource(R.color.content_background_offset))
                        .size(24.dp, 3.dp)
                )
                content {
                    // Action passed for clicking close button in the content
                    coroutineScope.launch {
                        modalBottomSheetState.hide() // will trigger the LaunchedEffect
                    }
                }
            }
        }
    ) {}

    BackHandler {
        coroutineScope.launch {
            modalBottomSheetState.hide() // will trigger the LaunchedEffect
        }
    }

    // Take action based on hidden state
    LaunchedEffect(modalBottomSheetState.currentValue) {
        when (modalBottomSheetState.currentValue) {
            ModalBottomSheetValue.Hidden -> {
                when {
                    isSheetOpened -> {
                        systemUiController.setStatusBarColor(statusBarColor, darkIcons = false)
                        parent.removeView(composeView)
                    }
                    else -> {
                        isSheetOpened = true
                        coroutineScope.launch {
                            delay(100L)
                            modalBottomSheetState.show()
                        }
                    }
                }
            }
            else -> {
            }
        }
    }
}
