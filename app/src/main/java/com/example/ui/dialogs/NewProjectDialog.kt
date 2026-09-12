package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NewProjectDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, description: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "New Pocket Code Project",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Project Title") },
          placeholder = { Text("e.g. Space Runner") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_title_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (optional)") },
          placeholder = { Text("What will your app or game do?") },
          maxLines = 3,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_desc_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onConfirm(title.trim(), description.trim())
            onDismiss()
          }
        },
        enabled = title.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_project")
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
