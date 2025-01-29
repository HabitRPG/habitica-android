package com.habitrpg.android.habitica.ui.views.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabiticaPullRefreshIndicator(
    isInitial: Boolean,
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    scale: Boolean = true
) {
    AnimatedVisibility(
        visible = isInitial && isRefreshing,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            HabiticaCircularProgressView(Modifier)
        }
    }
    if (!isInitial) {
        Surface(
            modifier =
            modifier,
            shape = CircleShape,
            color = backgroundColor
        ) {
            AnimatedVisibility(
                visible = isRefreshing || state.distanceFraction > 0f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HabiticaCircularProgressView(
                    partialDisplay = if (isRefreshing) 1f else state.distanceFraction,
                    animate = isRefreshing,
                    indicatorSize = 40.dp,
                    strokeWidth = 6.dp,
                    modifier =
                    Modifier
                        .border(1.dp, HabiticaTheme.colors.windowBackground, CircleShape)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview() {
    val state = rememberPullToRefreshState()
    Box(Modifier) {
        LazyColumn {
        }
        HabiticaPullRefreshIndicator(isInitial = false, isRefreshing = true, state = state)
    }
}
