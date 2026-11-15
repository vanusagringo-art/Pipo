package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.WiredAiService
import com.example.audio.DialUpSoundEngine
import com.example.data.AiPersona
import com.example.data.ChatMessage
import com.example.data.SocialPost
import com.example.data.UserAppearanceSettings
import com.example.data.WiredCreation
import com.example.data.WiredRepository
import com.example.data.WorkflowTask
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DesktopWindowId(val title: String, val badge: String) {
  LIVING_MACHINE("PROTOCOLO 07 // LAIN.EXE", "IA VIVA"),
  MENTOR_WORKFLOW("NAVI MENTOR // FLUXO", "MENTOR"),
  CUSTOM_AIS("SUB-NAVIS // IAs CRIADAS", "SUB-NAVI"),
  WIRED_STUDIO("WIRED STUDIO // FORJA", "CRIAR"),
  RETRO_SOCIAL("WIREDNET BBS & SCRAPS", "SOCIAL"),
  CUSTOMIZER("CONFIGURAÇÕES // TEMAS", "CONFIG"),
}

sealed interface StreamState {
  data object Idle : StreamState
  data object Connecting : StreamState
  data class Streaming(val partialText: String) : StreamState
  data class Error(val message: String) : StreamState
}

sealed interface DataState<out T> {
  data object Loading : DataState<Nothing>
  data class Ready<T>(val data: T) : DataState<T>
  data class Error(val message: String) : DataState<Nothing>
}

