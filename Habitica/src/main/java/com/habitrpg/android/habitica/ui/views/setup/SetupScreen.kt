package com.habitrpg.android.habitica.ui.views.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.R

@Composable
fun SetupScreen(onNextOnboardingStep: () -> Unit) {
    val density = LocalDensity.current
    Box(Modifier.fillMaxSize().background(Color(0xFFC7E7FD))) {
        Column(Modifier.padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()).padding(top = 48.dp)) {
            Box(
                Modifier.fillMaxWidth().height(186.dp)
                    .background(ShaderBrush(ImageShader(ImageBitmap.imageResource(R.drawable.stable_background_spring), TileMode.Repeated, TileMode.Repeated)))
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth().height(18.dp)
                        .background(ShaderBrush(ImageShader(ImageBitmap.imageResource(R.drawable.border_pixelated), TileMode.Repeated, TileMode.Repeated)))
                )
            }
        }
    }
}
