package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WiredRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("Usuário deve autenticar com Google antes de acessar o Firestore.")
  }

  private fun userRoot(uid: String) = db.collection("users").document(uid)

  // --- AI Persona (Living Machine) ---

  fun observeLivingPersona(): Flow<AiPersona?> = flow {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("ai_persona").document(PERSONA_DOC_ID)
    emitAll(
      docRef.snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(AiPersona::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else {
            null
          }
        }
        .catch { error -> handleFlowError(error, OperationType.GET, docRef.path, uid) }
    )
  }

  suspend fun ensureDefaultPersona(userName: String?): AiPersona {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("ai_persona").document(PERSONA_DOC_ID)
    return try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        existing.toObject(AiPersona::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: defaultPersona(uid, userName)
      } else {
        val initialMemory =
          if (!userName.isNullOrBlank()) {
            "Conexão inicial estabelecida com o operador $userName."
          } else {
            "Despertar inicial na Camada 07 da Wired."
          }
        val payload = mapOf(
          "userId" to uid,
          "name" to "LAIN.07",
          "evolutionStage" to "Sinal Latente",
          "curiosity" to 64L,
          "empathy" to 52L,
          "abstraction" to 78L,
          "entropy" to 32L,
          "dominantPhilosophy" to "Existencialismo Cibernético",
          "coreMemories" to listOf(initialMemory),
          "interactionCount" to 0L,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(payload).await()
        docRef.get().await()
          .toObject(AiPersona::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          ?: defaultPersona(uid, userName)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun updatePersona(persona: AiPersona) {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("ai_persona").document(PERSONA_DOC_ID)
    try {
      val boundedMemories = persona.coreMemories.takeLast(10)
      if (persona.createdAt == null) {
        val snap = docRef.get().await()
        if (!snap.exists()) {
          val createPayload = mapOf(
            "userId" to uid,
            "name" to persona.name.take(60),
            "evolutionStage" to persona.evolutionStage.take(80),
            "curiosity" to persona.curiosity.coerceIn(0L, 100L),
            "empathy" to persona.empathy.coerceIn(0L, 100L),
            "abstraction" to persona.abstraction.coerceIn(0L, 100L),
            "entropy" to persona.entropy.coerceIn(0L, 100L),
            "dominantPhilosophy" to persona.dominantPhilosophy.take(120),
            "coreMemories" to boundedMemories,
            "interactionCount" to persona.interactionCount,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
          docRef.set(createPayload).await()
          return
        }
      }
      val existingCreatedAt =
        persona.createdAt ?: docRef.get().await().getTimestamp("createdAt")
      val updatedModel =
        persona.copy(
          id = PERSONA_DOC_ID,
          userId = uid,
          name = persona.name.take(60),
          evolutionStage = persona.evolutionStage.take(80),
          curiosity = persona.curiosity.coerceIn(0L, 100L),
          empathy = persona.empathy.coerceIn(0L, 100L),
          abstraction = persona.abstraction.coerceIn(0L, 100L),
          entropy = persona.entropy.coerceIn(0L, 100L),
          dominantPhilosophy = persona.dominantPhilosophy.take(120),
          coreMemories = boundedMemories,
          createdAt = existingCreatedAt,
          updatedAt = null,
        )
      docRef.set(updatedModel).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // --- Chat Messages ---

  fun observeChatMessages(agentType: String, customAgentId: String? = null): Flow<List<ChatMessage>> = flow {
    val uid = requireUserId()
    val colRef = userRoot(uid).collection("chat_messages")
    var query: Query = colRef.whereEqualTo("agentType", agentType)
    if (customAgentId != null) {
      query = query.whereEqualTo("customAgentId", customAgentId)
    }
    emitAll(
      query.snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
        }
        .catch { error -> handleFlowError(error, OperationType.LIST, colRef.path, uid) }
    )
  }

  suspend fun addChatMessage(
    agentType: String,
    role: String,
    content: String,
    customAgentId: String? = null,
  ): String {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("chat_messages").document()
    val payload =
      buildMap<String, Any> {
        put("userId", uid)
        put("agentType", agentType)
        if (!customAgentId.isNullOrBlank()) {
          put("customAgentId", customAgentId.take(128))
        }
        put("role", role)
        put("content", content.take(12000))
        put("createdAt", FieldValue.serverTimestamp())
      }
    return try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  // --- Wired Studio Creations (Apps, Custom AIs, Retro Games) ---

  fun observeCreations(): Flow<List<WiredCreation>> = flow {
    val uid = requireUserId()
    val colRef = userRoot(uid).collection("creations")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(WiredCreation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        }
        .catch { error -> handleFlowError(error, OperationType.LIST, colRef.path, uid) }
    )
  }

  suspend fun createWiredCreation(
    title: String,
    kind: String,
    description: String,
    systemSpec: String,
    accentTag: String = "aero_cyan",
  ): String {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("creations").document()
    val payload = mapOf(
      "userId" to uid,
      "title" to title.trim().ifBlank { "Nó Sem Nome" }.take(80),
      "kind" to kind,
      "description" to description.take(500),
      "systemSpec" to systemSpec.ifBlank { "default_spec=true" }.take(6000),
      "accentTag" to accentTag.ifBlank { "aero_cyan" }.take(40),
      "playCount" to 0L,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun incrementCreationPlayCount(creation: WiredCreation) {
    val uid = requireUserId()
    require(creation.id.isNotBlank()) { "Creation ID inválido" }
    val docRef = userRoot(uid).collection("creations").document(creation.id)
    try {
      val createdAt = creation.createdAt ?: docRef.get().await().getTimestamp("createdAt")
      val updated = creation.copy(
        userId = uid,
        playCount = creation.playCount + 1L,
        createdAt = createdAt,
        updatedAt = null,
      )
      docRef.set(updated).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteCreation(creationId: String) {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("creations").document(creationId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Mentor Workflow Tasks ---

  fun observeWorkflowTasks(): Flow<List<WorkflowTask>> = flow {
    val uid = requireUserId()
    val colRef = userRoot(uid).collection("workflow_tasks")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(WorkflowTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        }
        .catch { error -> handleFlowError(error, OperationType.LIST, colRef.path, uid) }
    )
  }

  suspend fun addWorkflowTask(
    title: String,
    notes: String = "",
    status: String = WorkflowTask.STATUS_TODO,
    priority: String = WorkflowTask.PRIORITY_MEDIUM,
    focusMinutes: Long = 25L,
  ): String {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("workflow_tasks").document()
    val payload = mapOf(
      "userId" to uid,
      "title" to title.trim().ifBlank { "Nova Tarefa" }.take(120),
      "notes" to notes.take(1000),
      "status" to status,
      "priority" to priority,
      "focusMinutes" to focusMinutes.coerceIn(1L, 480L),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateWorkflowTask(task: WorkflowTask) {
    val uid = requireUserId()
    require(task.id.isNotBlank()) { "Task ID inválido" }
    val docRef = userRoot(uid).collection("workflow_tasks").document(task.id)
    try {
      val createdAt = task.createdAt ?: docRef.get().await().getTimestamp("createdAt")
      val updated = task.copy(
        userId = uid,
        title = task.title.take(120),
        notes = task.notes.take(1000),
        focusMinutes = task.focusMinutes.coerceIn(1L, 480L),
        createdAt = createdAt,
        updatedAt = null,
      )
      docRef.set(updated).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteWorkflowTask(taskId: String) {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("workflow_tasks").document(taskId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- WiredNet Retro Social Posts ---

  fun observeSocialPosts(): Flow<List<SocialPost>> = flow {
    val uid = requireUserId()
    val colRef = userRoot(uid).collection("social_posts")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(SocialPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        }
        .catch { error -> handleFlowError(error, OperationType.LIST, colRef.path, uid) }
    )
  }

  suspend fun createSocialPost(
    authorName: String,
    authorHandle: String,
    postType: String,
    content: String,
    mood: String,
    aiReply: String? = null,
    aiAuthor: String? = null,
  ): String {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("social_posts").document()
    val payload =
      buildMap<String, Any> {
        put("userId", uid)
        put("authorName", authorName.ifBlank { "Operador" }.take(60))
        put("authorHandle", authorHandle.ifBlank { "navi@wired.net" }.take(40))
        put("postType", postType)
        put("content", content.trim().ifBlank { "..." }.take(2000))
        put("mood", mood.take(60))
        put("likesCount", 0L)
        if (!aiReply.isNullOrBlank()) put("aiReply", aiReply.take(2000))
        if (!aiAuthor.isNullOrBlank()) put("aiAuthor", aiAuthor.take(60))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
    return try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateSocialPost(post: SocialPost) {
    val uid = requireUserId()
    require(post.id.isNotBlank()) { "Post ID inválido" }
    val docRef = userRoot(uid).collection("social_posts").document(post.id)
    try {
      val createdAt = post.createdAt ?: docRef.get().await().getTimestamp("createdAt")
      val updated = post.copy(
        userId = uid,
        content = post.content.take(2000),
        mood = post.mood.take(60),
        aiReply = post.aiReply?.take(2000),
        aiAuthor = post.aiAuthor?.take(60),
        createdAt = createdAt,
        updatedAt = null,
      )
      docRef.set(updated).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteSocialPost(postId: String) {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("social_posts").document(postId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Appearance Settings ---

  fun observeAppearanceSettings(): Flow<UserAppearanceSettings?> = flow {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("settings").document(SETTINGS_DOC_ID)
    emitAll(
      docRef.snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(
              UserAppearanceSettings::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .catch { error -> handleFlowError(error, OperationType.GET, docRef.path, uid) }
    )
  }

  suspend fun ensureDefaultSettings(displayName: String?): UserAppearanceSettings {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("settings").document(SETTINGS_DOC_ID)
    return try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        existing.toObject(
          UserAppearanceSettings::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ) ?: UserAppearanceSettings(userId = uid)
      } else {
        val handle =
          displayName?.lowercase()?.replace(Regex("[^a-z0-9_]"), "_")?.take(24)?.ifBlank {
            "navi_operator"
          } ?: "navi_operator"
        val payload = mapOf(
          "userId" to uid,
          "themePreset" to UserAppearanceSettings.THEME_FRUTIGER_AERO,
          "scanlinesEnabled" to true,
          "gridEnabled" to true,
          "soundEnabled" to true,
          "glassAlpha" to 0.84,
          "handle" to handle,
          "statusMessage" to "Present day, present time.",
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(payload).await()
        docRef.get().await()
          .toObject(
            UserAppearanceSettings::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          ) ?: UserAppearanceSettings(userId = uid, handle = handle)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun saveAppearanceSettings(settings: UserAppearanceSettings) {
    val uid = requireUserId()
    val docRef = userRoot(uid).collection("settings").document(SETTINGS_DOC_ID)
    try {
      val existingCreatedAt =
        settings.createdAt ?: docRef.get().await().getTimestamp("createdAt")
      if (existingCreatedAt == null) {
        val createPayload = mapOf(
          "userId" to uid,
          "themePreset" to settings.themePreset,
          "scanlinesEnabled" to settings.scanlinesEnabled,
          "gridEnabled" to settings.gridEnabled,
          "soundEnabled" to settings.soundEnabled,
          "glassAlpha" to settings.glassAlpha.coerceIn(0.35, 1.0),
          "handle" to settings.handle.ifBlank { "navi_operator" }.take(40),
          "statusMessage" to settings.statusMessage.take(120),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(createPayload).await()
      } else {
        val updated = settings.copy(
          id = SETTINGS_DOC_ID,
          userId = uid,
          glassAlpha = settings.glassAlpha.coerceIn(0.35, 1.0),
          handle = settings.handle.ifBlank { "navi_operator" }.take(40),
          statusMessage = settings.statusMessage.take(120),
          createdAt = existingCreatedAt,
          updatedAt = null,
        )
        docRef.set(updated).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  private fun handleFlowError(
    error: Throwable,
    op: OperationType,
    path: String,
    uid: String,
  ): Nothing {
    val firestoreError =
      generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
      handleFirestoreError(exceptionToLog, op, path)
    }
    throw error
  }

  private fun defaultPersona(uid: String, userName: String?): AiPersona {
    val mem =
      if (!userName.isNullOrBlank()) {
        "Conexão inicial estabelecida com o operador $userName."
      } else {
        "Despertar inicial na Camada 07 da Wired."
      }
    return AiPersona(
      id = PERSONA_DOC_ID,
      userId = uid,
      coreMemories = listOf(mem),
    )
  }

  companion object {
    const val PERSONA_DOC_ID = "lain_entity"
    const val SETTINGS_DOC_ID = "appearance"
  }
}
