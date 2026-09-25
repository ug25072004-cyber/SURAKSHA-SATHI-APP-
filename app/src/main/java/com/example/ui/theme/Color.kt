package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// SURAKSHASATHI EMERGENCY RESPONSE DESIGN SYSTEM COLOR PALETTE
// Balanced Emergency Quadrant: BLUE (Trust), RED (Urgency), MAGENTA (Escalation), ORANGE (Warning)
// =========================================================================

// --- 1. BLUE: Trust, Authority & Operational Calm ---
val SurakshaBluePrimary = Color(0xFF0D47A1)         // High-authority deep blue (from brand logo)
val SurakshaBlueDark = Color(0xFF072A63)            // Deep navy command background tint
val SurakshaBlueLight = Color(0xFF1976D2)           // Active interactive elements & primary buttons
val SurakshaBlueContainerLight = Color(0xFFE3F2FD)  // Light card fill for calm informational reading
val SurakshaBlueContainerDark = Color(0xFF102A45)   // Dark card fill for stable telemetry
val SurakshaCyanAccent = Color(0xFF00B0FF)          // Telemetry highlight & GIS corridor glow
val SurakshaTealGreen = Color(0xFF00897B)           // Logo mountain & renewal accent

// --- 2. RED: Critical Urgency & Life-Safety Danger ---
val EmergencyRedCritical = Color(0xFFD32F2F)        // Zone 1 Critical priority, SOS button, evacuation orders
val EmergencyRedDark = Color(0xFF9A0007)            // Severe distress / breached boundary
val EmergencyRedPulse = Color(0xFFFF1744)           // Animated beacon pulse for unacknowledged critical events
val EmergencyRedContainerLight = Color(0xFFFFEBEE)  // High-contrast banner surface in light mode
val EmergencyRedContainerDark = Color(0xFF3B0D0D)   // High-contrast banner surface in dark mode
val OnEmergencyRed = Color(0xFFFFFFFF)

// --- 3. MAGENTA: High-Tension Tactical Escalation & Dynamic Re-allocation ---
val TacticalMagenta = Color(0xFFC2185B)             // Dynamic re-allocation trigger & rapid triage shifts
val TacticalMagentaDark = Color(0xFF7A0034)         // High-tension alert border
val TacticalMagentaElectric = Color(0xFFE91E63)     // Re-routing needed, hospital ICU surge
val TacticalMagentaContainerLight = Color(0xFFFCE4EC) // Light escalation badge
val TacticalMagentaContainerDark = Color(0xFF3D091F)  // Dark escalation badge
val OnTacticalMagenta = Color(0xFFFFFFFF)

// --- 4. ORANGE: Caution, Impending Risk & Logistics Scarcity ---
val WarningOrange = Color(0xFFF57C00)               // Zone 2 High priority, route hazard, resource depletion
val WarningOrangeDark = Color(0xFFBC5100)           // Severe weather advisory (cyclonic gale)
val WarningOrangeBright = Color(0xFFFF9800)         // Active warning beacon & pending approvals
val WarningOrangeContainerLight = Color(0xFFFFF3E0) // Light caution card surface
val WarningOrangeContainerDark = Color(0xFF3E1D00)  // Dark caution card surface
val OnWarningOrange = Color(0xFFFFFFFF)

// --- 5. NEUTRALS & SURFACES: Anti-Glare & High-Legibility ---
val BackgroundDark = Color(0xFF0A0F1D)              // Deep tactical anti-glare background
val SurfaceDark = Color(0xFF131B2E)                 // Tonal card surface in dark mode
val SurfaceVariantDark = Color(0xFF1E293B)          // Secondary telemetry container
val SurfaceBorderDark = Color(0xFF334155)           // Crisp card divider
val TextPrimaryDark = Color(0xFFF8FAFC)             // WCAG AAA crisp white text
val TextSecondaryDark = Color(0xFF94A3B8)           // High-legibility muted label text

val BackgroundLight = Color(0xFFF4F7FB)             // High-contrast daytime slate
val SurfaceLight = Color(0xFFFFFFFF)                // Pure card surface
val SurfaceVariantLight = Color(0xFFEDF2F7)         // Light telemetry container
val SurfaceBorderLight = Color(0xFFCBD5E1)          // Light mode border
val TextPrimaryLight = Color(0xFF0F172A)            // Near-black text
val TextSecondaryLight = Color(0xFF475569)          // Muted text

// --- 6. GREEN & UTILITY: Offline Stability & Operational Confirmation ---
val EmergencyGreenStable = Color(0xFF2E7D32)
val EmergencyMagentaAction = TacticalMagenta
val EmergencyOrangeWarning = WarningOrange
