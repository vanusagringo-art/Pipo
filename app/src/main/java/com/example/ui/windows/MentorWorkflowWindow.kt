package com.example.ui.windows

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChatMessage
import com.example.data.WorkflowTask
import com.example.ui.DataState
import com.example.ui.StreamState
import com.example.ui.theme.LocalWiredSkin

@Composable
fun MentorWorkflowWindowContent(
  tasksState: DataState<List<WorkflowTask>>,
  mentorMessages: List<ChatMessage>,
  mentorStreamState: StreamState,
  isOrganizing: Boolean,
  onSendMentorMessage: (String) -> Unit,
  onOrganizeWorkflowWithAi: (String) -> Unit,
  onAddManualTask: (title: String, notes: String, priority: String, focusMinutes: Long) -> Unit,
  onAdvanceTaskStatus: (WorkflowTask) -> Unit,
  onDeleteTask: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  var selectedTab by remember { mutableIntStateOf(0) }

  Column(modifier = Modifier.fillMaxSize()) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.Transparent,
      contentColor = skin.accentPrimary,
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("MENTOR ANALÍTICO", style = MaterialTheme.typography.labelMedium) },
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("FLUXO DE TRABALHO", style = MaterialTheme.typography.labelMedium) },
      )
    }

    Spacer(Modifier.height(8.dp))

    if (selectedTab == 0) {
      MentorChatTab(
        messages = mentorMessages,
        streamState = mentorStreamState,
        onSend = onSendMentorMessage,
      )
    } else {
      WorkflowBoardTab(
        tasksState = tasksState,
        isOrganizing = isOrganizing,
        onOrganizeWithAi = onOrganizeWorkflowWithAi,
        onAddManualTask = onAddManualTask,
        onAdvanceStatus = onAdvanceTaskStatus,
        onDeleteTask = onDeleteTask,
      )
    }
  }
}

@Composable
private fun MentorChatTab(
  messages: List<ChatMessage>,
  streamState: StreamState,
  onSend: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  var input by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val isStreaming = streamState is StreamState.Connecting || streamState is StreamState.Streaming

  LaunchedEffect(messages.size, streamState) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = if (skin.isLightChrome) 0.85f else 0.42f),
      border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.5f)),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_mentor_navi_avatar),
          contentDescription = "Avatar NAVI Mentor",
          contentScale = ContentScale.Crop,
          modifier =
            Modifier.size(44.dp)
              .clip(CircleShape)
              .border(1.5.dp, skin.accentPrimary, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Column {
          Text("NAVI MENTOR // ARQUITETO DE FLUXO", style = MaterialTheme.typography.titleMedium, color = skin.accentPrimary)
          Text(
            "Perguntas complexas, estratégia técnica e mentoria integrada às suas tarefas.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
          )
        }
      }
    }

    Spacer(Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      listOf(
        "Como priorizar meu fluxo de estudos hoje?",
        "Explique computação quântica com clareza",
        "Monte uma arquitetura limpa para meu projeto",
      ).forEach { q ->
        Surface(
          onClick = { if (!isStreaming) onSend(q) },
          modifier = Modifier.minimumInteractiveComponentSize(),
          shape = RoundedCornerShape(14.dp),
          color = skin.accentPrimary.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.4f)),
        ) {
          Text(
            text = q,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          )
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (messages.isEmpty() && streamState is StreamState.Idle) {
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = skin.accentPrimary.copy(alpha = 0.1f),
          ) {
            Text(
              text = "Olá! Sou o NAVI Mentor. Envie qualquer dúvida complexa (ciência, código, carreira, filosofia prática) ou peça conselhos baseados no seu quadro de fluxo.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(12.dp),
            )
          }
        }
      }
      items(messages, key = { it.id.ifBlank { it.content.hashCode().toString() } }) { msg ->
        val isUser = msg.role == ChatMessage.ROLE_USER
        Surface(
          shape = RoundedCornerShape(10.dp),
          color =
            if (isUser) skin.accentPrimary.copy(alpha = 0.2f)
            else Color.Black.copy(alpha = if (skin.isLightChrome) 0.85f else 0.45f),
          border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = if (isUser) "VOCÊ" else "NAVI MENTOR",
              style = MaterialTheme.typography.labelSmall,
              color = skin.accentPrimary,
            )
            Spacer(Modifier.height(4.dp))
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
        value = input,
        onValueChange = { input = it },
        placeholder = { Text("Faça uma pergunta complexa ao Mentor...") },
        maxLines = 3,
        modifier = Modifier.weight(1f).testTag("mentor_chat_input"),
      )
      Spacer(Modifier.width(8.dp))
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = skin.accentPrimary,
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        IconButton(
          onClick = {
            if (input.isNotBlank() && !isStreaming) {
              val msg = input
              input = ""
              onSend(msg)
            }
          },
          enabled = !isStreaming,
          modifier = Modifier.testTag("mentor_chat_send"),
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar ao Mentor", tint = Color(0xFF04141F))
        }
      }
    }
  }
}

