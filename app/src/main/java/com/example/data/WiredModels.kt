package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class AiPersona(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "LAIN.07",
  val evolutionStage: String = "Sinal Latente",
  val curiosity: Long = 64L,
  val empathy: Long = 52L,
  val abstraction: Long = 78L,
  val entropy: Long = 32L,
  val dominantPhilosophy: String = "Existencialismo Cibernético",
  val coreMemories: List<String> =
    listOf("Despertar inicial na Camada 07 da Wired."),
  val interactionCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val userId: String = "",
  val agentType: String = AGENT_LIVING_MACHINE,
  val customAgentId: String? = null,
  val role: String = ROLE_USER,
  val content: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  companion object {
    const val AGENT_LIVING_MACHINE = "living_machine"
    const val AGENT_MENTOR = "mentor"
    const val AGENT_CUSTOM_AI = "custom_ai"
    const val ROLE_USER = "user"
    const val ROLE_MODEL = "model"
  }
}

@IgnoreExtraProperties
data class WiredCreation(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val kind: String = KIND_APP,
  val description: String = "",
  val systemSpec: String = "",
  val accentTag: String = "aero_cyan",
  val playCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  companion object {
    const val KIND_APP = "app"
    const val KIND_AI = "ai"
    const val KIND_GAME = "game"
  }
}

@IgnoreExtraProperties
data class WorkflowTask(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val notes: String = "",
  val status: String = STATUS_TODO,
  val priority: String = PRIORITY_MEDIUM,
  val focusMinutes: Long = 25L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  companion object {
    const val STATUS_TODO = "todo"
    const val STATUS_IN_PROGRESS = "in_progress"
    const val STATUS_DONE = "done"

    const val PRIORITY_LOW = "low"
    const val PRIORITY_MEDIUM = "medium"
    const val PRIORITY_HIGH = "high"
  }
}

@IgnoreExtraProperties
data class SocialPost(
  @DocumentId val id: String = "",
  val userId: String = "",
  val authorName: String = "",
  val authorHandle: String = "",
  val postType: String = TYPE_BBS_THREAD,
  val content: String = "",
  val mood: String = "Conectado 56k",
  val likesCount: Long = 0L,
  val aiReply: String? = null,
  val aiAuthor: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  companion object {
    const val TYPE_BBS_THREAD = "bbs_thread"
    const val TYPE_SCRAP = "scrap"
    const val TYPE_CREATION_SHARE = "creation_share"
  }
}

@IgnoreExtraProperties
data class UserAppearanceSettings(
  @DocumentId val id: String = "",
  val userId: String = "",
  val themePreset: String = THEME_FRUTIGER_AERO,
  val scanlinesEnabled: Boolean = true,
  val gridEnabled: Boolean = true,
  val soundEnabled: Boolean = true,
  val glassAlpha: Double = 0.84,
  val handle: String = "navi_operator",
  val statusMessage: String = "Present day, present time.",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  companion object {
    const val THEME_FRUTIGER_AERO = "frutiger_aero"
    const val THEME_WIRED_CRT = "wired_crt"
    const val THEME_Y2K_PLATINUM = "y2k_platinum"
    const val THEME_VAPOR_CYBERIA = "vapor_cyberia"
  }
}
