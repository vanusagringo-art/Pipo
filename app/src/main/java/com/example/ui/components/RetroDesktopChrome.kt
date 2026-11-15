package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserAppearanceSettings
import com.example.ui.DesktopWindowId
import com.example.ui.theme.LocalWiredSkin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun WiredDesktopBackdrop(
  settings: UserAppearanceSettings,
  modifier: Modifier = Modifier,
) {
  val skin = LocalWiredSkin.current
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(skin.desktopTopColor, skin.desktopBottomColor)
          )
        )
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_wired_aero_bg),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.fillMaxSize()
          .alpha(
            when (settings.themePreset) {
              UserAppearanceSettings.THEME_WIRED_CRT -> 0.24f
              UserAppearanceSettings.THEME_VAPOR_CYBERIA -> 0.36f
              else -> 0.58f
            }
          ),
    )

    // Theme tint veil
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                skin.desktopTopColor.copy(alpha = 0.45f),
                skin.desktopBottomColor.copy(alpha = 0.65f),
              )
            )
          )
    )

    if (settings.gridEnabled) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 42.dp.toPx()
        val lineColor = skin.accentPrimary.copy(alpha = 0.08f)
        var x = 0f
        while (x < size.width) {
          drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
          x += step
        }
        var y = 0f
        while (y < size.height) {
          drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
          y += step
        }
      }
    }

    if (settings.scanlinesEnabled) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 4.dp.toPx()
        val scanColor = Color.Black.copy(alpha = 0.14f)
        var y = 0f
        while (y < size.height) {
          drawLine(scanColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
          y += step
        }
      }
    }
  }
}

@Composable
fun RetroWindowFrame(
  title: String,
  badge: String,
  statusFooter: String,
  onMinimize: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val skin = LocalWiredSkin.current
  val shape = RoundedCornerShape(skin.cornerRadius)

  Surface(
    modifier =
      modifier
        .fillMaxSize()
        .widthIn(max = 760.dp)
        .border(1.5.dp, skin.windowBorderLight.copy(alpha = 0.8f), shape),
    shape = shape,
    color = skin.windowSurface.copy(alpha = skin.glassAlpha),
    tonalElevation = 8.dp,
    shadowElevation = 14.dp,
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Glossy 2000s / Frutiger Aero Titlebar
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(
              Brush.verticalGradient(
                listOf(
                  skin.windowTitleStart.copy(alpha = 0.96f),
                  skin.windowTitleEnd.copy(alpha = 0.96f),
                )
              )
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier.size(10.dp)
              .clip(CircleShape)
              .background(skin.accentSecondary)
              .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = skin.windowTitleText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color.Black.copy(alpha = 0.28f),
          border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.4f)),
        ) {
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = skin.accentPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
        Spacer(Modifier.width(6.dp))
        WindowChromeButton(
          icon = Icons.Default.Minimize,
          contentDescription = "Minimizar janela",
          tint = skin.windowTitleText,
          onClick = onMinimize,
          testTag = "window_minimize_btn",
        )
        Spacer(Modifier.width(4.dp))
        WindowChromeButton(
          icon = Icons.Default.Close,
          contentDescription = "Fechar janela",
          tint = Color(0xFFFFB4AB),
          bgTint = Color(0xFF8C1D18).copy(alpha = 0.6f),
          onClick = onClose,
          testTag = "window_close_btn",
        )
      }

      // Glossy highlight strip (Frutiger Aero signature)
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(2.dp)
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color.Transparent,
                  Color.White.copy(alpha = 0.45f),
                  skin.accentPrimary.copy(alpha = 0.6f),
                  Color.Transparent,
                )
              )
            )
      )

      // Window Body
      Column(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
        content = content,
      )

      // Classic 2000s Status Bar
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(Color.Black.copy(alpha = if (skin.isLightChrome) 0.08f else 0.35f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = statusFooter,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Text(
          text = "CAMADA 07 // PROTOCOLO WIRED",
          style = MaterialTheme.typography.labelSmall,
          color = skin.accentPrimary.copy(alpha = 0.8f),
        )
      }
    }
  }
}

