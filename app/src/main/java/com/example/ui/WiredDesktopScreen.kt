package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.signOutUser
import com.example.data.AiPersona
import com.example.data.WiredCreation
import com.example.ui.components.AiQuickSwitcherBar
import com.example.ui.components.DesktopIconShortcut
import com.example.ui.components.RetroStartMenuPopup
import com.example.ui.components.RetroTaskbar
import com.example.ui.components.RetroWindowFrame
import com.example.ui.components.WiredDesktopBackdrop
import com.example.ui.components.WiredTopStatusRibbon
import com.example.ui.theme.LocalWiredSkin
import com.example.ui.theme.WiredOsTheme
import com.example.ui.windows.CreationRunnerContent
import com.example.ui.windows.CustomAiHubWindowContent
import com.example.ui.windows.CustomizerWindowContent
import com.example.ui.windows.LivingMachineWindowContent
import com.example.ui.windows.MentorWorkflowWindowContent
import com.example.ui.windows.RetroSocialWindowContent
import com.example.ui.windows.WiredStudioWindowContent
import kotlinx.coroutines.delay

@Composable
fun WiredDesktopScreen(
  viewModel: WiredViewModel,
  livingAiViewModel: LivingAIViewModel,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val settings by viewModel.appearanceSettings.collectAsStateWithLifecycle()
  val personaState by livingAiViewModel.personaState.collectAsStateWithLifecycle()
  val livingMessages by livingAiViewModel.conversationHistory.collectAsStateWithLifecycle()
  val livingStreamState by livingAiViewModel.streamState.collectAsStateWithLifecycle()
  val mentorMessages by viewModel.mentorMessages.collectAsStateWithLifecycle()
  val mentorStreamState by viewModel.mentorStreamState.collectAsStateWithLifecycle()
  val customAiMessages by viewModel.customAiMessages.collectAsStateWithLifecycle()
  val customAiStreamState by viewModel.customAiStreamState.collectAsStateWithLifecycle()

  val creations by viewModel.creations.collectAsStateWithLifecycle()
  val selectedCustomAiId by viewModel.selectedCustomAiId.collectAsStateWithLifecycle()
  val workflowTasks by viewModel.workflowTasks.collectAsStateWithLifecycle()
  val socialPosts by viewModel.socialPosts.collectAsStateWithLifecycle()

  val activeWindow by viewModel.activeWindow.collectAsStateWithLifecycle()
  val openWindows by viewModel.openWindows.collectAsStateWithLifecycle()
  val isStartMenuOpen by viewModel.isStartMenuOpen.collectAsStateWithLifecycle()
  val activeCreationRunner by viewModel.activeCreationRunner.collectAsStateWithLifecycle()
  val modemStatus by viewModel.modemStatus.collectAsStateWithLifecycle()
  val studioBusy by viewModel.studioBusy.collectAsStateWithLifecycle()
  val mentorOrganizing by viewModel.mentorOrganizing.collectAsStateWithLifecycle()
  val socialBusy by viewModel.socialBusy.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

  LaunchedEffect(toastMessage) {
    if (toastMessage != null) {
      delay(3500L)
      viewModel.clearToast()
    }
  }

  // Handle system back cleanly across Start Menu, Creation Runner, and Active Window
  BackHandler(enabled = isStartMenuOpen || activeCreationRunner != null || activeWindow != null) {
    when {
      isStartMenuOpen -> viewModel.dismissStartMenu()
      activeCreationRunner != null -> viewModel.closeCreationRunner()
      activeWindow != null -> viewModel.minimizeActiveWindow()
    }
  }

  val personaStage =
    when (val s = personaState) {
      is DataState.Ready -> s.data.evolutionStage
      else -> AiPersona().evolutionStage
    }

  val performSignOut: () -> Unit = {
    viewModel.dismissStartMenu()
    signOutUser(credentialManager = credentialManager, scope = scope)
  }

  WiredOsTheme(
    themePreset = settings.themePreset,
    glassAlpha = settings.glassAlpha.toFloat(),
  ) {
    val skin = LocalWiredSkin.current

    Box(modifier = Modifier.fillMaxSize()) {
      WiredDesktopBackdrop(settings = settings)

      Column(modifier = Modifier.fillMaxSize()) {
        WiredTopStatusRibbon(
          handle = settings.handle,
          modemStatus = modemStatus,
          personaStage = personaStage,
          onOpenSettings = { viewModel.openWindow(DesktopWindowId.CUSTOMIZER) },
        )

        AiQuickSwitcherBar(
          activeWindow = activeWindow,
          hasActiveRunner = activeCreationRunner != null,
          isLivingStreaming =
            livingStreamState is StreamState.Connecting ||
              livingStreamState is StreamState.Streaming,
          isMentorStreaming =
            mentorStreamState is StreamState.Connecting ||
              mentorStreamState is StreamState.Streaming,
          isCustomAiStreaming =
            customAiStreamState is StreamState.Connecting ||
              customAiStreamState is StreamState.Streaming,
          onSelectAiWindow = { win -> viewModel.openWindow(win) },
        )

        BoxWithConstraints(
          modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          val isExpanded = maxWidth >= 720.dp

          if (isExpanded) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              DesktopIconsColumn(
                onOpen = { viewModel.openWindow(it) },
                modifier = Modifier.width(190.dp).fillMaxHeight(),
              )
              Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                ActiveWorkspaceContent(
                  activeWindow = activeWindow,
                  activeCreationRunner = activeCreationRunner,
                  viewModel = viewModel,
                  livingAiViewModel = livingAiViewModel,
                  personaState = personaState,
                  livingMessages = livingMessages,
                  livingStreamState = livingStreamState,
                  creations = creations,
                  selectedCustomAiId = selectedCustomAiId,
                  studioBusy = studioBusy,
                  customAiMessages = customAiMessages,
                  customAiStreamState = customAiStreamState,
                  workflowTasks = workflowTasks,
                  mentorMessages = mentorMessages,
                  mentorStreamState = mentorStreamState,
                  mentorOrganizing = mentorOrganizing,
                  socialPosts = socialPosts,
                  socialBusy = socialBusy,
                  settings = settings,
                  modemStatus = modemStatus,
                  onSignOut = performSignOut,
                )
              }
            }
          } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              if (activeWindow == null && activeCreationRunner == null) {
                DesktopIconsGrid(onOpen = { viewModel.openWindow(it) })
              } else {
                ActiveWorkspaceContent(
                  activeWindow = activeWindow,
                  activeCreationRunner = activeCreationRunner,
                  viewModel = viewModel,
                  livingAiViewModel = livingAiViewModel,
                  personaState = personaState,
                  livingMessages = livingMessages,
                  livingStreamState = livingStreamState,
                  creations = creations,
                  selectedCustomAiId = selectedCustomAiId,
                  studioBusy = studioBusy,
                  customAiMessages = customAiMessages,
                  customAiStreamState = customAiStreamState,
                  workflowTasks = workflowTasks,
                  mentorMessages = mentorMessages,
                  mentorStreamState = mentorStreamState,
                  mentorOrganizing = mentorOrganizing,
                  socialPosts = socialPosts,
                  socialBusy = socialBusy,
                  settings = settings,
                  modemStatus = modemStatus,
                  onSignOut = performSignOut,
                )
              }
            }
          }
        }

        RetroTaskbar(
          openWindows = openWindows,
          activeWindow = activeWindow,
          isStartMenuOpen = isStartMenuOpen,
          isSettingsOpen = activeWindow == DesktopWindowId.CUSTOMIZER && activeCreationRunner == null,
          modemStatus = modemStatus,
          soundEnabled = settings.soundEnabled,
          onToggleStart = { viewModel.toggleStartMenu() },
          onSelectWindow = { win ->
            if (activeWindow == win && activeCreationRunner == null) {
              viewModel.minimizeActiveWindow()
            } else {
              viewModel.openWindow(win)
            }
          },
          onModemClick = { viewModel.triggerDialUpReconnect() },
          onToggleSound = {
            viewModel.updateAppearance { it.copy(soundEnabled = !it.soundEnabled) }
          },
          onOpenSettings = { viewModel.openWindow(DesktopWindowId.CUSTOMIZER) },
        )
      }

      RetroStartMenuPopup(
        visible = isStartMenuOpen,
        userHandle = settings.handle,
        statusMessage = settings.statusMessage,
        onOpenWindow = { viewModel.openWindow(it) },
        onModemReconnect = { viewModel.triggerDialUpReconnect() },
        onSignOut = performSignOut,
        onDismiss = { viewModel.dismissStartMenu() },
      )

      if (toastMessage != null) {
        Surface(
          modifier =
            Modifier.align(Alignment.TopCenter)
              .padding(top = 56.dp, start = 20.dp, end = 20.dp),
          shape = RoundedCornerShape(10.dp),
          color = skin.windowSurface.copy(alpha = 0.95f),
          border = BorderStroke(1.dp, skin.accentSecondary),
          shadowElevation = 10.dp,
        ) {
          Text(
            text = toastMessage.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = skin.accentSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ActiveWorkspaceContent(
  activeWindow: DesktopWindowId?,
  activeCreationRunner: com.example.data.WiredCreation?,
  viewModel: WiredViewModel,
  livingAiViewModel: LivingAIViewModel,
  personaState: DataState<AiPersona>,
  livingMessages: List<com.example.data.ChatMessage>,
  livingStreamState: StreamState,
  creations: DataState<List<com.example.data.WiredCreation>>,
  selectedCustomAiId: String?,
  studioBusy: Boolean,
  customAiMessages: List<com.example.data.ChatMessage>,
  customAiStreamState: StreamState,
  workflowTasks: DataState<List<com.example.data.WorkflowTask>>,
  mentorMessages: List<com.example.data.ChatMessage>,
  mentorStreamState: StreamState,
  mentorOrganizing: Boolean,
  socialPosts: DataState<List<com.example.data.SocialPost>>,
  socialBusy: Boolean,
  settings: com.example.data.UserAppearanceSettings,
  modemStatus: String,
  onSignOut: () -> Unit,
) {
  val currentPersona = (personaState as? DataState.Ready)?.data ?: AiPersona()
  val targetKey = activeCreationRunner?.id ?: activeWindow?.name ?: "DESKTOP"
  AnimatedContent(
    targetState = targetKey,
    transitionSpec = {
      (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
        scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
        .togetherWith(fadeOut())
    },
    label = "window_transition",
  ) { _ ->
    if (activeCreationRunner != null) {
      RetroWindowFrame(
        title = "EXEC // ${activeCreationRunner.title}",
        badge = activeCreationRunner.kind.uppercase(),
        statusFooter = "Módulo criado na Wired • Execuções: ${activeCreationRunner.playCount}",
        onMinimize = { viewModel.closeCreationRunner() },
        onClose = { viewModel.closeCreationRunner() },
      ) {
        CreationRunnerContent(
          creation = activeCreationRunner,
          customMessages = customAiMessages,
          customStreamState = customAiStreamState,
          soundEngine = viewModel.soundEngine,
          onSendCustomAiMessage = { txt -> viewModel.sendCustomAiMessage(activeCreationRunner, txt) },
          onBackToStudio = { viewModel.closeCreationRunner() },
        )
      }
    } else if (activeWindow != null) {
      RetroWindowFrame(
        title = activeWindow.title,
        badge = activeWindow.badge,
        statusFooter = "$modemStatus • Tema: ${settings.themePreset}",
        onMinimize = { viewModel.minimizeActiveWindow() },
        onClose = { viewModel.closeWindow(activeWindow) },
      ) {
        when (activeWindow) {
          DesktopWindowId.LIVING_MACHINE ->
            LivingMachineWindowContent(
              personaState = personaState,
              messages = livingMessages,
              streamState = livingStreamState,
              onSendMessage = { livingAiViewModel.sendMessage(it) },
            )
          DesktopWindowId.CUSTOM_AIS ->
            CustomAiHubWindowContent(
              creationsState = creations,
              selectedCustomAiId = selectedCustomAiId,
              customMessages = customAiMessages,
              streamState = customAiStreamState,
              isSynthesizing = studioBusy,
              onSelectCustomAi = { viewModel.selectCustomAi(it) },
              onSynthesizeCustomAi = { prompt ->
                viewModel.synthesizeCreationWithAi(prompt, WiredCreation.KIND_AI)
              },
              onCreatePresetCustomAi = { title, desc, prompt ->
                viewModel.createManualWiredNode(title, WiredCreation.KIND_AI, desc, prompt)
              },
              onSendMessage = { creation, txt -> viewModel.sendCustomAiMessage(creation, txt) },
            )
          DesktopWindowId.WIRED_STUDIO ->
            WiredStudioWindowContent(
              creationsState = creations,
              isBusy = studioBusy,
              onSynthesizeWithAi = { prompt, kind -> viewModel.synthesizeCreationWithAi(prompt, kind) },
              onCreateManual = { title, kind, desc, spec ->
                viewModel.createManualWiredNode(title, kind, desc, spec)
              },
              onLaunchCreation = { viewModel.launchCreation(it) },
              onShareCreation = { viewModel.shareCreationToRetroSocial(it, currentPersona) },
              onDeleteCreation = { viewModel.deleteCreation(it) },
            )
          DesktopWindowId.MENTOR_WORKFLOW ->
            MentorWorkflowWindowContent(
              tasksState = workflowTasks,
              mentorMessages = mentorMessages,
              mentorStreamState = mentorStreamState,
              isOrganizing = mentorOrganizing,
              onSendMentorMessage = { viewModel.sendMentorMessage(it) },
              onOrganizeWorkflowWithAi = { viewModel.organizeWorkflowWithAi(it) },
              onAddManualTask = { t, n, p, m -> viewModel.addManualTask(t, n, p, m) },
              onAdvanceTaskStatus = { viewModel.advanceTaskStatus(it) },
              onDeleteTask = { viewModel.deleteTask(it) },
            )
          DesktopWindowId.RETRO_SOCIAL ->
            RetroSocialWindowContent(
              postsState = socialPosts,
              settings = settings,
              isPublishing = socialBusy,
              onPublishPost = { c, t, m -> viewModel.publishRetroPost(c, t, m, currentPersona) },
              onLikePost = { viewModel.likePost(it) },
              onDeletePost = { viewModel.deletePost(it) },
            )
          DesktopWindowId.CUSTOMIZER ->
            CustomizerWindowContent(
              settings = settings,
              onUpdateSettings = { viewModel.updateAppearance(it) },
              onTestModemAudio = { viewModel.triggerDialUpReconnect() },
              onSignOut = onSignOut,
            )
        }
      }
    }
  }
}

private data class DesktopShortcutSpec(
  val windowId: DesktopWindowId,
  val label: String,
  val sublabel: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val accent: Color,
)

private val shortcutSpecs =
  listOf(
    DesktopShortcutSpec(
      DesktopWindowId.LIVING_MACHINE,
      "LAIN.07",
      "Máquina Viva",
      Icons.Default.Memory,
      Color(0xFF00E5A3),
    ),
    DesktopShortcutSpec(
      DesktopWindowId.MENTOR_WORKFLOW,
      "NAVI Mentor",
      "Fluxo & Respostas",
      Icons.Default.Psychology,
      Color(0xFF5C9DFF),
    ),
    DesktopShortcutSpec(
      DesktopWindowId.CUSTOM_AIS,
      "Sub-Navis",
      "IAs Criadas",
      Icons.Default.SmartToy,
      Color(0xFFFF6E96),
    ),
    DesktopShortcutSpec(
      DesktopWindowId.WIRED_STUDIO,
      "Wired Studio",
      "Apps, IAs & Jogos",
      Icons.Default.Build,
      Color(0xFF00D2FF),
    ),
    DesktopShortcutSpec(
      DesktopWindowId.RETRO_SOCIAL,
      "WiredNet BBS",
      "Scraps & Fórum",
      Icons.Default.Forum,
      Color(0xFFFF6E96),
    ),
    DesktopShortcutSpec(
      DesktopWindowId.CUSTOMIZER,
      "Configurações",
      "Temas, CRT & Som",
      Icons.Default.Settings,
      Color(0xFFFFC107),
    ),
  )

@Composable
private fun DesktopIconsGrid(onOpen: (DesktopWindowId) -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize().padding(8.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = "ÁREA DE TRABALHO // WIREDOS",
      style = MaterialTheme.typography.labelMedium,
      color = Color.White.copy(alpha = 0.85f),
    )
    Spacer(Modifier.height(12.dp))
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 135.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(shortcutSpecs, key = { it.windowId.name }) { item ->
        DesktopIconShortcut(
          label = item.label,
          sublabel = item.sublabel,
          icon = item.icon,
          accent = item.accent,
          testTag = "desktop_icon_${item.windowId.name}",
          onClick = { onOpen(item.windowId) },
        )
      }
    }
  }
}

@Composable
private fun DesktopIconsColumn(
  onOpen: (DesktopWindowId) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    shortcutSpecs.forEach { item ->
      DesktopIconShortcut(
        label = item.label,
        sublabel = item.sublabel,
        icon = item.icon,
        accent = item.accent,
        testTag = "desktop_icon_${item.windowId.name}",
        onClick = { onOpen(item.windowId) },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
