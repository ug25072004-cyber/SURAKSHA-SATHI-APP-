package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalEmergencyColors

/**
 * Official SurakshaSathi logo component.
 * Displays the authentic brand emblem with custom typography.
 */
@Composable
fun SurakshaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showWordmark: Boolean = true,
    tagline: String? = "Emergency Command Center"
) {
    val emergencyColors = LocalEmergencyColors.current

    Row(
        modifier = modifier.testTag("suraksha_brand_logo"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF00B0FF).copy(alpha = 0.35f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_emblem),
                contentDescription = "SurakshaSathi App Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Suraksha",
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.38f).coerceAtLeast(14f).sp,
                        color = Color(0xFF00B0FF),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Sathi",
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.38f).coerceAtLeast(14f).sp,
                        color = Color(0xFF00E676),
                        letterSpacing = (-0.5).sp
                    )
                }
                if (tagline != null) {
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.labelSmall,
                        color = emergencyColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