class WiredViewModel(
  private val repository: WiredRepository,
  private val aiService: WiredAiService,
  val soundEngine: DialUpSoundEngine,
  private val currentUserId: String,
  private val userDisplayName: String?,
) : ViewModel() {

  // Window manager state (2000s desktop experience)
  private val _activeWindow = MutableStateFlow<DesktopWindowId?>(DesktopWindowId.LIVING_MACHINE)
  val activeWindow: StateFlow<DesktopWindowId?> = _activeWindow.asStateFlow()

  private val _openWindows =
    MutableStateFlow(
      listOf(
        DesktopWindowId.LIVING_MACHINE,
        DesktopWindowId.MENTOR_WORKFLOW,
        DesktopWindowId.CUSTOM_AIS,
      )
    )
  val openWindows: StateFlow<List<DesktopWindowId>> = _openWindows.asStateFlow()

  private val _selectedCustomAiId = MutableStateFlow<String?>(null)
  val selectedCustomAiId: StateFlow<String?> = _selectedCustomAiId.asStateFlow()

  private val _isStartMenuOpen = MutableStateFlow(false)
  val isStartMenuOpen: StateFlow<Boolean> = _isStartMenuOpen.asStateFlow()

  private val _activeCreationRunner = MutableStateFlow<WiredCreation?>(null)
  val activeCreationRunner: StateFlow<WiredCreation?> = _activeCreationRunner.asStateFlow()

  private val _modemStatus = MutableStateFlow("56.6 kbps // CONECTADO")
  val modemStatus: StateFlow<String> = _modemStatus.asStateFlow()

  // AI stream states
  private val _mentorStreamState = MutableStateFlow<StreamState>(StreamState.Idle)
  val mentorStreamState: StateFlow<StreamState> = _mentorStreamState.asStateFlow()

  private val _customAiStreamState = MutableStateFlow<StreamState>(StreamState.Idle)
  val customAiStreamState: StateFlow<StreamState> = _customAiStreamState.asStateFlow()

  private val _studioBusy = MutableStateFlow(false)
  val studioBusy: StateFlow<Boolean> = _studioBusy.asStateFlow()

  private val _mentorOrganizing = MutableStateFlow(false)
  val mentorOrganizing: StateFlow<Boolean> = _mentorOrganizing.asStateFlow()

  private val _socialBusy = MutableStateFlow(false)
  val socialBusy: StateFlow<Boolean> = _socialBusy.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  val mentorMessages: StateFlow<List<ChatMessage>> =
    repository
      .observeChatMessages(ChatMessage.AGENT_MENTOR)
      .catch { e ->
        Log.w(TAG, "Error observing mentor messages", e)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val customAiMessages: StateFlow<List<ChatMessage>> =
    repository
      .observeChatMessages(ChatMessage.AGENT_CUSTOM_AI)
      .catch { e ->
        Log.w(TAG, "Error observing custom AI messages", e)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val creations: StateFlow<DataState<List<WiredCreation>>> =
    repository
      .observeCreations()
      .map<List<WiredCreation>, DataState<List<WiredCreation>>> { DataState.Ready(it) }
      .catch { e -> emit(DataState.Error(unwrapError(e, "Erro ao carregar criações"))) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val workflowTasks: StateFlow<DataState<List<WorkflowTask>>> =
    repository
      .observeWorkflowTasks()
      .map<List<WorkflowTask>, DataState<List<WorkflowTask>>> { DataState.Ready(it) }
      .catch { e -> emit(DataState.Error(unwrapError(e, "Erro ao carregar fluxo"))) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val socialPosts: StateFlow<DataState<List<SocialPost>>> =
    repository
      .observeSocialPosts()
      .map<List<SocialPost>, DataState<List<SocialPost>>> { DataState.Ready(it) }
      .catch { e -> emit(DataState.Error(unwrapError(e, "Erro ao carregar WiredNet"))) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

  val appearanceSettings: StateFlow<UserAppearanceSettings> =
    repository
      .observeAppearanceSettings()
      .map { settings ->
        val resolved = settings ?: UserAppearanceSettings(userId = currentUserId)
        soundEngine.isEnabled = resolved.soundEnabled
        resolved
      }
      .catch { e ->
        Log.w(TAG, "Error observing appearance settings", e)
        emit(UserAppearanceSettings(userId = currentUserId))
      }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        UserAppearanceSettings(userId = currentUserId),
      )

  init {
    bootstrapUserWorkspace()
  }

  private fun bootstrapUserWorkspace() {
    viewModelScope.launch {
      try {
        val settings = repository.ensureDefaultSettings(userDisplayName)
        soundEngine.isEnabled = settings.soundEnabled
        if (settings.soundEnabled) {
          soundEngine.playDialUpHandshake()
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Bootstrap failed", e)
        _toastMessage.value = e.localizedMessage ?: "Falha ao inicializar nó Wired."
      }
    }
  }

  // --- Window Management ---

  fun openWindow(windowId: DesktopWindowId) {
    soundEngine.playWindowOpen()
    _isStartMenuOpen.value = false
    if (!_openWindows.value.contains(windowId)) {
      _openWindows.value = _openWindows.value + windowId
    }
    _activeWindow.value = windowId
    _activeCreationRunner.value = null
  }

  fun minimizeActiveWindow() {
    soundEngine.playWindowMinimize()
    _activeWindow.value = null
    _activeCreationRunner.value = null
  }

  fun closeWindow(windowId: DesktopWindowId) {
    soundEngine.playRetroClick()
    val updated = _openWindows.value.filterNot { it == windowId }
    _openWindows.value = updated
    if (_activeWindow.value == windowId) {
      _activeWindow.value = updated.lastOrNull()
    }
  }

  fun toggleStartMenu() {
    soundEngine.playRetroClick()
    _isStartMenuOpen.value = !_isStartMenuOpen.value
  }

  fun dismissStartMenu() {
    _isStartMenuOpen.value = false
  }

  fun triggerDialUpReconnect() {
    _modemStatus.value = "DISCANDO V.90..."
    soundEngine.playDialUpHandshake()
    viewModelScope.launch {
      kotlinx.coroutines.delay(3100L)
      _modemStatus.value = "56.6 kbps // CONECTADO"
    }
  }

  fun launchCreation(creation: WiredCreation) {
    soundEngine.playWindowOpen()
    _activeCreationRunner.value = creation
    viewModelScope.launch {
      try {
        repository.incrementCreationPlayCount(creation)
      } catch (e: Exception) {
        Log.w(TAG, "Could not increment playCount", e)
      }
    }
  }

  fun closeCreationRunner() {
    soundEngine.playWindowMinimize()
    _activeCreationRunner.value = null
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  // --- 2. Mentor AI & Workflow Organizer ---

  fun sendMentorMessage(userText: String) {
    val trimmed = userText.trim()
    if (trimmed.isBlank()) return
    soundEngine.playRetroClick()

    viewModelScope.launch {
      _mentorStreamState.value = StreamState.Connecting
      try {
        val tasks = (workflowTasks.value as? DataState.Ready)?.data ?: emptyList()
        val history = mentorMessages.value

        repository.addChatMessage(
          agentType = ChatMessage.AGENT_MENTOR,
          role = ChatMessage.ROLE_USER,
          content = trimmed,
        )

        var finalReply = ""
        aiService.streamMentorReply(tasks, history, trimmed).collect { partial ->
          finalReply = partial
          _mentorStreamState.value = StreamState.Streaming(partial)
        }

        if (finalReply.isNotBlank()) {
          repository.addChatMessage(
            agentType = ChatMessage.AGENT_MENTOR,
            role = ChatMessage.ROLE_MODEL,
            content = finalReply,
          )
        }
        _mentorStreamState.value = StreamState.Idle
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Mentor timeout", e)
        _mentorStreamState.value = StreamState.Error("Mentor NAVI expirou após 60s.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Mentor error", e)
        _mentorStreamState.value =
          StreamState.Error(e.localizedMessage ?: "Falha no NAVI Mentor.")
      }
    }
  }

  fun organizeWorkflowWithAi(goal: String) {
    val trimmed = goal.trim()
    if (trimmed.isBlank()) return
    soundEngine.playRetroClick()

    viewModelScope.launch {
      _mentorOrganizing.value = true
      try {
        val existing = (workflowTasks.value as? DataState.Ready)?.data ?: emptyList()
        val suggestions = aiService.organizeWorkflowWithMentor(trimmed, existing)
        suggestions.forEach { task ->
          repository.addWorkflowTask(
            title = task.title,
            notes = task.notes,
            status = WorkflowTask.STATUS_TODO,
            priority = task.priority,
            focusMinutes = task.focusMinutes,
          )
        }
        _toastMessage.value = "NAVI Mentor organizou ${suggestions.size} tarefas no seu fluxo!"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Organize workflow error", e)
        _toastMessage.value = e.localizedMessage ?: "Falha ao organizar fluxo com IA."
      } finally {
        _mentorOrganizing.value = false
      }
    }
  }

  fun addManualTask(title: String, notes: String, priority: String, focusMinutes: Long) {
    if (title.isBlank()) return
    soundEngine.playRetroClick()
    viewModelScope.launch {
      try {
        repository.addWorkflowTask(title, notes, WorkflowTask.STATUS_TODO, priority, focusMinutes)
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao salvar tarefa."
      }
    }
  }

  fun advanceTaskStatus(task: WorkflowTask) {
    soundEngine.playRetroClick()
    val nextStatus =
      when (task.status) {
        WorkflowTask.STATUS_TODO -> WorkflowTask.STATUS_IN_PROGRESS
        WorkflowTask.STATUS_IN_PROGRESS -> WorkflowTask.STATUS_DONE
        else -> WorkflowTask.STATUS_TODO
      }
    viewModelScope.launch {
      try {
        repository.updateWorkflowTask(task.copy(status = nextStatus))
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao atualizar tarefa."
      }
    }
  }

  fun deleteTask(taskId: String) {
    soundEngine.playRetroClick()
    viewModelScope.launch {
      try {
        repository.deleteWorkflowTask(taskId)
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao remover tarefa."
      }
    }
  }

  // --- 3. Wired Studio (Apps, Custom AIs, Retro Games) ---

  fun selectCustomAi(creationId: String) {
    soundEngine.playRetroClick()
    _selectedCustomAiId.value = creationId
  }

  fun synthesizeCreationWithAi(prompt: String, kind: String) {
    if (prompt.isBlank()) return
    soundEngine.playCrtBlip()
    viewModelScope.launch {
      _studioBusy.value = true
      try {
        val synth = aiService.synthesizeWiredCreation(prompt, kind)
        val newId =
          repository.createWiredCreation(
            title = synth.title,
            kind = kind,
            description = synth.description,
            systemSpec = synth.systemSpec,
            accentTag = synth.accentTag,
          )
        if (kind == WiredCreation.KIND_AI) {
          _selectedCustomAiId.value = newId
        }
        _toastMessage.value = "Nó '${synth.title}' compilado na Wired!"
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Studio synthesis error", e)
        _toastMessage.value = e.localizedMessage ?: "Erro ao sintetizar criação."
      } finally {
        _studioBusy.value = false
      }
    }
  }

  fun createManualWiredNode(
    title: String,
    kind: String,
    description: String,
    systemSpec: String,
  ) {
    if (title.isBlank()) return
    soundEngine.playRetroClick()
    viewModelScope.launch {
      try {
        val newId =
          repository.createWiredCreation(title, kind, description, systemSpec, "aero_cyan")
        if (kind == WiredCreation.KIND_AI) {
          _selectedCustomAiId.value = newId
        }
        _toastMessage.value = "'$title' salvo no diretório /wired/studio."
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao criar nó."
      }
    }
  }

  fun deleteCreation(creationId: String) {
    soundEngine.playRetroClick()
    viewModelScope.launch {
      try {
        repository.deleteCreation(creationId)
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao deletar criação."
      }
    }
  }

  fun sendCustomAiMessage(creation: WiredCreation, userText: String) {
    val trimmed = userText.trim()
    if (trimmed.isBlank()) return
    soundEngine.playCrtBlip()

    viewModelScope.launch {
      _customAiStreamState.value = StreamState.Connecting
      try {
        val history = customAiMessages.value.filter { it.customAgentId == creation.id }
        repository.addChatMessage(
          agentType = ChatMessage.AGENT_CUSTOM_AI,
          role = ChatMessage.ROLE_USER,
          content = trimmed,
          customAgentId = creation.id,
        )

        var finalReply = ""
        aiService.streamCustomAiReply(creation, history, trimmed).collect { partial ->
          finalReply = partial
          _customAiStreamState.value = StreamState.Streaming(partial)
        }

        if (finalReply.isNotBlank()) {
          repository.addChatMessage(
            agentType = ChatMessage.AGENT_CUSTOM_AI,
            role = ChatMessage.ROLE_MODEL,
            content = finalReply,
            customAgentId = creation.id,
          )
        }
        _customAiStreamState.value = StreamState.Idle
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Custom AI error", e)
        _customAiStreamState.value =
          StreamState.Error(e.localizedMessage ?: "Erro ao executar Sub-Navi.")
      }
    }
  }

  // --- 4. WiredNet Retro Social ---

  fun publishRetroPost(
    content: String,
    postType: String,
    mood: String,
    persona: AiPersona = AiPersona(userId = currentUserId),
  ) {
    val trimmed = content.trim()
    if (trimmed.isBlank()) return
    soundEngine.playCrtBlip()

    viewModelScope.launch {
      _socialBusy.value = true
      try {
        val settings = appearanceSettings.value
        val (aiAuthor, aiReply) = aiService.generateRetroSocialReply(trimmed, postType, persona)
        repository.createSocialPost(
          authorName = userDisplayName ?: settings.handle,
          authorHandle = "${settings.handle}@wired.net",
          postType = postType,
          content = trimmed,
          mood = mood.ifBlank { settings.statusMessage },
          aiReply = aiReply,
          aiAuthor = aiAuthor,
        )
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Social post error", e)
        _toastMessage.value = e.localizedMessage ?: "Falha ao transmitir pacote BBS."
      } finally {
        _socialBusy.value = false
      }
    }
  }

  fun shareCreationToRetroSocial(
    creation: WiredCreation,
    persona: AiPersona = AiPersona(userId = currentUserId),
  ) {
    soundEngine.playWindowOpen()
    val kindLabel =
      when (creation.kind) {
        WiredCreation.KIND_AI -> "IA Sub-Navi"
        WiredCreation.KIND_GAME -> "Mini-Jogo"
        else -> "Mini-App"
      }
    publishRetroPost(
      content = "[WEBRING SHARE] Acabei de compilar '$kindLabel': ${creation.title} — ${creation.description}",
      postType = SocialPost.TYPE_CREATION_SHARE,
      mood = "Compilando na Wired",
      persona = persona,
    )
    openWindow(DesktopWindowId.RETRO_SOCIAL)
  }

  fun likePost(post: SocialPost) {
    soundEngine.playRetroClick()
    viewModelScope.launch {
      try {
        repository.updateSocialPost(post.copy(likesCount = post.likesCount + 1L))
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao curtir."
      }
    }
  }

  fun deletePost(postId: String) {
    soundEngine.playRetroClick()
    viewModelScope.launch {
      try {
        repository.deleteSocialPost(postId)
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao excluir post."
      }
    }
  }

  // --- 5. Full OS Customization ---

  fun updateAppearance(transform: (UserAppearanceSettings) -> UserAppearanceSettings) {
    soundEngine.playRetroClick()
    val next = transform(appearanceSettings.value)
    soundEngine.isEnabled = next.soundEnabled
    viewModelScope.launch {
      try {
        repository.saveAppearanceSettings(next)
      } catch (e: Exception) {
        _toastMessage.value = e.localizedMessage ?: "Erro ao salvar personalização."
      }
    }
  }

  private fun unwrapError(error: Throwable, fallback: String): String {
    val firestoreError =
      generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
    return firestoreError?.localizedMessage ?: error.localizedMessage ?: fallback
  }

  private companion object {
    const val TAG = "WiredViewModel"
  }
}
