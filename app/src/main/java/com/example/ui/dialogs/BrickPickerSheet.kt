package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.model.Brick
import com.example.model.BrickCategory
import com.example.model.BrickOp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrickPickerSheet(
  onDismiss: () -> Unit,
  onSelectBrick: (Brick) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedCategory by remember { mutableStateOf<BrickCategory?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("brick_picker_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Sheet title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Select a Brick",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Choose a code block to add to your script",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_brick_picker")) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Category filters
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { selectedCategory = null },
            label = { Text("All Bricks") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        }
        items(BrickCategory.values()) { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(Color(cat.colorHex), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(cat.displayName)
              }
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(cat.colorHex),
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bricks list
      val allOps = BrickOp.values().filter { op ->
        // Only allow non-header bricks in regular script body
        op !in listOf(BrickOp.WHEN_SCENE_STARTS, BrickOp.WHEN_TAPPED, BrickOp.WHEN_BROADCAST_RECEIVED, BrickOp.WHEN_MQTT_RECEIVED)
      }.filter { op ->
        selectedCategory == null || op.category == selectedCategory
      }

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(420.dp)
      ) {
        items(allOps) { op ->
          val categoryColor = Color(op.category.colorHex)
          Card(
            onClick = {
              val brick = createDefaultBrickForOp(op)
              onSelectBrick(brick)
              onDismiss()
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("brick_option_${op.name}"),
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
                  .size(14.dp)
                  .background(categoryColor, CircleShape)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = op.defaultLabel,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = op.category.displayName,
                  fontSize = 12.sp,
                  color = categoryColor,
                  fontWeight = FontWeight.Medium
                )
              }
              Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}

private fun createDefaultBrickForOp(op: BrickOp): Brick {
  return when (op) {
    BrickOp.WAIT_SECONDS -> Brick(op = op, paramNum1 = 1.0f)
    BrickOp.REPEAT_TIMES -> Brick(op = op, paramNum1 = 5f)
    BrickOp.FOREVER -> Brick(op = op)
    BrickOp.IF_VARIABLE -> Brick(op = op, paramString1 = "score", paramString2 = ">", paramNum1 = 10f)
    BrickOp.PLACE_AT_XY -> Brick(op = op, paramNum1 = 0f, paramNum2 = 0f)
    BrickOp.MOVE_STEPS -> Brick(op = op, paramNum1 = 10f)
    BrickOp.TURN_DEGREES -> Brick(op = op, paramNum1 = 15f)
    BrickOp.POINT_IN_DIRECTION -> Brick(op = op, paramNum1 = 90f)
    BrickOp.BOUNCE_IF_ON_EDGE -> Brick(op = op)
    BrickOp.CHANGE_X_BY -> Brick(op = op, paramNum1 = 10f)
    BrickOp.CHANGE_Y_BY -> Brick(op = op, paramNum1 = 10f)
    BrickOp.SET_X_TO -> Brick(op = op, paramNum1 = 0f)
    BrickOp.SET_Y_TO -> Brick(op = op, paramNum1 = 0f)
    BrickOp.GLIDE_TO_XY -> Brick(op = op, paramNum1 = 1.0f, paramString1 = "50", paramNum2 = 50f)
    BrickOp.NEXT_LOOK -> Brick(op = op)
    BrickOp.SET_LOOK -> Brick(op = op, paramNum1 = 0f)
    BrickOp.SET_SIZE_PERCENT -> Brick(op = op, paramNum1 = 100f)
    BrickOp.CHANGE_SIZE_BY -> Brick(op = op, paramNum1 = 10f)
    BrickOp.SHOW -> Brick(op = op)
    BrickOp.HIDE -> Brick(op = op)
    BrickOp.SAY_TEXT -> Brick(op = op, paramString1 = "Awesome!", paramNum1 = 1.5f)
    BrickOp.PLAY_SOUND -> Brick(op = op, paramString1 = "Jump")
    BrickOp.PLAY_NOTE_TONE -> Brick(op = op, paramNum1 = 440f, paramNum2 = 0.2f)
    BrickOp.SET_VARIABLE -> Brick(op = op, paramString1 = "score", paramNum1 = 0f)
    BrickOp.CHANGE_VARIABLE_BY -> Brick(op = op, paramString1 = "score", paramNum1 = 1f)
    BrickOp.SHOW_VARIABLE -> Brick(op = op, paramString1 = "score")
    BrickOp.BROADCAST -> Brick(op = op, paramString1 = "game_event")
    BrickOp.PEN_DOWN -> Brick(op = op)
    BrickOp.PEN_UP -> Brick(op = op)
    BrickOp.SET_PEN_COLOR -> Brick(op = op, paramString1 = "#00E5FF")
    BrickOp.SET_PEN_SIZE -> Brick(op = op, paramNum1 = 5f)
    BrickOp.CLEAR_PEN -> Brick(op = op)

    // Physics (Newcatroid)
    BrickOp.SET_GRAVITY -> Brick(op = op, paramNum1 = 0f, paramNum2 = -400f)
    BrickOp.SET_VELOCITY -> Brick(op = op, paramNum1 = 50f, paramNum2 = 100f)
    BrickOp.CHANGE_VELOCITY_BY -> Brick(op = op, paramNum1 = 10f, paramNum2 = 20f)
    BrickOp.SET_BOUNCE_ELASTICITY -> Brick(op = op, paramNum1 = 75f)
    BrickOp.SET_MASS -> Brick(op = op, paramNum1 = 1f)
    BrickOp.APPLY_IMPULSE -> Brick(op = op, paramNum1 = 0f, paramNum2 = 250f)

    // Camera & 3D
    BrickOp.FIX_CAMERA_TO_SPRITE -> Brick(op = op)
    BrickOp.SET_CAMERA_ZOOM -> Brick(op = op, paramNum1 = 120f)
    BrickOp.PAN_CAMERA -> Brick(op = op, paramNum1 = 0f, paramNum2 = 50f)
    BrickOp.SHAKE_CAMERA -> Brick(op = op, paramNum1 = 15f)
    BrickOp.RESET_CAMERA -> Brick(op = op)

    // AI
    BrickOp.AI_ASK_PROMPT -> Brick(op = op, paramString1 = "Tell me a fun game quest", paramString2 = "ai_response")
    BrickOp.AI_CLASSIFY_TEXT -> Brick(op = op, paramString1 = "I win this game!", paramString2 = "sentiment")
    BrickOp.AI_GENERATE_NPC_DIALOGUE -> Brick(op = op, paramString1 = "Wise Wizard")

    // Network & MQTT
    BrickOp.MQTT_CONNECT -> Brick(op = op)
    BrickOp.MQTT_PUBLISH -> Brick(op = op, paramString1 = "pocketcode/game/score", paramString2 = "score=100")
    BrickOp.WHEN_MQTT_RECEIVED -> Brick(op = op, paramString1 = "pocketcode/game/score")
    BrickOp.HTTP_GET_REQUEST -> Brick(op = op, paramString1 = "https://catrobat.org/api/status", paramString2 = "web_data")
    BrickOp.OPEN_WEBVIEW -> Brick(op = op, paramString1 = "https://catrobat.org")
    BrickOp.PLAY_VIDEO_URL -> Brick(op = op, paramString1 = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")

    // Device
    BrickOp.VIBRATE_DEVICE -> Brick(op = op, paramNum1 = 80f)
    BrickOp.TEXT_TO_SPEECH -> Brick(op = op, paramString1 = "PocketCodePlus is ready!")
    else -> Brick(op = op)
  }
}
