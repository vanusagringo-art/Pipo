package com.example.ui.windows

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.UserAppearanceSettings
import com.example.ui.theme.LocalWiredSkin

@Composable
fun CustomizerWindowContent(
  settings: UserAppearanceSettings,
  onUpdateSettings: ((UserAppearanceSettings) -> UserAppearanceSettings) -> Unit,
  onTestModemAudio: () -> Unit,
  onSignOut: () -> Unit,
) {
  val skin = LocalWiredSkin.current
  var handleDraft by remember(settings.handle) { mutableStateOf(settings.handle) }
  var statusDraft by remember(settings.statusMessage) { mutableStateOf(settings.statusMessage) }

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = "SKINS DO SISTEMA OPERACIONAL",
      style = MaterialTheme.typography.titleMedium,
      color = skin.accentPrimary,
    )

    val presets =
      listOf(
        UserAppearanceSettings.THEME_FRUTIGER_AERO to "Frutiger Aero 2006 (Vidro & Aqua)",
        UserAppearanceSettings.THEME_WIRED_CRT to "Wired 1998 CRT (Fósforo & Obsidiana)",
        UserAppearanceSettings.THEME_Y2K_PLATINUM to "Y2K Platinum XP (Prata & Azul Real)",
        UserAppearanceSettings.THEME_VAPOR_CYBERIA to "Club Cyberia (Neon Magenta & Ciano)",
      )

    presets.forEach { (presetId, label) ->
      val active = settings.themePreset == presetId
      Surface(
        onClick = { onUpdateSettings { it.copy(themePreset = presetId) } },
        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("theme_$presetId"),
        shape = RoundedCornerShape(10.dp),
        color =
          if (active) skin.accentPrimary.copy(alpha = 0.22f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border =
          BorderStroke(
            1.5.dp,
            if (active) skin.accentPrimary else skin.windowBorderLight.copy(alpha = 0.3f),
          ),
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
          )
          if (active) {
            Text("ATIVO", style = MaterialTheme.typography.labelSmall, color = skin.accentSecondary)
          }
        }
      }
    }

    // Visual & Audio Toggles
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
      border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.3f)),
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleSettingRow(
          label = "Linhas de Varredura CRT (Scanlines)",
          checked = settings.scanlinesEnabled,
          onCheckedChange = { checked -> onUpdateSettings { it.copy(scanlinesEnabled = checked) } },
        )
        ToggleSettingRow(
          label = "Grade Cibernética da Wired",
          checked = settings.gridEnabled,
          onCheckedChange = { checked -> onUpdateSettings { it.copy(gridEnabled = checked) } },
        )
        ToggleSettingRow(
          label = "Áudio Modem Discado 56k & SFX",
          checked = settings.soundEnabled,
          onCheckedChange = { checked -> onUpdateSettings { it.copy(soundEnabled = checked) } },
        )

        Text(
          text = "Transparência Aero Glass: ${(settings.glassAlpha * 100).toInt()}%",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
          value = settings.glassAlpha.toFloat(),
          onValueChange = { alpha -> onUpdateSettings { it.copy(glassAlpha = alpha.toDouble()) } },
          valueRange = 0.45f..0.98f,
        )

        OutlinedButton(
          onClick = onTestModemAudio,
          modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
        ) {
          Text("TOCAR HANDSHAKE MODEM 56K", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    // Identity Customization
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
      border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.35f)),
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("IDENTIDADE NA WIRED", style = MaterialTheme.typography.labelMedium, color = skin.accentSecondary)
        OutlinedTextField(
          value = handleDraft,
          onValueChange = { handleDraft = it },
          label = { Text("Codinome / Handle") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = statusDraft,
          onValueChange = { statusDraft = it },
          label = { Text("Mensagem de Status") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Button(
          onClick = {
            onUpdateSettings {
              it.copy(
                handle = handleDraft.ifBlank { "navi_operator" },
                statusMessage = statusDraft.ifBlank { "Present day, present time." },
              )
            }
          },
          modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = skin.accentSecondary,
              contentColor = Color(0xFF04141F),
            ),
        ) {
          Text("SALVAR IDENTIDADE", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    OutlinedButton(
      onClick = onSignOut,
      modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
    ) {
      Text("ENCERRAR SESSÃO GOOGLE", style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Composable
private fun ToggleSettingRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    Spacer(Modifier.width(8.dp))
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
