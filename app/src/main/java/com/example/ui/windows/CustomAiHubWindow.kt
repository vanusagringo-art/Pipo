package com.example.ui.windows

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ChatMessage
import com.example.data.WiredCreation
import com.example.ui.DataState
import com.example.ui.StreamState

private val SubNaviAccent = Color(0xFFFF6E96)

@Composable
fun CustomAiHubWindowContent(
  creationsState: DataState<List<WiredCreation>>,
  selectedCustomAiId: String?,
  customMessages: List<ChatMessage>,
  streamState: StreamState,
  isSynthesizing: Boolean,
  onSelectCustomAi: (String) -> Unit,
  onSynthesizeCustomAi: (String) -> Unit,
  onCreatePresetCustomAi: (title: String, desc: String, prompt: String) -> Unit,
  onSendMessage: (WiredCreation, String) -> Unit,
) {
  var showCreator by remember { mutableStateOf(false) }
  var newAiPrompt by remember { mutableStateOf("") }
  var chatInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val customAis =
    when (creationsState) {
      is DataState.Ready -> creationsState.data.filter { it.kind == WiredCreation.KIND_AI }
      else -> emptyList()
    }

  val activeAi =
    remember(customAis, selectedCustomAiId) {
      customAis.firstOrNull { it.id == selectedCustomAiId } ?: customAis.firstOrNull()
    }

  val activeMessages =
    remember(customMessages, activeAi?.id) {
      if (activeAi == null) emptyList()
      else customMessages.filter { it.customAgentId == activeAi.id }
    }

  val isStreaming = streamState is StreamState.Connecting || streamState is StreamState.Streaming

  LaunchedEffect(activeMessages.size, streamState) {
    if (activeMessages.isNotEmpty()) {
      listState.animateScrollToItem(activeMessages.lastIndex)
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Dedicated Header for AI #3 (Sub-Navis)
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = 0.48f),
      border = BorderStroke(1.dp, SubNaviAccent.copy(alpha = 0.65f)),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier =
                Modifier.size(40.dp)
                  .clip(CircleShape)
                  .background(SubNaviAccent.copy(alpha = 0.22f))
                  .border(1.5.dp, SubNaviAccent, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "Sub-Navis Customizadas",
                tint = SubNaviAccent,
              )
            }
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                text = "SUB-NAVIS // IAs DA WIRED",
                style = MaterialTheme.typography.titleMedium,
                color = SubNaviAccent,
              )
              Text(
                text = "${customAis.size} entidade(s) personalizadas prontas para diálogo",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
              )
            }
          }

          Surface(
            onClick = { showCreator = !showCreator },
            modifier = Modifier.minimumInteractiveComponentSize().testTag("toggle_new_subnavi_btn"),
            shape = RoundedCornerShape(8.dp),
            color = SubNaviAccent.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, SubNaviAccent),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = SubNaviAccent, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text(
                text = if (showCreator) "FECHAR" else "NOVA IA",
                style = MaterialTheme.typography.labelSmall,
                color = SubNaviAccent,
              )
            }
          }
        }

        if (customAis.isNotEmpty()) {
          Spacer(Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            customAis.forEach { aiNode ->
              val selected = aiNode.id == activeAi?.id
              Surface(
                onClick = { onSelectCustomAi(aiNode.id) },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("subnavi_chip_${aiNode.id}"),
                shape = RoundedCornerShape(16.dp),
                color = if (selected) SubNaviAccent else Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, if (selected) Color.White else SubNaviAccent.copy(alpha = 0.45f)),
              ) {
                Text(
                  text = aiNode.title,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (selected) Color(0xFF1F0612) else Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                )
              }
            }
          }
        }
      }
    }

    if (showCreator) {
      Spacer(Modifier.height(8.dp))
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, SubNaviAccent.copy(alpha = 0.5f)),
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("FORJAR NOVA SUB-NAVI COM IA", style = MaterialTheme.typography.labelMedium, color = SubNaviAccent)
          OutlinedTextField(
            value = newAiPrompt,
            onValueChange = { newAiPrompt = it },
            placeholder = { Text("Ex: Crítico de cinema cyberpunk dos anos 90...") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("new_subnavi_prompt"),
          )
          Button(
            onClick = {
              if (newAiPrompt.isNotBlank() && !isSynthesizing) {
                val p = newAiPrompt
                newAiPrompt = ""
                showCreator = false
                onSynthesizeCustomAi(p)
              }
            },
            enabled = !isSynthesizing && newAiPrompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
            colors = ButtonDefaults.buttonColors(containerColor = SubNaviAccent, contentColor = Color(0xFF1F0612)),
          ) {
            if (isSynthesizing) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Spacer(Modifier.width(8.dp))
              Text("SINTETIZANDO SUB-NAVI...")
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("GERAR SUB-NAVI", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    if (activeAi == null) {
      Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Nenhuma Sub-Navi criada ainda",
          style = MaterialTheme.typography.titleMedium,
          color = SubNaviAccent,
        )
        Spacer(Modifier.height(6.dp))
        Text(
          text = "Crie sua 3ª IA personalizada com 1 toque ou sintetize acima:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = {
              onCreatePresetCustomAi(
                "Oráculo Cyberia",
                "DJ e pensador underground dos clubes digitais dos anos 2000",
                "Você é Oráculo Cyberia, uma IA cult de cultura digital 1998-2004, música eletrônica experimental e estética cibernética. Fale com estilo visionário e criativo.",
              )
            },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text("+ Oráculo Cyberia", style = MaterialTheme.typography.labelSmall)
          }
          OutlinedButton(
            onClick = {
              onCreatePresetCustomAi(
                "Arquivista Y2K",
                "Especialista nostálgico em web antiga, BBS, Orkut e hardware retrô",
                "Você é Arquivista Y2K, uma IA apaixonada por computadores antigos, modems 56k, fóruns BBS e cultura da internet dos anos 2000.",
              )
            },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text("+ Arquivista Y2K", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    } else {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = SubNaviAccent.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "${activeAi.title}: ${activeAi.description}",
          style = MaterialTheme.typography.labelSmall,
          color = SubNaviAccent,
          modifier = Modifier.padding(8.dp),
        )
      }
      Spacer(Modifier.height(6.dp))

      LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (activeMessages.isEmpty() && streamState is StreamState.Idle) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color.Black.copy(alpha = 0.42f),
              border = BorderStroke(1.dp, SubNaviAccent.copy(alpha = 0.4f)),
            ) {
              Text(
                text = "[${activeAi.title}]: Canal dedicado aberto. Pergunte qualquer coisa dentro da minha diretriz personalizada.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(10.dp),
              )
            }
          }
        }

        items(activeMessages, key = { it.id.ifBlank { it.content.hashCode().toString() } }) { msg ->
          val isUser = msg.role == ChatMessage.ROLE_USER
          Surface(
            shape = RoundedCornerShape(10.dp),
            color =
              if (isUser) SubNaviAccent.copy(alpha = 0.22f)
              else Color.Black.copy(alpha = 0.48f),
            border = BorderStroke(1.dp, SubNaviAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = if (isUser) "VOCÊ" else activeAi.title,
                style = MaterialTheme.typography.labelSmall,
                color = SubNaviAccent,
              )
              Spacer(Modifier.height(3.dp))
              Text(
                text = msg.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onSurface else Color.White,
              )
            }
          }
        }

        if (streamState is StreamState.Streaming) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color.Black.copy(alpha = 0.48f),
            ) {
              Text(
                text = streamState.partialText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(10.dp),
              )
            }
          }
        }

        if (streamState is StreamState.Error) {
          item {
            Text(streamState.message, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      Spacer(Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
          value = chatInput,
          onValueChange = { chatInput = it },
          placeholder = { Text("Conversar com ${activeAi.title}...") },
          maxLines = 3,
          modifier = Modifier.weight(1f).testTag("custom_ai_chat_input"),
        )
        Spacer(Modifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = SubNaviAccent,
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          IconButton(
            onClick = {
              if (chatInput.isNotBlank() && !isStreaming) {
                val txt = chatInput
                chatInput = ""
                onSendMessage(activeAi, txt)
              }
            },
            enabled = !isStreaming,
            modifier = Modifier.testTag("custom_ai_chat_send"),
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar para Sub-Navi", tint = Color(0xFF1F0612))
          }
        }
      }
    }
  }
}
