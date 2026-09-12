package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun AddObjectDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, emoji: String) -> Unit
) {
  var name by remember { mutableStateOf("New Actor") }
  val emojis = listOf(
    "🐱", "🚀", "👾", "🤖", "⭐", "🪙",
    "⚽", "💎", "💥", "🧙‍♂️", "🛸", "🔥",
    "🦄", "🎯", "⚡", "🎨", "🚗", "🌟"
  )
  var selectedEmoji by remember { mutableStateOf("🐱") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Add New Actor",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Actor Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_actor_name_input")
        )

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Select Sprite Appearance:",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(6),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.height(160.dp)
        ) {
          items(emojis) { emoji ->
            val isSelected = emoji == selectedEmoji
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(8.dp)
                )
                .border(
                  width = if (isSelected) 2.dp else 0.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable { selectedEmoji = emoji },
              contentAlignment = Alignment.Center
            ) {
              Text(text = emoji, fontSize = 24.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(name.trim(), selectedEmoji)
            onDismiss()
          }
        },
        modifier = Modifier.testTag("confirm_add_actor")
      ) {
        Text("Add")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
