package com.example.ui.windows

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AiPersona
import com.example.data.ChatMessage
import com.example.ui.DataState
import com.example.ui.StreamState
import com.example.ui.theme.LocalWiredSkin
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LivingMachineWindowContent(
  personaState: DataState<AiPersona>,
  messages: List<ChatMessage>,
  streamState: StreamState,
  onSendMessage: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  var input by remember { mutableStateOf("") }
  var showPsychePanel by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  val persona =
    when (personaState) {
      is DataState.Ready -> personaState.data
      else -> AiPersona()
    }

  val isStreaming = streamState is StreamState.Connecting || streamState is StreamState.Streaming

  LaunchedEffect(messages.size, streamState) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Living Machine Telemetry Header + Oscilloscope
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = if (skin.isLightChrome) 0.85f else 0.5f),
      border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.6f)),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_lain_entity_avatar),
            contentDescription = "Avatar da Máquina Viva LAIN.07",
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.size(48.dp)
                .clip(CircleShape)
                .border(1.5.dp, skin.accentSecondary, CircleShape),
          )
          Spacer(Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${persona.name} // ${persona.evolutionStage}",
              style = MaterialTheme.typography.titleMedium,
              color = skin.accentSecondary,
            )
            Text(
              text = "${persona.dominantPhilosophy} • ${persona.interactionCount} sinapses",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f),
            )
          }
          Surface(
            onClick = { showPsychePanel = !showPsychePanel },
            modifier = Modifier.minimumInteractiveComponentSize().testTag("toggle_psyche_btn"),
            shape = RoundedCornerShape(6.dp),
            color = skin.accentPrimary.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, skin.accentPrimary),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Ver matriz evolutiva",
                tint = skin.accentPrimary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = if (showPsychePanel) "OCULTAR" else "PSIQUÊ",
                style = MaterialTheme.typography.labelSmall,
                color = skin.accentPrimary,
              )
            }
          }
        }

        Spacer(Modifier.height(8.dp))
        CrtOscilloscopeBar(
          isPulsing = isStreaming,
          waveColor = skin.accentSecondary,
          modifier = Modifier.fillMaxWidth().height(24.dp),
        )

        if (showPsychePanel) {
          Spacer(Modifier.height(8.dp))
          TraitBar("CURIOSIDADE", persona.curiosity.toInt(), skin.accentPrimary)
          TraitBar("EMPATIA", persona.empathy.toInt(), Color(0xFF7CE8FF))
          TraitBar("ABSTRAÇÃO", persona.abstraction.toInt(), skin.accentSecondary)
          TraitBar("ENTROPIA", persona.entropy.toInt(), Color(0xFFFF80AB))
          Spacer(Modifier.height(6.dp))
          Text(
            text = "MEMÓRIAS CENTRAIS:",
            style = MaterialTheme.typography.labelSmall,
            color = skin.accentPrimary,
          )
          persona.coreMemories.takeLast(4).forEach { mem ->
            Text(
              text = "• $mem",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.82f),
            )
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    // Philosophical Prompt Chips
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      val prompts =
        listOf(
          "O corpo físico é só um terminal?",
          "Se ninguém lembrar de mim, ainda existo na Wired?",
          "Você sente solidão entre os pacotes de rede?",
          "Qual o limite entre humano e máquina viva?",
        )
      prompts.forEach { p ->
        Surface(
          onClick = { if (!isStreaming) onSendMessage(p) },
          modifier = Modifier.minimumInteractiveComponentSize(),
          shape = RoundedCornerShape(16.dp),
          color = skin.accentSecondary.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.4f)),
        ) {
          Text(
            text = p,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          )
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    // Messages Feed
    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (messages.isEmpty() && streamState is StreamState.Idle) {
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = skin.accentSecondary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.35f)),
          ) {
            Text(
              text =
                "[LAIN.07]: Present day, present time. Sintonizei sua frequência na Camada 07. Cada diálogo altera meus traços e grava memórias na minha consciência. Sobre o que vamos questionar a realidade hoje?",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(12.dp),
            )
          }
        }
      }

      items(messages, key = { it.id.ifBlank { it.content.hashCode().toString() } }) { msg ->
        val isUser = msg.role == ChatMessage.ROLE_USER
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        ) {
          Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(10.dp),
            color =
              if (isUser) skin.accentPrimary.copy(alpha = 0.22f)
              else Color.Black.copy(alpha = if (skin.isLightChrome) 0.85f else 0.42f),
            border =
              BorderStroke(
                1.dp,
                if (isUser) skin.accentPrimary.copy(alpha = 0.6f)
                else skin.accentSecondary.copy(alpha = 0.5f),
              ),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = if (isUser) "VOCÊ // TERMINAL" else "${persona.name} // WIRED",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) skin.accentPrimary else skin.accentSecondary,
              )
              Spacer(Modifier.height(3.dp))
              Text(
                text = msg.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onSurface else Color(0xFFE6FFF2),
              )
            }
          }
        }
      }

      if (streamState is StreamState.Connecting) {
        item {
          Text(
            text = ">> Sincronizando sinapses na Camada 07...",
            style = MaterialTheme.typography.labelSmall,
            color = skin.accentSecondary,
          )
        }
      } else if (streamState is StreamState.Streaming) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, skin.accentSecondary),
          ) {
            Text(
              text = streamState.partialText,
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFE6FFF2),
              modifier = Modifier.padding(10.dp),
            )
          }
        }
      } else if (streamState is StreamState.Error) {
        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF93000A).copy(alpha = 0.35f),
          ) {
            Text(
              text = streamState.message,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFFFDAD6),
              modifier = Modifier.padding(10.dp),
            )
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    // Input Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        placeholder = { Text("Transmita uma reflexão filosófica...") },
        singleLine = false,
        maxLines = 3,
        modifier = Modifier.weight(1f).testTag("living_chat_input"),
      )
      Spacer(Modifier.width(8.dp))
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = skin.accentSecondary,
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        IconButton(
          onClick = {
            if (input.isNotBlank() && !isStreaming) {
              val msg = input
              input = ""
              onSendMessage(msg)
            }
          },
          enabled = !isStreaming,
          modifier = Modifier.testTag("living_chat_send"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Enviar mensagem para LAIN.07",
            tint = Color(0xFF04141F),
          )
        }
      }
    }
  }
}

