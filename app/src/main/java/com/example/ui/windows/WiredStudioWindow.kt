package com.example.ui.windows

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.audio.DialUpSoundEngine
import com.example.data.ChatMessage
import com.example.data.WiredCreation
import com.example.ui.DataState
import com.example.ui.StreamState
import com.example.ui.theme.LocalWiredSkin
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun WiredStudioWindowContent(
  creationsState: DataState<List<WiredCreation>>,
  isBusy: Boolean,
  onSynthesizeWithAi: (prompt: String, kind: String) -> Unit,
  onCreateManual: (title: String, kind: String, description: String, spec: String) -> Unit,
  onLaunchCreation: (WiredCreation) -> Unit,
  onShareCreation: (WiredCreation) -> Unit,
  onDeleteCreation: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  var selectedKind by remember { mutableStateOf(WiredCreation.KIND_GAME) }
  var aiPrompt by remember { mutableStateOf("") }
  var showManualForm by remember { mutableStateOf(false) }
  var manualTitle by remember { mutableStateOf("") }
  var manualDesc by remember { mutableStateOf("") }
  var manualSpec by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxSize()) {
    // Creation Type Selector
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KindChip(
        label = "Mini-App",
        icon = Icons.Default.Apps,
        selected = selectedKind == WiredCreation.KIND_APP,
        onClick = { selectedKind = WiredCreation.KIND_APP },
        modifier = Modifier.weight(1f),
      )
      KindChip(
        label = "IA Sub-Navi",
        icon = Icons.Default.SmartToy,
        selected = selectedKind == WiredCreation.KIND_AI,
        onClick = { selectedKind = WiredCreation.KIND_AI },
        modifier = Modifier.weight(1f),
      )
      KindChip(
        label = "Mini-Jogo",
        icon = Icons.Default.Games,
        selected = selectedKind == WiredCreation.KIND_GAME,
        onClick = { selectedKind = WiredCreation.KIND_GAME },
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(Modifier.height(8.dp))

    // Forge / Compiler Box
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = if (skin.isLightChrome) 0.08f else 0.35f),
      border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.45f)),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "FORJA VIRTUAL DA WIRED",
            style = MaterialTheme.typography.labelMedium,
            color = skin.accentPrimary,
          )
          Surface(
            onClick = { showManualForm = !showManualForm },
            modifier = Modifier.minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(6.dp),
            color = skin.accentSecondary.copy(alpha = 0.16f),
          ) {
            Text(
              text = if (showManualForm) "Modo IA" else "Modo Manual",
              style = MaterialTheme.typography.labelSmall,
              color = skin.accentSecondary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            )
          }
        }

        Spacer(Modifier.height(6.dp))

        if (!showManualForm) {
          OutlinedTextField(
            value = aiPrompt,
            onValueChange = { aiPrompt = it },
            placeholder = {
              Text(
                when (selectedKind) {
                  WiredCreation.KIND_AI -> "Ex: Uma IA filosofa cyberpunk especialista em música synth..."
                  WiredCreation.KIND_GAME -> "Ex: Jogo arcade de desviar de pacotes corrompidos na Wired..."
                  else -> "Ex: Terminal Pomodoro CRT com codificador hexadecimal..."
                }
              )
            },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("studio_ai_prompt"),
          )
          Spacer(Modifier.height(8.dp))
          Button(
            onClick = {
              if (aiPrompt.isNotBlank() && !isBusy) {
                val p = aiPrompt
                aiPrompt = ""
                onSynthesizeWithAi(p, selectedKind)
              }
            },
            enabled = !isBusy && aiPrompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("studio_synthesize_btn"),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = skin.accentPrimary,
                contentColor = Color(0xFF04141F),
              ),
          ) {
            if (isBusy) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
              Spacer(Modifier.width(8.dp))
              Text("COMPILANDO NÓ...", style = MaterialTheme.typography.labelMedium)
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("SINTETIZAR COM IA", style = MaterialTheme.typography.labelMedium)
            }
          }
        } else {
          OutlinedTextField(
            value = manualTitle,
            onValueChange = { manualTitle = it },
            label = { Text("Nome da criação") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(6.dp))
          OutlinedTextField(
            value = manualDesc,
            onValueChange = { manualDesc = it },
            label = { Text("Descrição") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(6.dp))
          OutlinedTextField(
            value = manualSpec,
            onValueChange = { manualSpec = it },
            label = {
              Text(
                when (selectedKind) {
                  WiredCreation.KIND_AI -> "Prompt de personalidade da IA"
                  WiredCreation.KIND_GAME -> "Desafios separados por '|' ou 'snake'"
                  else -> "'pomodoro' ou 'cipher'"
                }
              )
            },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(8.dp))
          Button(
            onClick = {
              if (manualTitle.isNotBlank()) {
                onCreateManual(manualTitle, selectedKind, manualDesc, manualSpec)
                manualTitle = ""
                manualDesc = ""
                manualSpec = ""
                showManualForm = false
              }
            },
            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
          ) {
            Text("COMPILAR MANUALMENTE", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    // Quick Starter Blueprints
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      OutlinedButton(
        onClick = {
          onCreateManual(
            "Protocolo Snake 98",
            WiredCreation.KIND_GAME,
            "Arcade CRT clássico: colete pacotes luminosos sem colidir.",
            "snake_arcade",
          )
        },
        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
      ) {
        Text("+ Snake 98", style = MaterialTheme.typography.labelSmall)
      }
      OutlinedButton(
        onClick = {
          onCreateManual(
            "Cifra & Pomodoro CRT",
            WiredCreation.KIND_APP,
            "Timer de foco 25m + codificador hexadecimal.",
            "pomodoro_cipher",
          )
        },
        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
      ) {
        Text("+ App CRT", style = MaterialTheme.typography.labelSmall)
      }
    }

    Spacer(Modifier.height(8.dp))

    // Saved Creations Library
    when (creationsState) {
      is DataState.Loading -> {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is DataState.Error -> {
        Text(creationsState.message, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.labelSmall)
      }
      is DataState.Ready -> {
        val list = creationsState.data
        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (list.isEmpty()) {
            item {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = skin.accentPrimary.copy(alpha = 0.09f),
              ) {
                Text(
                  text = "Nenhum módulo compilado ainda. Use a Forja acima ou um modelo rápido para criar seu primeiro App, IA ou Mini-Jogo na Wired!",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(14.dp),
                )
              }
            }
          }
          items(list, key = { it.id }) { creation ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.35f)),
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${creation.title} [${creation.kind.uppercase()}]",
                      style = MaterialTheme.typography.titleMedium,
                      color = skin.accentPrimary,
                    )
                    Text(
                      text = creation.description.ifBlank { "Módulo interativo da Wired" },
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = "Execuções: ${creation.playCount}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = { onLaunchCreation(creation) },
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("launch_creation_${creation.id}"),
                    colors =
                      ButtonDefaults.buttonColors(
                        containerColor = skin.accentSecondary,
                        contentColor = Color(0xFF04141F),
                      ),
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("EXECUTAR", style = MaterialTheme.typography.labelSmall)
                  }
                  IconButton(
                    onClick = { onShareCreation(creation) },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(
                      Icons.Default.Share,
                      contentDescription = "Compartilhar na WiredNet",
                      tint = skin.accentPrimary,
                    )
                  }
                  IconButton(
                    onClick = { onDeleteCreation(creation.id) },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Excluir criação",
                      tint = Color(0xFFFF8A80),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KindChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skin = LocalWiredSkin.current
  Surface(
    onClick = onClick,
    modifier = modifier.minimumInteractiveComponentSize(),
    shape = RoundedCornerShape(8.dp),
    color = if (selected) skin.accentPrimary else Color.Black.copy(alpha = 0.2f),
    border = BorderStroke(1.dp, if (selected) Color.White else skin.accentPrimary.copy(alpha = 0.4f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) Color(0xFF04141F) else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(15.dp),
      )
      Spacer(Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) Color(0xFF04141F) else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
      )
    }
  }
}

@Composable
fun CreationRunnerContent(
  creation: WiredCreation,
  customMessages: List<ChatMessage>,
  customStreamState: StreamState,
  soundEngine: DialUpSoundEngine,
  onSendCustomAiMessage: (String) -> Unit,
  onBackToStudio: () -> Unit,
) {
  val skin = LocalWiredSkin.current
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onBackToStudio,
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Voltar ao Wired Studio",
          tint = skin.accentPrimary,
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "EXECUTANDO // ${creation.title}",
          style = MaterialTheme.typography.titleMedium,
          color = skin.accentSecondary,
        )
        Text(
          text = creation.description,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
      }
    }

    Spacer(Modifier.height(8.dp))

    when (creation.kind) {
      WiredCreation.KIND_AI -> {
        CustomAiRunner(
          creation = creation,
          messages = customMessages.filter { it.customAgentId == creation.id },
          streamState = customStreamState,
          onSend = onSendCustomAiMessage,
        )
      }
      WiredCreation.KIND_GAME -> {
        RetroGameRunner(creation = creation, soundEngine = soundEngine)
      }
      else -> {
        RetroMiniAppRunner(creation = creation, soundEngine = soundEngine)
      }
    }
  }
}

