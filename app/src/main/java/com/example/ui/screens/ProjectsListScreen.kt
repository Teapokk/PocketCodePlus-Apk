package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.repository.CatrobatFileManager
import com.example.ui.dialogs.NewProjectDialog
import com.example.ui.theme.PocketAccentAmber
import com.example.ui.theme.PocketTealDark
import com.example.ui.theme.PocketTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
  projects: List<Project>,
  onOpenProject: (String) -> Unit,
  onPlayProject: (String) -> Unit,
  onCreateProject: (title: String, description: String) -> Unit,
  onDuplicateProject: (String) -> Unit,
  onDeleteProject: (String) -> Unit,
  onImportProject: ((Project) -> Unit)? = null,
  onOpenMqttConfig: (() -> Unit)? = null
) {
  val context = LocalContext.current
  var showNewDialog by remember { mutableStateOf(false) }
  var mainTab by remember { mutableIntStateOf(0) } // 0: Projects, 1: Credits
  var projectFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Featured, 2: My Projects
  var searchQuery by remember { mutableStateOf("") }

  val filteredProjects = projects.filter {
    (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)) &&
      when (projectFilterTab) {
        1 -> it.isPreset
        2 -> !it.isPreset
        else -> true
      }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = PocketAccentAmber,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.size(34.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "+",
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.Black
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "PocketCodePlus",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = Color.White
              )
              Text(
                text = "Catrobat Visual Programming Mod",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
          }
        },
        actions = {
          // Import Demo/PCP project action
          IconButton(
            onClick = {
              // Demonstrate import of a bundled PCP package
              val sample = projects.firstOrNull() ?: return@IconButton
              val pcpBytes = CatrobatFileManager.exportToPcp(context, sample)
              val imported = CatrobatFileManager.importFromPcp(context, pcpBytes)
              if (imported != null) {
                val renamed = imported.copy(title = "${imported.title} (Imported .pcp)")
                onImportProject?.invoke(renamed)
                Toast.makeText(context, "Successfully imported .pcp project package!", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.testTag("import_project_button")
          ) {
            Icon(Icons.Default.FileUpload, contentDescription = "Import .pcp or .catrobat", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PocketTealDark)
      )
    },
    floatingActionButton = {
      if (mainTab == 0) {
        FloatingActionButton(
          onClick = { showNewDialog = true },
          containerColor = PocketAccentAmber,
          contentColor = Color.Black,
          modifier = Modifier.testTag("create_project_fab")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Project")
            Spacer(modifier = Modifier.width(6.dp))
            Text("New Project", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Primary Navigation Tabs: Projects, Credits
      TabRow(
        selectedTabIndex = mainTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = mainTab == 0,
          onClick = { mainTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Projects (${projects.size})", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("nav_tab_projects")
        )
        Tab(
          selected = mainTab == 1,
          onClick = { mainTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Credits", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("nav_tab_credits")
        )
      }

      when (mainTab) {
        0 -> {
          // Projects View
          ProjectsTabContent(
            projects = filteredProjects,
            totalProjectsCount = projects.size,
            selectedFilterTab = projectFilterTab,
            onSelectFilterTab = { projectFilterTab = it },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onOpenProject = onOpenProject,
            onPlayProject = onPlayProject,
            onDuplicateProject = onDuplicateProject,
            onDeleteProject = onDeleteProject
          )
        }
        1 -> {
          // Giant Credits Tab
          CreditsScreen()
        }
      }
    }
  }

  if (showNewDialog) {
    NewProjectDialog(
      onDismiss = { showNewDialog = false },
      onConfirm = { title, desc ->
        onCreateProject(title, desc)
      }
    )
  }
}

@Composable
private fun ProjectsTabContent(
  projects: List<Project>,
  totalProjectsCount: Int,
  selectedFilterTab: Int,
  onSelectFilterTab: (Int) -> Unit,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onOpenProject: (String) -> Unit,
  onPlayProject: (String) -> Unit,
  onDuplicateProject: (String) -> Unit,
  onDeleteProject: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Vector Hero Banner (Strictly code & compose visuals - No AI image artifacts)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = PocketTealDark),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.horizontalGradient(
              colors = listOf(PocketTealDark, Color(0xFF004D40), Color(0xFF0F172A))
            )
          )
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = PocketAccentAmber,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "POCKETCODE+",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.Black,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Physics • MQTT • AI",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Create • Code • Play",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 17.sp
            )
            Text(
              text = "Supports .pcp and .catrobat packages with real-time diagnostics.",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }

          Surface(
            color = Color.White.copy(alpha = 0.12f),
            shape = CircleShape,
            modifier = Modifier.size(46.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = "🚀", fontSize = 22.sp)
            }
          }
        }
      }
    }

    // Secondary Filter Tabs (All, Featured, My Projects)
    TabRow(
      selectedTabIndex = selectedFilterTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      Tab(
        selected = selectedFilterTab == 0,
        onClick = { onSelectFilterTab(0) },
        text = { Text("All ($totalProjectsCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
      )
      Tab(
        selected = selectedFilterTab == 1,
        onClick = { onSelectFilterTab(1) },
        text = { Text("Featured Mods", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
      )
      Tab(
        selected = selectedFilterTab == 2,
        onClick = { onSelectFilterTab(2) },
        text = { Text("My Projects", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
      )
    }

    // Search Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search projects by name or blocks...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_projects_input")
      )
    }

    // Projects List
    if (projects.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "📦", fontSize = 48.sp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No projects found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Tap the + button below to create your first PocketCodePlus project!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }
        items(projects) { project ->
          ProjectCard(
            project = project,
            onOpen = { onOpenProject(project.id) },
            onPlay = { onPlayProject(project.id) },
            onDuplicate = { onDuplicateProject(project.id) },
            onDelete = { onDeleteProject(project.id) }
          )
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
      }
    }
  }
}

@Composable
fun ProjectCard(
  project: Project,
  onOpen: () -> Unit,
  onPlay: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit
) {
  val context = LocalContext.current
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onOpen() }
      .testTag("project_card_${project.id}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
          val previewEmoji = project.objects.firstOrNull()?.looks?.firstOrNull()?.emojiOrIcon ?: "📱"
          Box(
            modifier = Modifier
              .size(46.dp)
              .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(text = previewEmoji, fontSize = 24.sp)
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = project.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (project.isPreset) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = PocketTealPrimary,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "FEATURED",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${project.objects.size} Sprites • ${project.objects.sumOf { it.scripts.size }} Scripts • ${project.objects.sumOf { o -> o.scripts.sumOf { s -> s.bricks.size } }} Blocks",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Box {
          IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
          }
          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text("Export .pcp Archive") },
              leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
              onClick = {
                menuExpanded = false
                val bytes = CatrobatFileManager.exportToPcp(context, project)
                Toast.makeText(context, "Saved ${bytes.size} bytes to ${project.title.lowercase().replace(" ", "_")}.pcp", Toast.LENGTH_LONG).show()
              }
            )
            DropdownMenuItem(
              text = { Text("Export .catrobat Archive") },
              leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
              onClick = {
                menuExpanded = false
                val bytes = CatrobatFileManager.exportToCatrobat(context, project)
                Toast.makeText(context, "Saved ${bytes.size} bytes to ${project.title.lowercase().replace(" ", "_")}.catrobat", Toast.LENGTH_LONG).show()
              }
            )
            DropdownMenuItem(
              text = { Text("Duplicate") },
              leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onDuplicate()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete") },
              leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
              onClick = {
                menuExpanded = false
                onDelete()
              }
            )
          }
        }
      }

      if (project.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = project.description,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onPlay,
          colors = ButtonDefaults.buttonColors(containerColor = PocketTealPrimary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("play_project_${project.id}")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Run Game", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Play Project", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onOpen,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("edit_project_${project.id}")
        ) {
          Icon(Icons.Default.Code, contentDescription = "Edit Code", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Code & Sprites")
        }
      }
    }
  }
}
