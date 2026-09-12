package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.CostumeLook
import com.example.repository.MqttConfigRepository
import com.example.repository.ProjectRepository
import com.example.ui.screens.MqttConfigScreen
import com.example.ui.screens.ObjectEditorScreen
import com.example.ui.screens.ProjectOverviewScreen
import com.example.ui.screens.ProjectsListScreen
import com.example.ui.screens.StagePlayerScreen

sealed class NavScreen {
  object ProjectsList : NavScreen()
  data class ProjectOverview(val projectId: String) : NavScreen()
  data class ObjectEditor(val projectId: String, val objectId: String) : NavScreen()
  data class StagePlayer(val projectId: String) : NavScreen()
  data class MqttConfig(val returnTo: NavScreen = ProjectsList) : NavScreen()
}

@Composable
fun PocketCodeApp(
  repository: ProjectRepository = remember { ProjectRepository() }
) {
  val context = LocalContext.current
  val mqttRepository = remember { MqttConfigRepository.getInstance(context) }
  val projects by repository.projects.collectAsState()
  var currentScreen by remember { mutableStateOf<NavScreen>(NavScreen.ProjectsList) }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    when (val screen = currentScreen) {
      is NavScreen.ProjectsList -> {
        ProjectsListScreen(
          projects = projects,
          onOpenProject = { id -> currentScreen = NavScreen.ProjectOverview(id) },
          onPlayProject = { id -> currentScreen = NavScreen.StagePlayer(id) },
          onCreateProject = { title, desc ->
            val newProj = repository.createProject(title, desc)
            currentScreen = NavScreen.ProjectOverview(newProj.id)
          },
          onDuplicateProject = { id -> repository.duplicateProject(id) },
          onDeleteProject = { id -> repository.deleteProject(id) },
          onImportProject = { imported -> repository.importProject(imported) },
          onOpenMqttConfig = { currentScreen = NavScreen.MqttConfig(currentScreen) }
        )
      }

      is NavScreen.ProjectOverview -> {
        BackHandler { currentScreen = NavScreen.ProjectsList }
        val project = projects.find { it.id == screen.projectId }
        if (project != null) {
          ProjectOverviewScreen(
            project = project,
            onBack = { currentScreen = NavScreen.ProjectsList },
            onOpenObject = { objId ->
              currentScreen = NavScreen.ObjectEditor(project.id, objId)
            },
            onPlay = { currentScreen = NavScreen.StagePlayer(project.id) },
            onAddObject = { name, emoji ->
              repository.addObject(project.id, name, emoji)
            },
            onDeleteObject = { objId ->
              repository.deleteObject(project.id, objId)
            }
          )
        } else {
          currentScreen = NavScreen.ProjectsList
        }
      }

      is NavScreen.ObjectEditor -> {
        BackHandler { currentScreen = NavScreen.ProjectOverview(screen.projectId) }
        val project = projects.find { it.id == screen.projectId }
        val actor = project?.objects?.find { it.id == screen.objectId }
        if (project != null && actor != null) {
          ObjectEditorScreen(
            project = project,
            actor = actor,
            onBack = { currentScreen = NavScreen.ProjectOverview(project.id) },
            onPlay = { currentScreen = NavScreen.StagePlayer(project.id) },
            onOpenMqttConfig = { currentScreen = NavScreen.MqttConfig(currentScreen) },
            onAddScript = { headerOp ->
              repository.addScript(project.id, actor.id, headerOp)
            },
            onDeleteScript = { scriptId ->
              repository.deleteScript(project.id, actor.id, scriptId)
            },
            onAddBrick = { scriptId, brick ->
              repository.addBrickToScript(project.id, actor.id, scriptId, brick)
            },
            onUpdateBrick = { scriptId, brick ->
              repository.updateBrickInScript(project.id, actor.id, scriptId, brick)
            },
            onDeleteBrick = { scriptId, brickId ->
              repository.deleteBrickFromScript(project.id, actor.id, scriptId, brickId)
            },
            onAddLook = { look ->
              repository.addLookToObject(project.id, actor.id, look)
            },
            onDeleteLook = { lookId ->
              repository.deleteLookFromObject(project.id, actor.id, lookId)
            }
          )
        } else {
          currentScreen = NavScreen.ProjectsList
        }
      }

      is NavScreen.StagePlayer -> {
        BackHandler { currentScreen = NavScreen.ProjectOverview(screen.projectId) }
        val project = projects.find { it.id == screen.projectId }
        if (project != null) {
          StagePlayerScreen(
            project = project,
            onBack = { currentScreen = NavScreen.ProjectOverview(project.id) },
            onOpenMqttConfig = { currentScreen = NavScreen.MqttConfig(currentScreen) }
          )
        } else {
          currentScreen = NavScreen.ProjectsList
        }
      }

      is NavScreen.MqttConfig -> {
        BackHandler { currentScreen = screen.returnTo }
        MqttConfigScreen(
          repository = mqttRepository,
          onBack = { currentScreen = screen.returnTo }
        )
      }
    }
  }
}
