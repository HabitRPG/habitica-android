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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.pullRefreshIndicatorTransform
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import java.lang.Float.min

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HabiticaPullRefreshIndicator(
    isInitial: Boolean,
    isRefreshing: Boolean,
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    scale: Boolean = true
) {
    AnimatedVisibility(visible = isInitial && isRefreshing,
        enter = fadeIn(),
        exit = fadeOut()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            HabiticaCircularProgressView(Modifier)
        }
    }
    if (!isInitial) {
        Surface(
            modifier = modifier
                .pullRefreshIndicatorTransform(state, scale),
            shape = CircleShape,
            color = backgroundColor,
            elevation = if (isRefreshing) 6.dp else (min(1f, state.progress * 2) * 6f).dp
        ) {

            AnimatedVisibility(
                visible = isRefreshing || state.progress > 0f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HabiticaCircularProgressView(
                    partialDisplay = if (isRefreshing) 1f else state.progress,
                    animate = isRefreshing,
                    indicatorSize = 40.dp,
                    strokeWidth = 6.dp,
                    modifier = Modifier
                        .border(1.dp, HabiticaTheme.colors.windowBackground, CircleShape)
                        .padding(4.dp)
                        .background(MaterialTheme.colors.surface, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun Preview() {
    val state = rememberPullRefreshState(refreshing = true, onRefresh = {  })
    Box(Modifier.pullRefresh(state)) {
        LazyColumn {

        }
        HabiticaPullRefreshIndicator(isInitial = false, isRefreshing = true, state = state)
    }
}
