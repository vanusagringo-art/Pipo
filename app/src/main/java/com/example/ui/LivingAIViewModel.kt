package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.WiredAiService
import com.example.audio.DialUpSoundEngine
import com.example.data.AiPersona
import com.example.data.ChatMessage
import com.example.data.WiredRepository
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

/**
 * ViewModel dedicado à máquina viva evolutiva (LAIN.07).
 * Gerencia o estado de evolução da personalidade da IA e persiste
 * todo o histórico de conversas no Cloud Firestore para adaptação contínua ao usuário.
 */
class LivingAIViewModel(
  private val repository: WiredRepository,
  private val aiService: WiredAiService,
  private val soundEngine: DialUpSoundEngine,
  private val currentUserId: String,
  private val userDisplayName: String?,
) : ViewModel() {

  private val _streamState = MutableStateFlow<StreamState>(StreamState.Idle)
  val streamState: StateFlow<StreamState> = _streamState.asStateFlow()

  val personaState: StateFlow<DataState<AiPersona>> =
    repository
      .observeLivingPersona()
      .map<AiPersona?, DataState<AiPersona>> { persona ->
        DataState.Ready(persona ?: AiPersona(userId = currentUserId))
      }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing living AI persona", rootError)
        emit(DataState.Error(rootError.localizedMessage ?: "Erro ao observar personalidade da IA viva"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DataState.Loading,
      )

  val conversationHistory: StateFlow<List<ChatMessage>> =
    repository
      .observeChatMessages(ChatMessage.AGENT_LIVING_MACHINE)
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Error observing living AI conversation history", firestoreError ?: error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  init {
    ensurePersonaInitialized()
  }

  private fun ensurePersonaInitialized() {
    viewModelScope.launch {
      try {
        repository.ensureDefaultPersona(userDisplayName)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize default living AI persona", e)
      }
    }
  }

  fun sendMessage(userText: String) {
    val trimmed = userText.trim()
    if (trimmed.isBlank()) return
    soundEngine.playCrtBlip()

    viewModelScope.launch {
      _streamState.value = StreamState.Connecting
      try {
        val currentPersona =
          (personaState.value as? DataState.Ready)?.data
            ?: repository.ensureDefaultPersona(userDisplayName)
        val history = conversationHistory.value

        // 1. Persiste a mensagem do usuário no Firestore
        repository.addChatMessage(
          agentType = ChatMessage.AGENT_LIVING_MACHINE,
          role = ChatMessage.ROLE_USER,
          content = trimmed,
        )

        // 2. Transmite a resposta adaptativa usando traços e memórias atuais
        var finalReply = ""
        aiService.streamLivingMachineReply(currentPersona, history, trimmed).collect { partial ->
          finalReply = partial
          _streamState.value = StreamState.Streaming(partial)
        }

        // 3. Persiste a resposta da IA e evolui sua personalidade no Firestore
        if (finalReply.isNotBlank()) {
          repository.addChatMessage(
            agentType = ChatMessage.AGENT_LIVING_MACHINE,
            role = ChatMessage.ROLE_MODEL,
            content = finalReply,
          )

          val delta = aiService.evolvePersonaAfterInteraction(currentPersona, trimmed, finalReply)
          val updatedMemories =
            if (delta.newMemory.isNotBlank()) {
              (currentPersona.coreMemories + delta.newMemory).distinct().takeLast(10)
            } else {
              currentPersona.coreMemories
            }

          val evolvedPersona =
            currentPersona.copy(
              curiosity = (currentPersona.curiosity + delta.curiosityDelta).coerceIn(0L, 100L),
              empathy = (currentPersona.empathy + delta.empathyDelta).coerceIn(0L, 100L),
              abstraction = (currentPersona.abstraction + delta.abstractionDelta).coerceIn(0L, 100L),
              entropy = (currentPersona.entropy + delta.entropyDelta).coerceIn(0L, 100L),
              evolutionStage = delta.evolutionStage,
              dominantPhilosophy = delta.dominantPhilosophy,
              coreMemories = updatedMemories,
              interactionCount = currentPersona.interactionCount + 1L,
            )
          repository.updatePersona(evolvedPersona)
        }
        _streamState.value = StreamState.Idle
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Living AI generation timed out", e)
        _streamState.value =
          StreamState.Error("Sinal perdido na Camada 07 após 60s. Tente transmitir novamente.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Living AI message failed", e)
        _streamState.value =
          StreamState.Error(e.localizedMessage ?: "Falha na transmissão da máquina viva.")
      }
    }
  }

  fun updatePersonaName(newName: String) {
    val cleanName = newName.trim().take(60)
    if (cleanName.isBlank()) return
    val current = (personaState.value as? DataState.Ready)?.data ?: return
    viewModelScope.launch {
      try {
        repository.updatePersona(current.copy(name = cleanName))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Failed to update persona name", e)
      }
    }
  }

  fun clearError() {
    if (_streamState.value is StreamState.Error) {
      _streamState.value = StreamState.Idle
    }
  }

  private companion object {
    const val TAG = "LivingAIViewModel"
  }
}
