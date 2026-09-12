package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Brick
import com.example.model.BrickCategory
import com.example.model.BrickOp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PocketBrickView(
  brick: Brick,
  isHeader: Boolean = false,
  onEditParam: ((paramKey: String, currentValue: String) -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
  onMoveUp: (() -> Unit)? = null,
  onMoveDown: (() -> Unit)? = null,
  onAddNestedBrick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val categoryColor = Color(brick.op.category.colorHex)
  val darkOverlay = categoryColor.copy(alpha = 0.85f)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
      .testTag("brick_${brick.id}"),
    shape = RoundedCornerShape(
      topStart = if (isHeader) 14.dp else 6.dp,
      topEnd = if (isHeader) 14.dp else 6.dp,
      bottomStart = 8.dp,
      bottomEnd = 8.dp
    ),
    colors = CardDefaults.cardColors(containerColor = categoryColor),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isHeader) 4.dp else 2.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
      // Top row: Header tab / Category name & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Brick category badge
          Surface(
            color = Color.Black.copy(alpha = 0.25f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = brick.op.category.displayName.uppercase(),
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = brick.op.defaultLabel,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
          )
        }

        // Action icons (Reorder / Delete)
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onMoveUp != null) {
            IconButton(
              onClick = onMoveUp,
              modifier = Modifier.size(28.dp).testTag("brick_move_up_${brick.id}")
            ) {
              Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Move Up",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
              )
            }
          }
          if (onMoveDown != null) {
            IconButton(
              onClick = onMoveDown,
              modifier = Modifier.size(28.dp).testTag("brick_move_down_${brick.id}")
            ) {
              Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Move Down",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
              )
            }
          }
          if (onDelete != null && !isHeader) {
            IconButton(
              onClick = onDelete,
              modifier = Modifier.size(28.dp).testTag("brick_delete_${brick.id}")
            ) {
              Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Brick",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Parameters row (Interactive editable parameter pills)
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        when (brick.op) {
          BrickOp.WAIT_SECONDS -> {
            ParamPill("seconds", "${brick.paramNum1} s", onEditParam)
          }
          BrickOp.REPEAT_TIMES -> {
            ParamPill("times", "${brick.paramNum1.toInt()} times", onEditParam)
          }
          BrickOp.IF_VARIABLE -> {
            ParamPill("var", brick.paramString1.ifBlank { "score" }, onEditParam)
            ParamPill("op", brick.paramString2.ifBlank { ">" }, onEditParam)
            ParamPill("val", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.PLACE_AT_XY -> {
            ParamPill("x", "X: ${brick.paramNum1}", onEditParam)
            ParamPill("y", "Y: ${brick.paramNum2}", onEditParam)
          }
          BrickOp.MOVE_STEPS -> {
            ParamPill("steps", "${brick.paramNum1} steps", onEditParam)
          }
          BrickOp.TURN_DEGREES -> {
            ParamPill("degrees", "${brick.paramNum1}°", onEditParam)
          }
          BrickOp.POINT_IN_DIRECTION -> {
            ParamPill("degrees", "${brick.paramNum1}°", onEditParam)
          }
          BrickOp.CHANGE_X_BY -> {
            ParamPill("dx", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.CHANGE_Y_BY -> {
            ParamPill("dy", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.SET_X_TO -> {
            ParamPill("x", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.SET_Y_TO -> {
            ParamPill("y", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.GLIDE_TO_XY -> {
            ParamPill("sec", "${brick.paramNum1} s", onEditParam)
            ParamPill("targetX", "X: ${brick.paramString1.ifBlank { "0" }}", onEditParam)
            ParamPill("targetY", "Y: ${brick.paramNum2}", onEditParam)
          }
          BrickOp.SET_SIZE_PERCENT -> {
            ParamPill("size", "${brick.paramNum1.toInt()} %", onEditParam)
          }
          BrickOp.CHANGE_SIZE_BY -> {
            ParamPill("deltaSize", "${brick.paramNum1.toInt()} %", onEditParam)
          }
          BrickOp.SAY_TEXT -> {
            ParamPill("text", "\"${brick.paramString1.ifBlank { "Hello!" }}\"", onEditParam)
            ParamPill("sec", "${brick.paramNum1} s", onEditParam)
          }
          BrickOp.PLAY_SOUND -> {
            ParamPill("sound", brick.paramString1.ifBlank { "Pop" }, onEditParam)
          }
          BrickOp.PLAY_NOTE_TONE -> {
            ParamPill("freq", "${brick.paramNum1.toInt()} Hz", onEditParam)
            ParamPill("sec", "${brick.paramNum2} s", onEditParam)
          }
          BrickOp.SET_VARIABLE -> {
            ParamPill("var", brick.paramString1.ifBlank { "score" }, onEditParam)
            ParamPill("val", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.CHANGE_VARIABLE_BY -> {
            ParamPill("var", brick.paramString1.ifBlank { "score" }, onEditParam)
            ParamPill("delta", "${brick.paramNum1}", onEditParam)
          }
          BrickOp.SHOW_VARIABLE -> {
            ParamPill("var", brick.paramString1.ifBlank { "score" }, onEditParam)
          }
          BrickOp.WHEN_BROADCAST_RECEIVED, BrickOp.BROADCAST -> {
            ParamPill("msg", "\"${brick.paramString1.ifBlank { "message1" }}\"", onEditParam)
          }
          BrickOp.SET_PEN_COLOR -> {
            ParamPill("color", brick.paramString1.ifBlank { "#00E5FF" }, onEditParam)
          }
          BrickOp.SET_PEN_SIZE -> {
            ParamPill("size", "${brick.paramNum1.toInt()} px", onEditParam)
          }

          // Physics (Newcatroid)
          BrickOp.SET_GRAVITY -> {
            ParamPill("gx", "gX: ${brick.paramNum1}", onEditParam)
            ParamPill("gy", "gY: ${brick.paramNum2}", onEditParam)
          }
          BrickOp.SET_VELOCITY -> {
            ParamPill("vx", "vX: ${brick.paramNum1}", onEditParam)
            ParamPill("vy", "vY: ${brick.paramNum2}", onEditParam)
          }
          BrickOp.CHANGE_VELOCITY_BY -> {
            ParamPill("dvx", "ΔvX: ${brick.paramNum1}", onEditParam)
            ParamPill("dvy", "ΔvY: ${brick.paramNum2}", onEditParam)
          }
          BrickOp.SET_BOUNCE_ELASTICITY -> {
            ParamPill("bounce", "Bounce: ${brick.paramNum1.toInt()}%", onEditParam)
          }
          BrickOp.SET_MASS -> {
            ParamPill("mass", "${brick.paramNum1} kg", onEditParam)
          }
          BrickOp.APPLY_IMPULSE -> {
            ParamPill("fx", "fX: ${brick.paramNum1}", onEditParam)
            ParamPill("fy", "fY: ${brick.paramNum2}", onEditParam)
          }

          // Camera & 3D
          BrickOp.SET_CAMERA_ZOOM -> {
            ParamPill("zoom", "${brick.paramNum1.toInt()}%", onEditParam)
          }
          BrickOp.PAN_CAMERA -> {
            ParamPill("panX", "X: ${brick.paramNum1}", onEditParam)
            ParamPill("panY", "Y: ${brick.paramNum2}", onEditParam)
          }
          BrickOp.SHAKE_CAMERA -> {
            ParamPill("shake", "Shake: ${brick.paramNum1.toInt()}", onEditParam)
          }

          // AI
          BrickOp.AI_ASK_PROMPT -> {
            ParamPill("prompt", "\"${brick.paramString1.ifBlank { "Ask question..." }}\"", onEditParam)
            ParamPill("intoVar", brick.paramString2.ifBlank { "ai_response" }, onEditParam)
          }
          BrickOp.AI_CLASSIFY_TEXT -> {
            ParamPill("text", "\"${brick.paramString1.ifBlank { "Text to analyze" }}\"", onEditParam)
            ParamPill("intoVar", brick.paramString2.ifBlank { "sentiment" }, onEditParam)
          }
          BrickOp.AI_GENERATE_NPC_DIALOGUE -> {
            ParamPill("role", "\"${brick.paramString1.ifBlank { "Wise Wizard" }}\"", onEditParam)
          }

          // Network & MQTT
          BrickOp.MQTT_PUBLISH -> {
            ParamPill("topic", "\"${brick.paramString1.ifBlank { "pocketcode/topic" }}\"", onEditParam)
            ParamPill("payload", "\"${brick.paramString2.ifBlank { "payload" }}\"", onEditParam)
          }
          BrickOp.WHEN_MQTT_RECEIVED -> {
            ParamPill("topic", "\"${brick.paramString1.ifBlank { "pocketcode/topic" }}\"", onEditParam)
          }
          BrickOp.HTTP_GET_REQUEST -> {
            ParamPill("url", "\"${brick.paramString1.ifBlank { "https://api..." }}\"", onEditParam)
            ParamPill("intoVar", brick.paramString2.ifBlank { "web_data" }, onEditParam)
          }
          BrickOp.OPEN_WEBVIEW -> {
            ParamPill("url", "\"${brick.paramString1.ifBlank { "https://catrobat.org" }}\"", onEditParam)
          }
          BrickOp.PLAY_VIDEO_URL -> {
            ParamPill("videoUrl", "\"${brick.paramString1.ifBlank { "YouTube / Video URL" }}\"", onEditParam)
          }

          // Device
          BrickOp.VIBRATE_DEVICE -> {
            ParamPill("ms", "${brick.paramNum1.toInt()} ms", onEditParam)
          }
          BrickOp.TEXT_TO_SPEECH -> {
            ParamPill("speech", "\"${brick.paramString1.ifBlank { "PocketCodePlus" }}\"", onEditParam)
          }
          else -> {}
        }
      }

      // Compound brick contents (Repeat, Forever, If)
      if (brick.childBricks.isNotEmpty() || brick.op in listOf(BrickOp.FOREVER, BrickOp.REPEAT_TIMES, BrickOp.IF_VARIABLE)) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(start = 12.dp, top = 6.dp, end = 6.dp, bottom = 6.dp)
        ) {
          Column {
            if (brick.childBricks.isEmpty()) {
              Text(
                text = "Empty loop / block body",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
              )
            } else {
              brick.childBricks.forEach { child ->
                PocketBrickView(
                  brick = child,
                  onEditParam = onEditParam,
                  modifier = Modifier.padding(vertical = 2.dp)
                )
              }
            }
            if (onAddNestedBrick != null) {
              Surface(
                onClick = onAddNestedBrick,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 4.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = "Add nested brick", tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add Brick Inside", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ParamPill(
  key: String,
  displayValue: String,
  onEdit: ((paramKey: String, currentValue: String) -> Unit)?
) {
  Surface(
    onClick = { onEdit?.invoke(key, displayValue) },
    shape = RoundedCornerShape(12.dp),
    color = Color.White,
    shadowElevation = 1.dp,
    modifier = Modifier.testTag("param_pill_$key")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = displayValue,
        color = Color(0xFF1E293B),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      if (onEdit != null) {
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
          Icons.Default.Edit,
          contentDescription = "Edit $key",
          tint = Color(0xFF64748B),
          modifier = Modifier.size(11.dp)
        )
      }
    }
  }
}
