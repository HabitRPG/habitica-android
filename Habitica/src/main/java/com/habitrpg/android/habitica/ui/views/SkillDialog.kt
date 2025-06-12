package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.tooling.preview.Preview
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.habitrpg.android.habitica.ui.theme.pixelArtBackground

@Composable
fun SkillDialog(
    modifier: Modifier = Modifier,
    skillPath: String = "",
    skillKey: String = "",
    resourceIconPainter: Painter,
    title: String = "",
    description: String = "",
    mpCost: String = "",
    isTransformationItem: Boolean = false,
    onUseSkill: () -> Unit,
) {
    val colors = HabiticaTheme.colors
    val isDark = isSystemInDarkTheme()
    val chipBg = colorResource(R.color.blue_500_24)
    val chipTextColor = if (isDark) colorResource(R.color.blue_500) else colorResource(R.color.blue_10)

    Box(
        modifier
            .fillMaxWidth()
            .background(colors.windowBackground, shape = RoundedCornerShape(18.dp))
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .background(colorResource(R.color.content_background_offset))
                    .size(24.dp, 3.dp)
            )

            Box(
                Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.pixelArtBackground(hasIcon = true)),
                contentAlignment = Alignment.Center
            ) {
                PixelArtView(
                    imageName = "$skillPath$skillKey",
                    modifier = Modifier.size(62.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            if (!isTransformationItem) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(chipBg)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = resourceIconPainter,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mpCost,
                        color = chipTextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(28.dp))
            } else {
                Spacer(Modifier.height(18.dp))
            }
            Button(
                onClick = onUseSkill,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.brand_400),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(43.dp)
            ) {
                val label = if (isTransformationItem)
                    stringResource(R.string.use_on_party)
                else
                    stringResource(R.string.use_skill)
                Text(
                    text = label,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp
                )
            }
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF232136)
@Composable
fun PreviewSkillDialog() {
    SkillDialog(
        skillKey = "",
        skillPath = "",
        resourceIconPainter = painterResource(id = R.drawable.empty_slot),
        title = "Title Skill",
        description = "Skill Description",
        mpCost = "10 MP",
        isTransformationItem = true,
        onUseSkill = {

        }
    )
}






