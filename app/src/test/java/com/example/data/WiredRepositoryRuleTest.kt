package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WiredRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureDefaultPersonaAndSettings_authenticatedOwner_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = WiredRepository(firestore, auth)

    val persona = withTimeout(DEFAULT_TIMEOUT_MS) { repository.ensureDefaultPersona("Alice") }
    assertEquals("LAIN.07", persona.name)

    val settings = withTimeout(DEFAULT_TIMEOUT_MS) { repository.ensureDefaultSettings("Alice") }
    assertEquals(UserAppearanceSettings.THEME_FRUTIGER_AERO, settings.themePreset)
  }

  @Test
  fun createAndObserveCreationTaskAndPost_authenticatedOwner_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = WiredRepository(firestore, auth)

    val creationId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repository.createWiredCreation(
          title = "Protocolo Snake",
          kind = WiredCreation.KIND_GAME,
          description = "Mini-jogo retrô",
          systemSpec = "speed=160;grid=12",
          accentTag = "cyber_green",
        )
      }
    assertTrue(creationId.isNotBlank())

    val creations =
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeCreations().first { list -> list.any { it.id == creationId } }
      }
    assertTrue(creations.any { it.id == creationId && it.title == "Protocolo Snake" })

    val taskId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repository.addWorkflowTask(
          title = "Estudar Redes Neurais",
          notes = "Revisar capítulos 1-3",
          priority = WorkflowTask.PRIORITY_HIGH,
          focusMinutes = 45L,
        )
      }
    val tasks =
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeWorkflowTasks().first { list -> list.any { it.id == taskId } }
      }
    assertTrue(tasks.any { it.id == taskId })

    val postId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repository.createSocialPost(
          authorName = "Alice",
          authorHandle = "alice@wired.jp",
          postType = SocialPost.TYPE_BBS_THREAD,
          content = "Conectada na Wired!",
          mood = "Sintonizada",
          aiReply = "A rede lembra de você.",
          aiAuthor = "LAIN.07",
        )
      }
    val posts =
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeSocialPosts().first { list -> list.any { it.id == postId } }
      }
    assertTrue(posts.any { it.id == postId })
  }

  @Test
  fun observeWorkflowTasks_unauthenticatedUser_failsWithIllegalStateOrPermissionDenied() =
    runBlocking {
      auth.signOut()
      val repository = WiredRepository(firestore, auth)

      var caught: Throwable? = null
      try {
        withTimeout(FLOW_TIMEOUT_MS) { repository.observeWorkflowTasks().first() }
      } catch (t: Throwable) {
        caught = t
      }
      assertNotNull(caught)
    }

  @Test
  fun crossUserAccess_directFirestoreRead_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = WiredRepository(firestore, auth)
    val taskId =
      withTimeout(DEFAULT_TIMEOUT_MS) {
        aliceRepo.addWorkflowTask("Segredo da Alice", "Privado")
      }

    signInTestUser(BOB_EMAIL)
    var deniedCode: FirebaseFirestoreException.Code? = null
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore
          .collection("users")
          .document(aliceUid)
          .collection("workflow_tasks")
          .document(taskId)
          .get()
          .let { com.google.android.gms.tasks.Tasks.await(it) }
      }
    } catch (e: Exception) {
      deniedCode = e.unwrapFirestoreException()?.code
    }
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, deniedCode)
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val BOB_EMAIL = "bob@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
