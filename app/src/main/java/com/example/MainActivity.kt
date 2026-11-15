package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ai.WiredAiService
import com.example.audio.DialUpSoundEngine
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.data.WiredRepository
import com.example.firebase.configureAppCheck
import com.example.ui.AuthScreen
import com.example.ui.LivingAIViewModel
import com.example.ui.WiredDesktopScreen
import com.example.ui.WiredViewModel
import com.example.ui.theme.WiredOsTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Mandatory Step 1: Configure App Check with launch intent before UI or ViewModels
    configureAppCheck(this, intent)

    enableEdgeToEdge()
    setContent {
      WiredOsTheme {
        AppNavigation()
      }
    }
  }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val context = LocalContext.current
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

  LaunchedEffect(Unit) {
    if (auth.currentUser == null) {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = {},
        onUnauthenticated = {},
        scope = this,
      )
    }
  }

  val user = currentUser
  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    key(user.uid) {
      val soundEngine = remember(user.uid) { DialUpSoundEngine() }
      val wiredViewModel: WiredViewModel =
        viewModel(
          key = user.uid,
          factory =
            viewModelFactory {
              initializer {
                val app =
                  checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                  }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                WiredViewModel(
                  repository = WiredRepository(db, auth),
                  aiService = WiredAiService(),
                  soundEngine = soundEngine,
                  currentUserId = user.uid,
                  userDisplayName = user.displayName,
                )
              }
            },
        )
      val livingAiViewModel: LivingAIViewModel =
        viewModel(
          key = "living_${user.uid}",
          factory =
            viewModelFactory {
              initializer {
                val app =
                  checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                  }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                LivingAIViewModel(
                  repository = WiredRepository(db, auth),
                  aiService = WiredAiService(),
                  soundEngine = soundEngine,
                  currentUserId = user.uid,
                  userDisplayName = user.displayName,
                )
              }
            },
        )
      WiredDesktopScreen(
        viewModel = wiredViewModel,
        livingAiViewModel = livingAiViewModel,
      )
    }
  }
}