@Composable
private fun CustomAiRunner(
  creation: WiredCreation,
  messages: List<ChatMessage>,
  streamState: StreamState,
  onSend: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  var input by remember { mutableStateOf("") }
  val isBusy = streamState is StreamState.Connecting || streamState is StreamState.Streaming

  Column(modifier = Modifier.fillMaxSize()) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = skin.accentPrimary.copy(alpha = 0.12f),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = "Diretriz Sub-Navi: ${creation.systemSpec}",
        style = MaterialTheme.typography.labelSmall,
        color = skin.accentPrimary,
        modifier = Modifier.padding(8.dp),
      )
    }
    Spacer(Modifier.height(8.dp))
    LazyColumn(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      items(messages, key = { it.id.ifBlank { it.content.hashCode().toString() } }) { msg ->
        val isUser = msg.role == ChatMessage.ROLE_USER
        Surface(
          shape = RoundedCornerShape(8.dp),
          color =
            if (isUser) skin.accentPrimary.copy(alpha = 0.22f)
            else Color.Black.copy(alpha = 0.45f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "${if (isUser) "VOCÊ" else creation.title}: ${msg.content}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) MaterialTheme.colorScheme.onSurface else Color.White,
            modifier = Modifier.padding(10.dp),
          )
        }
      }
      if (streamState is StreamState.Streaming) {
        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.45f),
          ) {
            Text(
              text = "${creation.title}: ${streamState.partialText}",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White,
              modifier = Modifier.padding(10.dp),
            )
          }
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        placeholder = { Text("Converse com ${creation.title}...") },
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(6.dp))
      IconButton(
        onClick = {
          if (input.isNotBlank() && !isBusy) {
            val txt = input
            input = ""
            onSend(txt)
          }
        },
        enabled = !isBusy,
        modifier = Modifier.minimumInteractiveComponentSize(),
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = skin.accentPrimary)
      }
    }
  }
}

