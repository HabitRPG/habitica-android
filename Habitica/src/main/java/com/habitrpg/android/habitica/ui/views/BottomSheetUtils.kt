package com.habitrpg.android.habitica.ui.views

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.Boolean

fun Activity.showAsBottomSheet(content: @Composable (() -> Unit) -> Unit) {
    val viewGroup: ViewGroup = this.findViewById(android.R.id.content)
    addContentToView(viewGroup, content)
}

fun Activity.showAsBottomSheet(sheetColor: Color, disableScroll: Boolean = false, content: @Composable (() -> Unit) -> Unit) {
    val viewGroup: ViewGroup = this.findViewById(android.R.id.content)
    addContentToView(viewGroup, content, sheetColor, disableScroll)
}

fun Fragment.showAsBottomSheet(content: @Composable (() -> Unit) -> Unit) {
    val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
    addContentToView(viewGroup, content)
}

fun Fragment.showAsBottomSheet(sheetColor: Color, disableScroll: Boolean = false, content: @Composable (() -> Unit) -> Unit) {
    val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
    addContentToView(viewGroup, content, sheetColor, disableScroll)
}

private fun addContentToView(
    viewGroup: ViewGroup,
    content: @Composable (() -> Unit) -> Unit,
    sheetColor: Color? = null,
    disableScroll: Boolean = false
) {
    viewGroup.addView(
        ComposeView(viewGroup.context).apply {
            setContent {
                HabiticaTheme {
                    Column {
                        BottomSheetWrapper(viewGroup,
                            this@apply, sheetColor ?: HabiticaTheme.colors.windowBackground, disableScroll, content)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BottomSheetWrapper(
    parent: ViewGroup,
    composeView: ComposeView,
    sheetColor: Color = HabiticaTheme.colors.windowBackground,
    disableScroll: Boolean = false,
    content: @Composable (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpened by remember { mutableStateOf(false) }

    val radius = 20.dp
    ModalBottomSheet(
        {
            coroutineScope.launch {
                modalBottomSheetState.hide()
            }
        },
        containerColor = sheetColor,
        scrimColor = colorResource(R.color.gray_5).copy(alpha = 0.3f),
        sheetState = modalBottomSheetState,
        shape = RoundedCornerShape(topStart = radius, topEnd = radius),
        contentWindowInsets = { WindowInsets(0) },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                content {
                    // Action passed for clicking close button in the content
                    coroutineScope.launch {
                        modalBottomSheetState.hide() // will trigger the LaunchedEffect
                    }
                }
                Spacer(
                    Modifier.windowInsetsBottomHeight(
                        WindowInsets.navigationBarsIgnoringVisibility
                    )
                )
            }
        }
    )

    BackHandler {
        coroutineScope.launch {
            modalBottomSheetState.hide() // will trigger the LaunchedEffect
        }
    }

    // Take action based on hidden state
    LaunchedEffect(modalBottomSheetState.currentValue) {
        when (modalBottomSheetState.currentValue) {
            SheetValue.Hidden -> {
                when {
                    isSheetOpened -> {
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
