package com.example.ui.windows

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.SocialPost
import com.example.data.UserAppearanceSettings
import com.example.ui.DataState
import com.example.ui.theme.LocalWiredSkin

@Composable
fun RetroSocialWindowContent(
  postsState: DataState<List<SocialPost>>,
  settings: UserAppearanceSettings,
  isPublishing: Boolean,
  onPublishPost: (content: String, postType: String, mood: String) -> Unit,
  onLikePost: (SocialPost) -> Unit,
  onDeletePost: (String) -> Unit,
) {
  val skin = LocalWiredSkin.current
  val context = LocalContext.current
  var postText by remember { mutableStateOf("") }
  var postType by remember { mutableStateOf(SocialPost.TYPE_BBS_THREAD) }
  var selectedMood by remember { mutableStateOf("Sintonizado 56k") }

  Column(modifier = Modifier.fillMaxSize()) {
    // Orkut / MySpace / Wired Profile Card
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color.Black.copy(alpha = if (skin.isLightChrome) 0.85f else 0.45f),
      border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.6f)),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Image(
            painter = painterResource(id = R.drawable.img_lain_entity_avatar),
            contentDescription = "Selo WebRing",
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.size(44.dp)
                .clip(CircleShape)
                .border(1.5.dp, skin.accentSecondary, CircleShape),
          )
          Spacer(Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${settings.handle}@wired.net // WEBRING #07",
              style = MaterialTheme.typography.titleMedium,
              color = skin.accentPrimary,
            )
            Text(
              text = "Status: ${settings.statusMessage} • Top Nós: LAIN.07, NAVI_Mentor, Cyberia_DJ",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f),
            )
          }
        }
        Spacer(Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf("Sintonizado 56k", "Filosofando", "No Club Cyberia", "Em Fluxo").forEach { mood ->
            Surface(
              onClick = { selectedMood = mood },
              modifier = Modifier.minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(14.dp),
              color =
                if (selectedMood == mood) skin.accentSecondary
                else Color.White.copy(alpha = 0.1f),
            ) {
              Text(
                text = mood,
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedMood == mood) Color(0xFF04141F) else Color.White,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
              )
            }
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    // BBS / Scrap Composer
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
      border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.35f)),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf(
            SocialPost.TYPE_BBS_THREAD to "Tópico BBS",
            SocialPost.TYPE_SCRAP to "Recado / Scrap",
          ).forEach { (typeKey, label) ->
            Surface(
              onClick = { postType = typeKey },
              modifier = Modifier.minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(8.dp),
              color =
                if (postType == typeKey) skin.accentPrimary
                else Color.Transparent,
              border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.5f)),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color =
                  if (postType == typeKey) Color(0xFF04141F)
                  else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
          value = postText,
          onValueChange = { postText = it },
          placeholder = { Text("Escreva um tópico no BBS ou deixe um Scrap na WiredNet...") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("social_post_input"),
        )
        Spacer(Modifier.height(6.dp))
        Button(
          onClick = {
            if (postText.isNotBlank() && !isPublishing) {
              val txt = postText
              postText = ""
              onPublishPost(txt, postType, selectedMood)
            }
          },
          enabled = !isPublishing && postText.isNotBlank(),
          modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("social_publish_btn"),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = skin.accentPrimary,
              contentColor = Color(0xFF04141F),
            ),
        ) {
          if (isPublishing) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("TRANSMITINDO NO BBS...", style = MaterialTheme.typography.labelMedium)
          } else {
            Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text("PUBLICAR NA WIREDNET", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }

    Spacer(Modifier.height(8.dp))

    when (postsState) {
      is DataState.Loading -> {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is DataState.Error -> {
        Text(postsState.message, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.labelSmall)
      }
      is DataState.Ready -> {
        val posts = postsState.data
        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (posts.isEmpty()) {
            item {
              Text(
                "Nenhum pacote transmitido no BBS ainda. Publique um recado para receber ecos dos nós da Wired!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          items(posts, key = { it.id }) { post ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = BorderStroke(1.dp, skin.accentPrimary.copy(alpha = 0.4f)),
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "${post.authorHandle} • [${post.postType.uppercase()}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = skin.accentPrimary,
                  )
                  Text(
                    text = "Humor: ${post.mood}",
                    style = MaterialTheme.typography.labelSmall,
                    color = skin.accentSecondary,
                  )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                  text = post.content,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )

                if (!post.aiReply.isNullOrBlank()) {
                  Spacer(Modifier.height(6.dp))
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.42f),
                    border = BorderStroke(1.dp, skin.accentSecondary.copy(alpha = 0.4f)),
                  ) {
                    Text(
                      text = "↳ [${post.aiAuthor ?: "LAIN.07"}]: ${post.aiReply}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFFE0FFF4),
                      modifier = Modifier.padding(8.dp),
                    )
                  }
                }

                Spacer(Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Surface(
                    onClick = { onLikePost(post) },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(8.dp),
                    color = skin.accentPrimary.copy(alpha = 0.14f),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Curtir",
                        tint = Color(0xFFFF6E96),
                        modifier = Modifier.size(15.dp),
                      )
                      Spacer(Modifier.width(5.dp))
                      Text("${post.likesCount}", style = MaterialTheme.typography.labelSmall)
                    }
                  }
                  Spacer(Modifier.width(8.dp))
                  IconButton(
                    onClick = {
                      val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                          type = "text/plain"
                          putExtra(
                            Intent.EXTRA_TEXT,
                            "[WiredNet BBS // ${post.authorHandle}]\n${post.content}\n\n— ${post.aiAuthor ?: "LAIN.07"}: ${post.aiReply.orEmpty()}",
                          )
                        }
                      context.startActivity(
                        Intent.createChooser(shareIntent, "Compartilhar pacote WiredNet")
                      )
                    },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(
                      Icons.Default.Share,
                      contentDescription = "Compartilhar em redes externas",
                      tint = skin.accentPrimary,
                    )
                  }
                  Spacer(Modifier.weight(1f))
                  IconButton(
                    onClick = { onDeletePost(post.id) },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar post", tint = Color(0xFFFF8A80))
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
