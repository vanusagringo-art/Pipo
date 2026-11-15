package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.UserAppearanceSettings

data class WiredOsSkin(
  val presetId: String,
  val isLightChrome: Boolean,
  val desktopTopColor: Color,
  val desktopBottomColor: Color,
  val windowSurface: Color,
  val windowTitleStart: Color,
  val windowTitleEnd: Color,
  val windowTitleText: Color,
  val windowBorderLight: Color,
  val windowBorderDark: Color,
  val taskbarStart: Color,
  val taskbarEnd: Color,
  val accentPrimary: Color,
  val accentSecondary: Color,
  val crtText: Color,
  val cornerRadius: Dp,
  val glassAlpha: Float,
)

val LocalWiredSkin =
  staticCompositionLocalOf {
    skinForPreset(UserAppearanceSettings.THEME_FRUTIGER_AERO, 0.84f)
  }

fun skinForPreset(preset: String, glassAlpha: Float): WiredOsSkin {
  val alpha = glassAlpha.coerceIn(0.35f, 1.0f)
  return when (preset) {
    UserAppearanceSettings.THEME_WIRED_CRT ->
      WiredOsSkin(
        presetId = preset,
        isLightChrome = false,
        desktopTopColor = Color(0xFF03060D),
        desktopBottomColor = Color(0xFF091421),
        windowSurface = Color(0xFF0A101D),
        windowTitleStart = Color(0xFF0E2936),
        windowTitleEnd = Color(0xFF07151D),
        windowTitleText = WiredPhosphorGreen,
        windowBorderLight = Color(0xFF28726E),
        windowBorderDark = Color(0xFF050A12),
        taskbarStart = Color(0xFF0B1824),
        taskbarEnd = Color(0xFF050A12),
        accentPrimary = WiredPhosphorGreen,
        accentSecondary = WiredCyan,
        crtText = WiredPhosphorGreen,
        cornerRadius = 4.dp,
        glassAlpha = alpha,
      )
    UserAppearanceSettings.THEME_Y2K_PLATINUM ->
      WiredOsSkin(
        presetId = preset,
        isLightChrome = true,
        desktopTopColor = Color(0xFF1F5D99),
        desktopBottomColor = Color(0xFF5FA6D9),
        windowSurface = PlatinumSurface,
        windowTitleStart = PlatinumTitleBlue,
        windowTitleEnd = PlatinumTitleLight,
        windowTitleText = Color.White,
        windowBorderLight = Color.White,
        windowBorderDark = PlatinumDarkBorder,
        taskbarStart = Color(0xFF2257B5),
        taskbarEnd = Color(0xFF153B82),
        accentPrimary = Color(0xFF0B61D1),
        accentSecondary = Color(0xFF10A37F),
        crtText = Color(0xFF12263A),
        cornerRadius = 6.dp,
        glassAlpha = alpha,
      )
    UserAppearanceSettings.THEME_VAPOR_CYBERIA ->
      WiredOsSkin(
        presetId = preset,
        isLightChrome = false,
        desktopTopColor = CyberiaViolet,
        desktopBottomColor = Color(0xFF091B3A),
        windowSurface = CyberiaSurface,
        windowTitleStart = Color(0xFF7B1FA2),
        windowTitleEnd = Color(0xFF007B9E),
        windowTitleText = Color.White,
        windowBorderLight = CyberiaMagenta.copy(alpha = 0.7f),
        windowBorderDark = Color(0xFF0B0416),
        taskbarStart = Color(0xFF280B42),
        taskbarEnd = Color(0xFF0F1330),
        accentPrimary = CyberiaCyan,
        accentSecondary = CyberiaMagenta,
        crtText = CyberiaCyan,
        cornerRadius = 10.dp,
        glassAlpha = alpha,
      )
    else -> // THEME_FRUTIGER_AERO
      WiredOsSkin(
        presetId = UserAppearanceSettings.THEME_FRUTIGER_AERO,
        isLightChrome = false,
        desktopTopColor = AeroSkyTop,
        desktopBottomColor = Color(0xFF062038),
        windowSurface = Color(0xFF0C2740),
        windowTitleStart = Color(0xFF00A8E8),
        windowTitleEnd = Color(0xFF007EA7),
        windowTitleText = Color.White,
        windowBorderLight = Color(0xFF9BE7FF),
        windowBorderDark = Color(0xFF051829),
        taskbarStart = Color(0xFF0B3557),
        taskbarEnd = Color(0xFF061E33),
        accentPrimary = AeroAquaGlow,
        accentSecondary = AeroEmerald,
        crtText = Color(0xFFE0FBFF),
        cornerRadius = 14.dp,
        glassAlpha = alpha,
      )
  }
}

@Composable
fun WiredOsTheme(
  themePreset: String = UserAppearanceSettings.THEME_FRUTIGER_AERO,
  glassAlpha: Float = 0.84f,
  content: @Composable () -> Unit,
) {
  val skin = skinForPreset(themePreset, glassAlpha)
  val colorScheme =
    if (skin.isLightChrome) {
      lightColorScheme(
        primary = skin.accentPrimary,
        onPrimary = Color.White,
        secondary = skin.accentSecondary,
        onSecondary = Color.White,
        background = skin.desktopTopColor,
        onBackground = Color(0xFF101828),
        surface = skin.windowSurface,
        onSurface = Color(0xFF101828),
        surfaceVariant = Color(0xFFDDE3EC),
        onSurfaceVariant = Color(0xFF344054),
        outline = skin.windowBorderDark,
      )
    } else {
      darkColorScheme(
        primary = skin.accentPrimary,
        onPrimary = Color(0xFF02131D),
        secondary = skin.accentSecondary,
        onSecondary = Color(0xFF02131D),
        background = skin.desktopTopColor,
        onBackground = Color(0xFFECFEFF),
        surface = skin.windowSurface,
        onSurface = Color(0xFFECFEFF),
        surfaceVariant = skin.windowTitleStart.copy(alpha = 0.45f),
        onSurfaceVariant = Color(0xFFB5D8E6),
        outline = skin.windowBorderLight.copy(alpha = 0.6f),
      )
    }

  val shapes =
    Shapes(
      small = RoundedCornerShape(skin.cornerRadius * 0.6f),
      medium = RoundedCornerShape(skin.cornerRadius),
      large = RoundedCornerShape(skin.cornerRadius * 1.3f),
    )

  CompositionLocalProvider(LocalWiredSkin provides skin) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = WiredTypography,
      shapes = shapes,
      content = content,
    )
  }
}
