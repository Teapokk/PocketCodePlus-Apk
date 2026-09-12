package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProgramObject
import com.example.model.Project
import com.example.ui.dialogs.AddObjectDialog
import com.example.ui.theme.PocketAccentAmber
import com.example.ui.theme.PocketTealDark
import com.example.ui.theme.PocketTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectOverviewScreen(
  project: Project,
  onBack: () -> Unit,
  onOpenObject: (String) -> Unit,
  onPlay: () -> Unit,
  onAddObject: (name: String, emoji: String) -> Unit,
  onDeleteObject: (String) -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = project.title,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = Color.White
            )
            Text(
              text = "${project.objects.size} Actors / Sprites",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          IconButton(onClick = onPlay, modifier = Modifier.testTag("top_bar_play_button")) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(PocketAccentAmber, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play Project",
                tint = Color.Black
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PocketTealDark
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = PocketAccentAmber,
        contentColor = Color.Black,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Add Actor", fontWeight = FontWeight.Bold) },
        modifier = Modifier.testTag("add_actor_fab")
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(14.dp)
    ) {
      // Scene Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Scene Stage",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Coordinate bounds: X(-160..+160), Y(-280..+280)",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "Variables: ${project.variables.keys.joinToString(", ")}",
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Actors & Objects",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))

      if (project.objects.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No actors yet. Tap + Add Actor below to add a sprite!",
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(project.objects) { obj ->
            ActorItemCard(
              actor = obj,
              onClick = { onOpenObject(obj.id) },
              onDelete = { onDeleteObject(obj.id) }
            )
          }
          item { Spacer(modifier = Modifier.height(60.dp)) }
        }
      }
    }
  }

  if (showAddDialog) {
    AddObjectDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, emoji ->
        onAddObject(name, emoji)
      }
    )
  }
}

@Composable
fun ActorItemCard(
  actor: ProgramObject,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("actor_card_${actor.id}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val activeLook = actor.looks.getOrNull(actor.currentLookIndex) ?: actor.looks.firstOrNull()
      Box(
        modifier = Modifier
          .size(48.dp)
          .background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(text = activeLook?.emojiOrIcon ?: "⭐", fontSize = 26.sp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = actor.name,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.IntegrationInstructions,
              contentDescription = null,
              modifier = Modifier.size(13.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${actor.scripts.size} scripts",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.Image,
              contentDescription = null,
              modifier = Modifier.size(13.dp),
              tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${actor.looks.size} looks",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.testTag("delete_actor_${actor.id}")
      ) {
        Icon(
          Icons.Default.Delete,
          contentDescription = "Delete Actor",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      }
    }
  }
}
