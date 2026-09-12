package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.CostumeLook
import com.example.model.ProgramObject
import com.example.model.Project
import com.example.model.Script
import com.example.model.SoundPlayer
import com.example.ui.components.PocketBrickView
import com.example.ui.dialogs.BrickPickerSheet
import com.example.ui.dialogs.EditParamDialog
import com.example.ui.theme.PocketAccentAmber
import com.example.ui.theme.PocketTealDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectEditorScreen(
  project: Project,
  actor: ProgramObject,
  onBack: () -> Unit,
  onPlay: () -> Unit,
  onAddScript: (BrickOp) -> Unit,
  onDeleteScript: (String) -> Unit,
  onAddBrick: (scriptId: String, Brick) -> Unit,
  onUpdateBrick: (scriptId: String, Brick) -> Unit,
  onDeleteBrick: (scriptId: String, brickId: String) -> Unit,
  onAddLook: (CostumeLook) -> Unit,
  onDeleteLook: (String) -> Unit,
  onOpenMqttConfig: (() -> Unit)? = null
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var pickingForScriptId by remember { mutableStateOf<String?>(null) }
  var showNewScriptHeaderDialog by remember { mutableStateOf(false) }
  var editingParamTarget by remember { mutableStateOf<EditingParam?>(null) }
  var showAddLookDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            val activeLook = actor.looks.getOrNull(actor.currentLookIndex) ?: actor.looks.firstOrNull()
            Text(text = activeLook?.emojiOrIcon ?: "⭐", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = actor.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
              )
              Text(
                text = "Pocket Code Script Editor",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          if (onOpenMqttConfig != null) {
            IconButton(onClick = onOpenMqttConfig, modifier = Modifier.testTag("editor_mqtt_button")) {
              Icon(Icons.Default.Hub, contentDescription = "MQTT Setup", tint = Color.White)
            }
          }
          IconButton(onClick = onPlay, modifier = Modifier.testTag("editor_play_button")) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(PocketAccentAmber, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Play Project", tint = Color.Black)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PocketTealDark)
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 3 Classic Pocket Code Tabs: Scripts, Looks, Sounds
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.IntegrationInstructions, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scripts (${actor.scripts.size})", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_scripts")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Looks (${actor.looks.size})", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_looks")
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sounds", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_sounds")
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Logs", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_logs")
        )
      }

      // Tab Contents
      when (selectedTab) {
        0 -> {
          // Scripts Tab
          ScriptsTabView(
            scripts = actor.scripts,
            onNewScript = { showNewScriptHeaderDialog = true },
            onDeleteScript = onDeleteScript,
            onAddBrickToScript = { scriptId -> pickingForScriptId = scriptId },
            onEditBrickParam = { scriptId, brick, paramKey, currentVal ->
              editingParamTarget = EditingParam(scriptId, brick, paramKey, currentVal)
            },
            onDeleteBrick = onDeleteBrick,
            onMoveBrick = { scriptId, fromIdx, toIdx ->
              val script = actor.scripts.find { it.id == scriptId } ?: return@ScriptsTabView
              if (toIdx in script.bricks.indices) {
                val list = script.bricks.toMutableList()
                val item = list.removeAt(fromIdx)
                list.add(toIdx, item)
              }
            }
          )
        }
        1 -> {
          // Looks Tab
          LooksTabView(
            looks = actor.looks,
            activeLookIndex = actor.currentLookIndex,
            onAddLook = { showAddLookDialog = true },
            onDeleteLook = onDeleteLook
          )
        }
        2 -> {
          // Sounds Tab
          SoundsTabView()
        }
        3 -> {
          // Diagnostic Logs Tab
          DiagnosticsLogView(project = project, onOpenMqttConfig = onOpenMqttConfig)
        }
      }
    }
  }

  // Brick Picker Sheet
  if (pickingForScriptId != null) {
    BrickPickerSheet(
      onDismiss = { pickingForScriptId = null },
      onSelectBrick = { brick ->
        val scriptId = pickingForScriptId ?: return@BrickPickerSheet
        onAddBrick(scriptId, brick)
      }
    )
  }

  // Parameter Editor Dialog
  val currentEdit = editingParamTarget
  if (currentEdit != null) {
    EditParamDialog(
      paramKey = currentEdit.paramKey,
      currentValue = currentEdit.currentValue,
      onDismiss = { editingParamTarget = null },
      onConfirm = { newVal ->
        val updatedBrick = applyParamToBrick(currentEdit.brick, currentEdit.paramKey, newVal)
        onUpdateBrick(currentEdit.scriptId, updatedBrick)
        editingParamTarget = null
      }
    )
  }

  // New Script Header Chooser Dialog
  if (showNewScriptHeaderDialog) {
    AlertDialog(
      onDismissRequest = { showNewScriptHeaderDialog = false },
      title = { Text("Add New Script", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Select event trigger:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Card(
            onClick = {
              onAddScript(BrickOp.WHEN_SCENE_STARTS)
              showNewScriptHeaderDialog = false
            },
            colors = CardDefaults.cardColors(containerColor = Color(BrickOp.WHEN_SCENE_STARTS.category.colorHex)),
            modifier = Modifier.fillMaxWidth().testTag("header_when_scene_starts")
          ) {
            Text("When scene starts", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
          }
          Card(
            onClick = {
              onAddScript(BrickOp.WHEN_TAPPED)
              showNewScriptHeaderDialog = false
            },
            colors = CardDefaults.cardColors(containerColor = Color(BrickOp.WHEN_TAPPED.category.colorHex)),
            modifier = Modifier.fillMaxWidth().testTag("header_when_tapped")
          ) {
            Text("When tapped", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
          }
          Card(
            onClick = {
              onAddScript(BrickOp.WHEN_BROADCAST_RECEIVED)
              showNewScriptHeaderDialog = false
            },
            colors = CardDefaults.cardColors(containerColor = Color(BrickOp.WHEN_BROADCAST_RECEIVED.category.colorHex)),
            modifier = Modifier.fillMaxWidth().testTag("header_when_broadcast")
          ) {
            Text("When I receive message", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
          }
          Card(
            onClick = {
              onAddScript(BrickOp.WHEN_MQTT_RECEIVED)
              showNewScriptHeaderDialog = false
            },
            colors = CardDefaults.cardColors(containerColor = Color(BrickOp.WHEN_MQTT_RECEIVED.category.colorHex)),
            modifier = Modifier.fillMaxWidth().testTag("header_when_mqtt")
          ) {
            Text("When MQTT message arrives (broker.emqx.io)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showNewScriptHeaderDialog = false }) { Text("Cancel") }
      }
    )
  }

  // Add Look Dialog
  if (showAddLookDialog) {
    AddLookDialog(
      onDismiss = { showAddLookDialog = false },
      onConfirm = { name, emoji ->
        onAddLook(CostumeLook(name = name, emojiOrIcon = emoji))
      }
    )
  }
}

private data class EditingParam(
  val scriptId: String,
  val brick: Brick,
  val paramKey: String,
  val currentValue: String
)

private fun applyParamToBrick(brick: Brick, key: String, value: String): Brick {
  val floatVal = value.toFloatOrNull() ?: 0f
  return when (key) {
    "seconds", "sec" -> brick.copy(paramNum1 = floatVal)
    "times" -> brick.copy(paramNum1 = floatVal)
    "steps" -> brick.copy(paramNum1 = floatVal)
    "degrees" -> brick.copy(paramNum1 = floatVal)
    "dx", "gx", "vx", "dvx", "fx", "panX" -> brick.copy(paramNum1 = floatVal)
    "dy", "gy", "vy", "dvy", "fy", "panY" -> brick.copy(paramNum2 = floatVal)
    "x" -> brick.copy(paramNum1 = floatVal)
    "y" -> brick.copy(paramNum2 = floatVal)
    "targetX" -> brick.copy(paramString1 = value)
    "targetY" -> brick.copy(paramNum2 = floatVal)
    "size", "deltaSize", "bounce", "mass", "zoom", "shake", "ms" -> brick.copy(paramNum1 = floatVal)
    "text", "prompt", "speech", "role", "url", "videoUrl" -> brick.copy(paramString1 = value)
    "intoVar" -> brick.copy(paramString2 = value)
    "topic" -> brick.copy(paramString1 = value)
    "payload" -> brick.copy(paramString2 = value)
    "sound" -> brick.copy(paramString1 = value)
    "freq" -> brick.copy(paramNum1 = floatVal)
    "var" -> brick.copy(paramString1 = value)
    "val", "delta" -> brick.copy(paramNum1 = floatVal)
    "op" -> brick.copy(paramString2 = value)
    "msg" -> brick.copy(paramString1 = value)
    "color" -> brick.copy(paramString1 = value)
    else -> brick
  }
}

@Composable
private fun ScriptsTabView(
  scripts: List<Script>,
  onNewScript: () -> Unit,
  onDeleteScript: (String) -> Unit,
  onAddBrickToScript: (String) -> Unit,
  onEditBrickParam: (scriptId: String, Brick, paramKey: String, currentValue: String) -> Unit,
  onDeleteBrick: (scriptId: String, brickId: String) -> Unit,
  onMoveBrick: (scriptId: String, fromIdx: Int, toIdx: Int) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Code Scripts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Button(
          onClick = onNewScript,
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PocketAccentAmber),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("add_script_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Script", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (scripts.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🧩", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No scripts yet", fontWeight = FontWeight.Bold)
            Text(
              "Tap 'New Script' above to start coding behavior for this actor!",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(scripts) { script ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("script_stack_${script.id}"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            // Script header block
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              PocketBrickView(
                brick = script.header,
                isHeader = true,
                onEditParam = { key, curr ->
                  onEditBrickParam(script.id, script.header, key, curr)
                },
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = { onDeleteScript(script.id) },
                modifier = Modifier.testTag("delete_script_${script.id}")
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Script", tint = MaterialTheme.colorScheme.error)
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Body bricks
            script.bricks.forEachIndexed { idx, brick ->
              PocketBrickView(
                brick = brick,
                onEditParam = { key, curr ->
                  onEditBrickParam(script.id, brick, key, curr)
                },
                onDelete = { onDeleteBrick(script.id, brick.id) },
                onMoveUp = if (idx > 0) { { onMoveBrick(script.id, idx, idx - 1) } } else null,
                onMoveDown = if (idx < script.bricks.size - 1) { { onMoveBrick(script.id, idx, idx + 1) } } else null
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add brick button under script stack
            OutlinedButton(
              onClick = { onAddBrickToScript(script.id) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("add_brick_to_script_${script.id}")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add Brick", fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
    item { Spacer(modifier = Modifier.height(60.dp)) }
  }
}

@Composable
private fun LooksTabView(
  looks: List<CostumeLook>,
  activeLookIndex: Int,
  onAddLook: () -> Unit,
  onDeleteLook: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Costumes & Looks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Button(
          onClick = onAddLook,
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("add_look_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Look")
        }
      }
    }

    itemsIndexed(looks) { index, look ->
      val isActive = index == activeLookIndex
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .background(Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = look.emojiOrIcon, fontSize = 28.sp)
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = look.name,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isActive) "Active Costume" else "Costume #${index + 1}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (looks.size > 1) {
            IconButton(onClick = { onDeleteLook(look.id) }) {
              Icon(Icons.Default.Delete, contentDescription = "Delete Costume", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SoundsTabView() {
  val sampleSounds = listOf(
    Pair("Jump", "High pitch boing jump effect"),
    Pair("Laser", "Sci-fi retro laser zap"),
    Pair("Coin", "Two-tone metallic chime"),
    Pair("Pop", "Crisp bubble pop"),
    Pair("Boom", "Deep explosive rumble"),
    Pair("Win", "Joyful fanfare chime"),
    Pair("Beep", "Clean synthesized electronic beep")
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text("Audio Synthesizer & Sound Effects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text("Real-time low-latency synthesized sounds playable by 'Play Sound' bricks:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    items(sampleSounds) { (name, desc) ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(Color(0xFFDB2777), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Button(
            onClick = { SoundPlayer.playSound(name) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("preview_sound_$name")
          ) {
            Text("Test")
          }
        }
      }
    }
  }
}

@Composable
fun AddLookDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, emoji: String) -> Unit
) {
  var name by remember { mutableStateOf("New Look") }
  val emojis = listOf("😸", "🚀", "💥", "⚡", "🌟", "🎉", "🔥", "🛡️", "🧙", "👻", "👾", "💎")
  var selectedEmoji by remember { mutableStateOf("😸") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Costume Look", fontWeight = FontWeight.Bold) },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Costume Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          emojis.take(6).forEach { em ->
            Box(
              modifier = Modifier
                .size(38.dp)
                .background(
                  if (em == selectedEmoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  RoundedCornerShape(6.dp)
                )
                .clickable { selectedEmoji = em },
              contentAlignment = Alignment.Center
            ) {
              Text(em, fontSize = 20.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        if (name.isNotBlank()) {
          onConfirm(name.trim(), selectedEmoji)
          onDismiss()
        }
      }) {
        Text("Add")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
