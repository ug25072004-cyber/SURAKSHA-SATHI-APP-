package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
 * SurakshaSathi Official Brand Logo Component.
 * Displays the authentic SurakshaSathi brand emblem and visual identity:
 * - Protective Shield in Blue gradient
 * - Digital AI matrix pixels
 * - Sunrise rays with Orange Map Pin
 * - Mountain peaks & flowing river cupped in protective hands
 * - Wordmark: "Suraksha" (Navy/Cyan) + "Sathi" (Emerald Teal) with leaf accent
 */
@Composable
fun SurakshaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showWordmark: Boolean = true,
    tagline: String? = "Emergency Command Center"
) {
    val emergencyColors = LocalEmergencyColors.current

    Row(
        modifier = modifier.testTag("suraksha_brand_logo"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High quality rounded container with clean white background showcasing the official shield emblem
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFCFAFB),
            border = BorderStroke(1.dp, Color(0xFF00B0FF).copy(alpha = 0.35f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.suraksha_emblem),
                contentDescription = "SurakshaSathi Official Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
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
                        text = "Sath",
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.38f).coerceAtLeast(14f).sp,
                        color = Color(0xFF00E676),
                        letterSpacing = (-0.5).sp
                    )
                    // Styled 'i' with leaf accent matching official logo artwork
                    Box(contentAlignment = Alignment.TopCenter) {
                        Text(
                            text = "i",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.38f).coerceAtLeast(14f).sp,
                            color = Color(0xFF00E676),
                            letterSpacing = (-0.5).sp
                        )
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier
                                .size((size.value * 0.22f).coerceAtLeast(8f).dp)
                                .padding(bottom = 2.dp)
                        )
                    }
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

/**
 * Full branded banner card displaying the authentic SurakshaSathi logo artwork.
 */
@Composable
fun SurakshaBrandBannerCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val emergencyColors = LocalEmergencyColors.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("suraksha_brand_banner_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFAFB)),
        border = BorderStroke(1.5.dp, Color(0xFF00B0FF).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.suraksha_full_logo),
                contentDescription = "SurakshaSathi Official Logo Artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