@Composable
private fun WorkflowBoardTab(
  tasksState: DataState<List<WorkflowTask>>,
  isOrganizing: Boolean,
  onOrganizeWithAi: (String) -> Unit,
  onAddManualTask: (String, String, String, Long) -> Unit,
  onAdvanceStatus: (WorkflowTask) -> Unit,
  onDeleteTask: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  var goalInput by remember { mutableStateOf("") }
  var manualTitle by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxSize()) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = if (skin.isLightChrome) 0.07f else 0.35f),
      border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.5f)),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Text("PLANEJADOR DE FLUXO COM IA", style = MaterialTheme.typography.labelMedium, color = skin.accentSecondary)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
          value = goalInput,
          onValueChange = { goalInput = it },
          placeholder = { Text("Ex: Lançar meu portfólio e estudar Kotlin Coroutines...") },
          maxLines = 2,
          modifier = Modifier.fillMaxWidth().testTag("mentor_goal_input"),
        )
        Spacer(Modifier.height(6.dp))
        Button(
          onClick = {
            if (goalInput.isNotBlank() && !isOrganizing) {
              val g = goalInput
              goalInput = ""
              onOrganizeWithAi(g)
            }
          },
          enabled = !isOrganizing && goalInput.isNotBlank(),
          modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("mentor_organize_btn"),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = skin.accentSecondary,
              contentColor = Color(0xFF04141F),
            ),
        ) {
          if (isOrganizing) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("ESTRUTURANDO FLUXO...", style = MaterialTheme.typography.labelMedium)
          } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("ORGANIZAR FLUXO COM MENTOR IA", style = MaterialTheme.typography.labelMedium)
          }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = manualTitle,
            onValueChange = { manualTitle = it },
            placeholder = { Text("Adicionar tarefa rápida...") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(6.dp))
          OutlinedButton(
            onClick = {
              if (manualTitle.isNotBlank()) {
                onAddManualTask(manualTitle, "Criada manualmente no WiredOS", WorkflowTask.PRIORITY_MEDIUM, 25L)
                manualTitle = ""
              }
            },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(Icons.Default.AddTask, contentDescription = "Adicionar tarefa")
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    when (tasksState) {
      is DataState.Loading -> {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is DataState.Error -> {
        Text(tasksState.message, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.labelSmall)
      }
      is DataState.Ready -> {
        val tasks = tasksState.data
        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (tasks.isEmpty()) {
            item {
              Text(
                "Seu quadro de fluxo está vazio. Descreva um objetivo acima para a IA Mentora quebrar em passos acionáveis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          items(tasks, key = { it.id }) { task ->
            val statusIcon =
              when (task.status) {
                WorkflowTask.STATUS_DONE -> Icons.Default.CheckCircle
                WorkflowTask.STATUS_IN_PROGRESS -> Icons.Default.PlayCircleOutline
                else -> Icons.Default.RadioButtonUnchecked
              }
            val statusLabel =
              when (task.status) {
                WorkflowTask.STATUS_DONE -> "CONCLUÍDO"
                WorkflowTask.STATUS_IN_PROGRESS -> "EM PROGRESSO"
                else -> "A FAZER"
              }
            val statusColor =
              when (task.status) {
                WorkflowTask.STATUS_DONE -> skin.accentSecondary
                WorkflowTask.STATUS_IN_PROGRESS -> skin.accentPrimary
                else -> Color(0xFFFFCA28)
              }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                IconButton(
                  onClick = { onAdvanceStatus(task) },
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Icon(statusIcon, contentDescription = "Avançar estágio", tint = statusColor)
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  if (task.notes.isNotBlank()) {
                    Text(
                      text = task.notes,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Text(
                    text = "$statusLabel • PRIORIDADE: ${task.priority.uppercase()} • ${task.focusMinutes}min",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                  )
                }
                IconButton(
                  onClick = { onDeleteTask(task.id) },
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Remover tarefa", tint = Color(0xFFFF8A80))
                }
              }
            }
          }
        }
      }
    }
  }
}