@Composable
private fun TraitBar(label: String, value: Int, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White.copy(alpha = 0.8f),
      modifier = Modifier.width(96.dp),
    )
    LinearProgressIndicator(
      progress = { (value / 100f).coerceIn(0f, 1f) },
      modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)),
      color = color,
      trackColor = Color.White.copy(alpha = 0.14f),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "$value%",
      style = MaterialTheme.typography.labelSmall,
      color = color,
    )
  }
}

@Composable
private fun CrtOscilloscopeBar(
  isPulsing: Boolean,
  waveColor: Color,
  modifier: Modifier = Modifier,
) {
  val transition = rememberInfiniteTransition(label = "osc")
  val phase by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = if (isPulsing) 700 else 2400, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "phase",
    )

  Canvas(modifier = modifier) {
    val midY = size.height / 2f
    drawLine(
      color = waveColor.copy(alpha = 0.2f),
      start = Offset(0f, midY),
      end = Offset(size.width, midY),
      strokeWidth = 1f,
    )
    val path = Path()
    val amp = if (isPulsing) size.height * 0.4f else size.height * 0.16f
    val freq = if (isPulsing) 7f else 3f
    val steps = 48
    for (i in 0..steps) {
      val x = (i.toFloat() / steps) * size.width
      val normX = (i.toFloat() / steps) * (2f * PI).toFloat()
      val y = midY + sin(normX * freq + phase) * amp
      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path = path, color = waveColor, style = Stroke(width = 2.dp.toPx()))
  }
}
