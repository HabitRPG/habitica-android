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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import com.habitrpg.android.habitica.ui.theme.pixelArtBackground

@Composable
fun SkillDialog(
    modifier: Modifier = Modifier,
    skillPath: String = "",
    skillKey: String = "",
    resourceIconPainter: Painter,
    title: String,
    description: String,
    mpCost: String,
    onUseSkill: () -> Unit,
) {
    val colors = HabiticaTheme.colors
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
                Modifier
                    .padding(bottom = 12.dp)
                    .size(width = 38.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(colors.textSecondary)
            )

            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.pixelArtBackground(hasIcon = true)),
                contentAlignment = Alignment.Center
            ) {
                PixelArtView (
                    imageName = skillPath + skillKey,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = description,
                fontSize = 15.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF393F68))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = resourceIconPainter,
                    contentDescription = null,
                    tint = Color(0xFF77C7F7),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mpCost,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onUseSkill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA380FC),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Use Skill",
                    fontWeight = FontWeight.Bold,
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
        onUseSkill = {

        }
    )
}






