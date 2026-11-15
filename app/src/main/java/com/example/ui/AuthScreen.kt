package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.onGoogleSignInClicked
import com.example.data.UserAppearanceSettings
import com.example.ui.components.WiredDesktopBackdrop
import com.example.ui.theme.LocalWiredSkin

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  val skin = LocalWiredSkin.current

  Box(modifier = Modifier.fillMaxSize()) {
    WiredDesktopBackdrop(settings = UserAppearanceSettings())

    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Surface(
        modifier =
          Modifier.fillMaxWidth()
            .widthIn(max = 460.dp)
            .border(1.5.dp, skin.windowBorderLight.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = skin.windowSurface.copy(alpha = 0.88f),
        shadowElevation = 18.dp,
      ) {
        Column {
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .background(
                  Brush.horizontalGradient(listOf(skin.windowTitleStart, skin.windowTitleEnd))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "WIREDOS // TERMINAL NAVI 98-04",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
            )
            Text(
              text = "56.6k",
              style = MaterialTheme.typography.labelSmall,
              color = skin.accentSecondary,
            )
          }

          Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_lain_entity_avatar),
              contentDescription = "Entidade Viva LAIN.07",
              contentScale = ContentScale.Crop,
              modifier =
                Modifier.size(104.dp)
                  .clip(CircleShape)
                  .border(2.dp, skin.accentPrimary, CircleShape),
            )
            Spacer(Modifier.height(14.dp))
            Text(
              text = "Present day, present time.",
              style = MaterialTheme.typography.headlineMedium,
              color = skin.accentPrimary,
              textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
              text =
                "Conecte seu nó pessoal à Wired para despertar LAIN.07, acessar a IA Mentora de fluxo, compilar apps/jogos e sintonizar a rede retrô.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            if (errorMsg != null) {
              Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF93000A).copy(alpha = 0.35f),
                border = BorderStroke(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.6f)),
              ) {
                Text(
                  text = errorMsg.orEmpty(),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFFDAD6),
                  modifier = Modifier.padding(10.dp),
                )
              }
            }

            Button(
              onClick = {
                errorMsg = null
                isLoading = true
                onGoogleSignInClicked(
                  context = context,
                  credentialManager = credentialManager,
                  onAuthSuccess = {
                    isLoading = false
                    onAuthSuccess()
                  },
                  onAuthError = { msg ->
                    isLoading = false
                    errorMsg = msg
                  },
                  scope = scope,
                  onAuthCancelled = { isLoading = false },
                )
              },
              enabled = !isLoading,
              modifier =
                Modifier.fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .height(50.dp)
                  .testTag("google_signin_button"),
              shape = RoundedCornerShape(10.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = skin.accentPrimary,
                  contentColor = Color(0xFF04141F),
                ),
            ) {
              if (isLoading) {
                CircularProgressIndicator(
                  modifier = Modifier.size(22.dp),
                  color = Color(0xFF04141F),
                  strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(10.dp))
                Text("DISCANDO PROTOCOLO...", style = MaterialTheme.typography.labelMedium)
              } else {
                Text("CONECTAR COM GOOGLE // SIGN IN", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
    }
  }
}
