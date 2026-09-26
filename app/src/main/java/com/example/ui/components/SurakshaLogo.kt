package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalEmergencyColors

/**
 * SurakshaSathi Official Brand Logo Component.
 * Faithfully renders the emblem:
 * - Protective Shield in Blue gradient
 * - Digital AI matrix pixels
 * - Sunrise rays with Orange Map Pin
 * - Mountain peaks & flowing river cupped in protective hands
 * - Wordmark: "Suraksha" (Navy) + "Sathi" (Teal) with green leaf on 'i'
 */
@Composable
fun SurakshaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showWordmark: Boolean = true,
    tagline: String? = "Emergency Resource Intelligence"
) {
    val emergencyColors = LocalEmergencyColors.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Official Logo Emblem Container
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.suraksha_sathi_logo),
                contentDescription = "SurakshaSathi Official Emblem",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Suraksha",
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.36f).sp,
                        color = Color(0xFF00B0FF),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Sath",
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.36f).sp,
                        color = Color(0xFF00E676),
                        letterSpacing = (-0.5).sp
                    )
                    // Styled 'i' with leaf accent
                    Box(contentAlignment = Alignment.TopCenter) {
                        Text(
                            text = "i",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.36f).sp,
                            color = Color(0xFF00E676),
                            letterSpacing = (-0.5).sp
                        )
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier
                                .size(10.dp)
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