@Composable
private fun RetroGameRunner(creation: WiredCreation, soundEngine: DialUpSoundEngine) {
  val skin = LocalWiredSkin.current
  val challenges =
    remember(creation.systemSpec) {
      creation.systemSpec.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }
  val hasQuizChallenges = challenges.size >= 2

  var modeArcade by remember { mutableStateOf(!hasQuizChallenges) }
  var score by remember { mutableIntStateOf(0) }
  var challengeIndex by remember { mutableIntStateOf(0) }

  // Snake Arcade State
  val gridSize = 12
  var snake by remember { mutableStateOf(listOf(5 to 5, 4 to 5, 3 to 5)) }
  var dir by remember { mutableStateOf(1 to 0) }
  var food by remember { mutableStateOf(8 to 7) }
  var gameOver by remember { mutableStateOf(false) }

  LaunchedEffect(modeArcade, gameOver) {
    if (!modeArcade || gameOver) return@LaunchedEffect
    while (!gameOver) {
      delay(240L)
      val head = snake.first()
      val next =
        ((head.first + dir.first + gridSize) % gridSize) to
          ((head.second + dir.second + gridSize) % gridSize)
      if (snake.contains(next)) {
        gameOver = true
        soundEngine.playWindowMinimize()
      } else if (next == food) {
        snake = listOf(next) + snake
        score += 10
        soundEngine.playCrtBlip()
        food = Random.nextInt(gridSize) to Random.nextInt(gridSize)
      } else {
        snake = listOf(next) + snake.dropLast(1)
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("SCORE: $score", style = MaterialTheme.typography.titleMedium, color = skin.accentSecondary)
      if (hasQuizChallenges) {
        OutlinedButton(
          onClick = { modeArcade = !modeArcade },
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          Text(if (modeArcade) "Ver Desafios" else "Modo Arcade", style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    if (modeArcade) {
      Surface(
        modifier = Modifier.fillMaxWidth(0.78f).aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF040914),
        border = BorderStroke(1.5.dp, skin.accentSecondary),
      ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
          val cellW = size.width / gridSize
          val cellH = size.height / gridSize
          // Food node
          drawRect(
            color = Color(0xFFFF4081),
            topLeft = Offset(food.first * cellW + 2f, food.second * cellH + 2f),
            size = Size(cellW - 4f, cellH - 4f),
          )
          // Snake segments
          snake.forEachIndexed { idx, (gx, gy) ->
            drawRect(
              color = if (idx == 0) skin.accentSecondary else skin.accentPrimary,
              topLeft = Offset(gx * cellW + 1.5f, gy * cellH + 1.5f),
              size = Size(cellW - 3f, cellH - 3f),
            )
          }
        }
      }

      Spacer(Modifier.height(8.dp))

      if (gameOver) {
        Button(
          onClick = {
            snake = listOf(5 to 5, 4 to 5, 3 to 5)
            dir = 1 to 0
            score = 0
            gameOver = false
          },
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          Text("REINICIAR PROTOCOLO", style = MaterialTheme.typography.labelMedium)
        }
      } else {
        // Retro D-Pad
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          IconButton(
            onClick = { if (dir != 0 to 1) dir = 0 to -1 },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Cima", tint = skin.accentPrimary)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            IconButton(
              onClick = { if (dir != 1 to 0) dir = -1 to 0 },
              modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
              Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Esquerda", tint = skin.accentPrimary)
            }
            IconButton(
              onClick = { if (dir != -1 to 0) dir = 1 to 0 },
              modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
              Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Direita", tint = skin.accentPrimary)
            }
          }
          IconButton(
            onClick = { if (dir != 0 to -1) dir = 0 to 1 },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Baixo", tint = skin.accentPrimary)
          }
        }
      }
    } else {
      Surface(
        modifier = Modifier.fillMaxWidth().weight(1f),
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, skin.accentPrimary),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "DESAFIO #${challengeIndex + 1} DE ${challenges.size}",
            style = MaterialTheme.typography.labelMedium,
            color = skin.accentSecondary,
          )
          Spacer(Modifier.height(12.dp))
          Text(
            text = challenges[challengeIndex % challenges.size],
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
          )
          Spacer(Modifier.height(16.dp))
          Button(
            onClick = {
              soundEngine.playWindowOpen()
              score += 25
              challengeIndex = (challengeIndex + 1) % challenges.size
            },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text("CONCLUIR DESAFIO (+25 PTS)", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }
  }
}

@Composable
private fun RetroMiniAppRunner(creation: WiredCreation, soundEngine: DialUpSoundEngine) {
  val skin = LocalWiredSkin.current
  var secondsLeft by remember { mutableIntStateOf(25 * 60) }
  var timerRunning by remember { mutableStateOf(false) }
  var cipherInput by remember { mutableStateOf("PRESENT DAY") }

  LaunchedEffect(timerRunning) {
    while (timerRunning && secondsLeft > 0) {
      delay(1000L)
      secondsLeft -= 1
    }
    if (secondsLeft == 0 && timerRunning) {
      timerRunning = false
      soundEngine.playDialUpHandshake()
    }
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = 0.48f),
      border = BorderStroke(1.dp, skin.accentSecondary),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text("CRONÔMETRO DE FOCO CRT", style = MaterialTheme.typography.labelMedium, color = skin.accentSecondary)
        Spacer(Modifier.height(6.dp))
        val mins = secondsLeft / 60
        val secs = secondsLeft % 60
        Text(
          text = "%02d:%02d".format(mins, secs),
          style = MaterialTheme.typography.displayLarge,
          color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              soundEngine.playRetroClick()
              timerRunning = !timerRunning
            },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text(if (timerRunning) "PAUSAR" else "INICIAR")
          }
          OutlinedButton(
            onClick = {
              soundEngine.playRetroClick()
              timerRunning = false
              secondsLeft = 25 * 60
            },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text("RESET 25m")
          }
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = 0.42f),
      border = BorderStroke(1.dp, skin.accentPrimary),
    ) {
      Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
        Text("CODIFICADOR HEX // ${creation.title}", style = MaterialTheme.typography.labelMedium, color = skin.accentPrimary)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
          value = cipherInput,
          onValueChange = { cipherInput = it },
          label = { Text("Texto fonte") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        val hexOut =
          cipherInput.toByteArray().joinToString(" ") { b -> "%02X".format(b) }
        Text(
          text = "HEX: $hexOut",
          style = MaterialTheme.typography.labelSmall,
          color = skin.accentSecondary,
        )
      }
    }
  }
}