@Composable
private fun WindowChromeButton(
  icon: ImageVector,
  contentDescription: String,
  tint: Color,
  onClick: () -> Unit,
  testTag: String,
  bgTint: Color = Color.White.copy(alpha = 0.14f),
) {
  Surface(
    onClick = onClick,
    modifier = Modifier.minimumInteractiveComponentSize().size(32.dp).testTag(testTag),
    shape = RoundedCornerShape(4.dp),
    color = bgTint,
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@Composable
fun DesktopIconShortcut(
  label: String,
  sublabel: String,
  icon: ImageVector,
  accent: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skin = LocalWiredSkin.current
  Surface(
    onClick = onClick,
    modifier = modifier.minimumInteractiveComponentSize().testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = skin.windowSurface.copy(alpha = 0.56f),
    border = BorderStroke(1.dp, skin.windowBorderLight.copy(alpha = 0.45f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier =
          Modifier.size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              Brush.verticalGradient(
                listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.4f))
              )
            )
            .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = Color.White,
          modifier = Modifier.size(26.dp),
        )
      }
      Spacer(Modifier.height(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = sublabel,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.72f),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun RetroTaskbar(
  openWindows: List<DesktopWindowId>,
  activeWindow: DesktopWindowId?,
  isStartMenuOpen: Boolean,
  isSettingsOpen: Boolean,
  modemStatus: String,
  soundEnabled: Boolean,
  onToggleStart: () -> Unit,
  onSelectWindow: (DesktopWindowId) -> Unit,
  onModemClick: () -> Unit,
  onToggleSound: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skin = LocalWiredSkin.current
  var clockText by remember { mutableStateOf("00:00") }
  LaunchedEffect(Unit) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    while (true) {
      clockText = fmt.format(Date())
      delay(15_000L)
    }
  }

  Surface(
    modifier = modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
    color = Color.Transparent,
    shadowElevation = 12.dp,
  ) {
    Column {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(1.5.dp)
            .background(skin.windowBorderLight.copy(alpha = 0.75f))
      )
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(
              Brush.verticalGradient(listOf(skin.taskbarStart, skin.taskbarEnd))
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Start / NAVI Orb Button
        Surface(
          onClick = onToggleStart,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("taskbar_start_button"),
          shape = RoundedCornerShape(8.dp),
          color =
            if (isStartMenuOpen) skin.accentSecondary
            else skin.accentPrimary,
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.Window,
              contentDescription = "Menu Iniciar",
              tint = Color(0xFF04141F),
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
              text = "NAVI",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF04141F),
            )
          }
        }

        Spacer(Modifier.width(6.dp))

        // Open Windows Strip
        Row(
          modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          openWindows.forEach { win ->
            val isSelected = win == activeWindow
            Surface(
              onClick = { onSelectWindow(win) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("taskbar_win_${win.name}"),
              shape = RoundedCornerShape(6.dp),
              color =
                if (isSelected) skin.accentPrimary.copy(alpha = 0.28f)
                else Color.White.copy(alpha = 0.08f),
              border =
                BorderStroke(
                  1.dp,
                  if (isSelected) skin.accentPrimary else Color.White.copy(alpha = 0.25f),
                ),
            ) {
              Text(
                text = win.badge,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) skin.accentPrimary else Color.White,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
              )
            }
          }
        }

        Spacer(Modifier.width(6.dp))

        // System Tray: Dial-Up Modem + Sound + Clock
        Surface(
          onClick = onModemClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("taskbar_modem_button"),
          shape = RoundedCornerShape(6.dp),
          color = Color.Black.copy(alpha = 0.3f),
          border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.SettingsEthernet,
              contentDescription = "Discar modem 56k",
              tint =
                if (modemStatus.contains("DISCANDO")) Color(0xFFFFD54F)
                else skin.accentSecondary,
              modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
              text = if (modemStatus.contains("DISCANDO")) "DIAL" else "56k",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
            )
          }
        }

        Spacer(Modifier.width(4.dp))

        Surface(
          onClick = onToggleSound,
          modifier = Modifier.minimumInteractiveComponentSize().size(34.dp).testTag("taskbar_sound_button"),
          shape = RoundedCornerShape(6.dp),
          color = Color.Black.copy(alpha = 0.3f),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector =
                if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
              contentDescription = if (soundEnabled) "Silenciar áudio" else "Ativar áudio",
              tint = if (soundEnabled) skin.accentPrimary else Color.Gray,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Spacer(Modifier.width(4.dp))

        Surface(
          onClick = onOpenSettings,
          modifier = Modifier.minimumInteractiveComponentSize().size(34.dp).testTag("taskbar_settings_button"),
          shape = RoundedCornerShape(6.dp),
          color =
            if (isSettingsOpen) skin.accentPrimary.copy(alpha = 0.28f)
            else Color.Black.copy(alpha = 0.3f),
          border =
            BorderStroke(
              1.dp,
              if (isSettingsOpen) skin.accentPrimary else Color.White.copy(alpha = 0.25f),
            ),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Abrir Configurações",
              tint = if (isSettingsOpen) skin.accentPrimary else Color.White,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Spacer(Modifier.width(6.dp))

        Text(
          text = clockText,
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.9f),
        )
      }
    }
  }
}

