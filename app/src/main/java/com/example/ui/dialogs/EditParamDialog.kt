package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.SoundPlayer

@Composable
fun EditParamDialog(
  paramKey: String,
  currentValue: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  // Clean prefix if any, e.g. "X: 50" -> "50"
  val initialClean = currentValue
    .removePrefix("X: ")
    .removePrefix("Y: ")
    .removePrefix("gX: ")
    .removePrefix("gY: ")
    .removePrefix("vX: ")
    .removePrefix("vY: ")
    .removePrefix("ΔvX: ")
    .removePrefix("ΔvY: ")
    .removePrefix("fX: ")
    .removePrefix("fY: ")
    .removePrefix("Bounce: ")
    .removePrefix("Shake: ")
    .removePrefix("Intensity: ")
    .removePrefix("\"")
    .removeSuffix("\"")
    .removeSuffix(" s")
    .removeSuffix(" steps")
    .removeSuffix(" times")
    .removeSuffix(" px")
    .removeSuffix(" Hz")
    .removeSuffix(" kg")
    .removeSuffix(" ms")
    .removeSuffix(" %")
    .removeSuffix("%")
    .removeSuffix("°")
    .trim()

  var textValue by remember { mutableStateOf(initialClean) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit Parameter: ${paramKey.uppercase()}",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Enter a new value or choose from presets:",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = textValue,
          onValueChange = { textValue = it },
          label = { Text(paramKey) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("param_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick presets depending on param type
        when (paramKey) {
          "sound" -> {
            Text("Presets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            val sounds = listOf("Jump", "Laser", "Coin", "Pop", "Boom", "Win", "Beep")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(sounds) { s ->
                SuggestionChip(
                  onClick = {
                    textValue = s
                    SoundPlayer.playSound(s)
                  },
                  label = { Text(s) }
                )
              }
            }
          }
          "color" -> {
            Text("Palette:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            val colors = listOf("#00E5FF", "#FF1744", "#76FF03", "#FFD600", "#D500F9", "#FF9100", "#FFFFFF")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
              items(colors) { hex ->
                val parsed = try {
                  Color(android.graphics.Color.parseColor(hex))
                } catch (_: Exception) {
                  Color.Cyan
                }
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .background(parsed, CircleShape)
                    .border(
                      width = if (textValue.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                      color = if (textValue.equals(hex, ignoreCase = true)) Color.White else Color.Gray,
                      shape = CircleShape
                    )
                    .clickable { textValue = hex }
                )
              }
            }
          }
          "degrees" -> {
            val degs = listOf("15", "45", "90", "180", "-15", "-90")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(degs) { d ->
                SuggestionChip(onClick = { textValue = d }, label = { Text("$d°") })
              }
            }
          }
          "steps", "dx", "dy" -> {
            val steps = listOf("5", "10", "20", "50", "-10")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(steps) { s ->
                SuggestionChip(onClick = { textValue = s }, label = { Text(s) })
              }
            }
          }
          "x", "y", "targetX", "targetY" -> {
            val coords = listOf("-100", "-50", "0", "50", "100")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(coords) { c ->
                SuggestionChip(onClick = { textValue = c }, label = { Text(c) })
              }
            }
          }
          "var" -> {
            val vars = listOf("score", "gold", "lives", "speed", "bounces", "timer")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(vars) { v ->
                SuggestionChip(onClick = { textValue = v }, label = { Text(v) })
              }
            }
          }
          "op" -> {
            val ops = listOf("==", ">", "<")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(ops) { o ->
                SuggestionChip(onClick = { textValue = o }, label = { Text(o) })
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(textValue)
          onDismiss()
        },
        modifier = Modifier.testTag("confirm_param_edit")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