@Composable
fun RetroStartMenuPopup(
  visible: Boolean,
  userHandle: String,
  statusMessage: String,
  onOpenWindow: (DesktopWindowId) -> Unit,
  onModemReconnect: () -> Unit,
  onSignOut: () -> Unit,
  onDismiss: () -> Unit,
) {
  val skin = LocalWiredSkin.current
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + scaleIn(initialScale = 0.94f),
    exit = fadeOut() + scaleOut(targetScale = 0.94f),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .clickable(onClick = onDismiss)
          .padding(start = 10.dp, bottom = 68.dp, end = 10.dp),
      contentAlignment = Alignment.BottomStart,
    ) {
      Surface(
        modifier = Modifier.width(276.dp).clickable(enabled = false) {},
        shape = RoundedCornerShape(12.dp),
        color = skin.windowSurface.copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, skin.accentPrimary),
        shadowElevation = 18.dp,
      ) {
        Column {
          // User Header Banner
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .background(
                  Brush.horizontalGradient(listOf(skin.windowTitleStart, skin.windowTitleEnd))
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_lain_entity_avatar),
              contentDescription = "Avatar NAVI",
              contentScale = ContentScale.Crop,
              modifier =
                Modifier.size(38.dp)
                  .clip(CircleShape)
                  .border(1.5.dp, Color.White, CircleShape),
            )
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                text = userHandle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = statusMessage,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Column(
            modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            DesktopWindowId.entries.forEach { win ->
              Surface(
                onClick = { onOpenWindow(win) },
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("start_item_${win.name}"),
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = win.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                  )
                  Text(
                    text = win.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = skin.accentPrimary,
                  )
                }
              }
            }

            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .height(1.dp)
                  .background(skin.windowBorderLight.copy(alpha = 0.25f))
            )

            Surface(
              onClick = {
                onDismiss()
                onModemReconnect()
              },
              modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(8.dp),
              color = skin.accentSecondary.copy(alpha = 0.14f),
            ) {
              Text(
                text = "Reconectar Modem 56k (Tocar Handshake)",
                style = MaterialTheme.typography.labelSmall,
                color = skin.accentSecondary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
              )
            }

            Surface(
              onClick = onSignOut,
              modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("start_signout_button"),
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF93000A).copy(alpha = 0.25f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Logout,
                  contentDescription = "Sair da conta",
                  tint = Color(0xFFFFB4AB),
                  modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                  text = "Desconectar da Wired",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFFFB4AB),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun WiredTopStatusRibbon(
  handle: String,
  modemStatus: String,
  personaStage: String,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skin = LocalWiredSkin.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.5f)),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(skin.accentSecondary)
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = "$handle // $modemStatus",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    Surface(
      modifier = Modifier.widthIn(max = 140.dp),
      shape = RoundedCornerShape(20.dp),
      color = Color.Black.copy(alpha = 0.45f),
      border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.5f)),
    ) {
      Text(
        text = "LAIN.07: $personaStage",
        style = MaterialTheme.typography.labelSmall,
        color = skin.accentSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      )
    }

    Surface(
      onClick = onOpenSettings,
      modifier = Modifier.minimumInteractiveComponentSize().testTag("ribbon_settings_button"),
      shape = RoundedCornerShape(20.dp),
      color = Color.Black.copy(alpha = 0.55f),
      border = BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.7f)),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Configurações",
          tint = Color(0xFFFFC107),
          modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
          text = "Config",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFFFFC107),
        )
      }
    }
  }
}

@Composable
fun AiQuickSwitcherBar(
  activeWindow: DesktopWindowId?,
  hasActiveRunner: Boolean,
  isLivingStreaming: Boolean,
  isMentorStreaming: Boolean,
  isCustomAiStreaming: Boolean,
  onSelectAiWindow: (DesktopWindowId) -> Unit,
  modifier: Modifier = Modifier,
) {
  data class AiChannelPill(
    val windowId: DesktopWindowId,
    val label: String,
    val roleTag: String,
    val accent: Color,
    val isBusy: Boolean,
  )

  val channels =
    listOf(
      AiChannelPill(
        windowId = DesktopWindowId.LIVING_MACHINE,
        label = "1. LAIN.07",
        roleTag = "Máquina Viva",
        accent = Color(0xFF00E5A3),
        isBusy = isLivingStreaming,
      ),
      AiChannelPill(
        windowId = DesktopWindowId.MENTOR_WORKFLOW,
        label = "2. NAVI Mentor",
        roleTag = "Fluxo & Q&A",
        accent = Color(0xFF00D2FF),
        isBusy = isMentorStreaming,
      ),
      AiChannelPill(
        windowId = DesktopWindowId.CUSTOM_AIS,
        label = "3. Sub-Navis",
        roleTag = "IAs Criadas",
        accent = Color(0xFFFF6E96),
        isBusy = isCustomAiStreaming,
      ),
    )

  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 3.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    channels.forEach { channel ->
      val isSelected = !hasActiveRunner && activeWindow == channel.windowId
      Surface(
        onClick = { onSelectAiWindow(channel.windowId) },
        modifier =
          Modifier.weight(1f)
            .minimumInteractiveComponentSize()
            .testTag("ai_switcher_${channel.windowId.name}"),
        shape = RoundedCornerShape(10.dp),
        color =
          if (isSelected) channel.accent.copy(alpha = 0.24f)
          else Color.Black.copy(alpha = 0.45f),
        border =
          BorderStroke(
            width = if (isSelected) 1.6.dp else 1.dp,
            color = if (isSelected) channel.accent else channel.accent.copy(alpha = 0.38f),
          ),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(8.dp)
                .clip(CircleShape)
                .background(if (channel.isBusy) Color(0xFFFFD54F) else channel.accent)
          )
          Spacer(Modifier.width(6.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = channel.label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isSelected) channel.accent else Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = if (channel.isBusy) "Transmitindo..." else channel.roleTag,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = Color.White.copy(alpha = 0.72f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}
