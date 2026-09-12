package com.example.repository

import android.content.Context
import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.CostumeLook
import com.example.model.DefaultProjects
import com.example.model.ProgramObject
import com.example.model.Project
import com.example.model.Script
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ProjectRepository(private val context: Context? = null) {
  private val _projects = MutableStateFlow<List<Project>>(DefaultProjects.getPresetProjects())
  val projects: StateFlow<List<Project>> = _projects.asStateFlow()

  fun getProject(projectId: String): Project? {
    return _projects.value.find { it.id == projectId }
  }

  fun createProject(title: String, description: String = ""): Project {
    val defaultCat = ProgramObject(
      id = "obj_" + UUID.randomUUID().toString().take(8),
      name = "Cat",
      looks = listOf(
        CostumeLook(name = "Happy Cat", emojiOrIcon = "🐱", tintHex = "#FFB300"),
        CostumeLook(name = "Wink Cat", emojiOrIcon = "😸", tintHex = "#FF7043")
      ),
      initialX = 0f,
      initialY = 0f,
      scripts = listOf(
        Script(
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Hello Pocket Code!", paramNum1 = 2f)
          )
        ),
        Script(
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Pop"),
            Brick(op = BrickOp.NEXT_LOOK),
            Brick(op = BrickOp.CHANGE_SIZE_BY, paramNum1 = 10f)
          )
        )
      )
    )

    val newProject = Project(
      title = title.ifBlank { "My Project" },
      description = description,
      objects = listOf(defaultCat),
      variables = mapOf("score" to 0f)
    )
    _projects.value = listOf(newProject) + _projects.value
    return newProject
  }

  fun updateProject(updated: Project) {
    _projects.value = _projects.value.map {
      if (it.id == updated.id) updated else it
    }
  }

  fun duplicateProject(projectId: String) {
    val original = getProject(projectId) ?: return
    val copy = original.copy(
      id = "proj_" + UUID.randomUUID().toString().take(8),
      title = "${original.title} (Copy)",
      createdAt = System.currentTimeMillis(),
      isPreset = false
    )
    _projects.value = listOf(copy) + _projects.value
  }

  fun deleteProject(projectId: String) {
    _projects.value = _projects.value.filterNot { it.id == projectId }
  }

  fun importProject(project: Project) {
    val uniqueCopy = project.copy(
      id = "proj_" + UUID.randomUUID().toString().take(8),
      createdAt = System.currentTimeMillis()
    )
    _projects.value = listOf(uniqueCopy) + _projects.value
  }

  fun addObject(projectId: String, name: String, emoji: String = "⭐"): ProgramObject? {
    val project = getProject(projectId) ?: return null
    val newObj = ProgramObject(
      name = name.ifBlank { "Actor ${project.objects.size + 1}" },
      looks = listOf(CostumeLook(name = "Look 1", emojiOrIcon = emoji)),
      scripts = listOf(
        Script(
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Jump"),
            Brick(op = BrickOp.CHANGE_SIZE_BY, paramNum1 = 10f)
          )
        )
      )
    )
    val updated = project.copy(objects = project.objects + newObj)
    updateProject(updated)
    return newObj
  }

  fun deleteObject(projectId: String, objectId: String) {
    val project = getProject(projectId) ?: return
    val updated = project.copy(objects = project.objects.filterNot { it.id == objectId })
    updateProject(updated)
  }

  fun addScript(projectId: String, objectId: String, headerOp: BrickOp): Script? {
    val project = getProject(projectId) ?: return null
    val obj = project.objects.find { it.id == objectId } ?: return null
    val newScript = Script(
      header = Brick(
        op = headerOp,
        paramString1 = if (headerOp == BrickOp.WHEN_BROADCAST_RECEIVED) "message1" else ""
      ),
      bricks = emptyList()
    )
    val updatedObj = obj.copy(scripts = obj.scripts + newScript)
    val updatedProject = project.copy(
      objects = project.objects.map { if (it.id == objectId) updatedObj else it }
    )
    updateProject(updatedProject)
    return newScript
  }

  fun deleteScript(projectId: String, objectId: String, scriptId: String) {
    val project = getProject(projectId) ?: return
    val obj = project.objects.find { it.id == objectId } ?: return
    val updatedObj = obj.copy(scripts = obj.scripts.filterNot { it.id == scriptId })
    val updatedProject = project.copy(
      objects = project.objects.map { if (it.id == objectId) updatedObj else it }
    )
    updateProject(updatedProject)
  }

  fun addBrickToScript(projectId: String, objectId: String, scriptId: String, brick: Brick) {
    val project = getProject(projectId) ?: return
    val obj = project.objects.find { it.id == objectId } ?: return
    val script = obj.scripts.find { it.id == scriptId } ?: return
    val updatedScript = script.copy(bricks = script.bricks + brick)
    val updatedObj = obj.copy(scripts = obj.scripts.map { if (it.id == scriptId) updatedScript else it })
    updateProject(project.copy(objects = project.objects.map { if (it.id == objectId) updatedObj else it }))
  }

  fun updateBrickInScript(projectId: String, objectId: String, scriptId: String, brick: Brick) {
    val project = getProject(projectId) ?: return
    val obj = project.objects.find { it.id == objectId } ?: return
    val script = obj.scripts.find { it.id == scriptId } ?: return
    val updatedScript = script.copy(
      bricks = script.bricks.map { if (it.id == brick.id) brick else it }
    )
    val updatedObj = obj.copy(scripts = obj.scripts.map { if (it.id == scriptId) updatedScript else it })
    updateProject(project.copy(objects = project.objects.map { if (it.id == objectId) updatedObj else it }))
  }

  fun deleteBrickFromScript(projectId: String, objectId: String, scriptId: String, brickId: String) {
    val project = getProject(projectId) ?: return
    val obj = project.objects.find { it.id == objectId } ?: return
    val script = obj.scripts.find { it.id == scriptId } ?: return
    val updatedScript = script.copy(bricks = script.bricks.filterNot { it.id == brickId })
    val updatedObj = obj.copy(scripts = obj.scripts.map { if (it.id == scriptId) updatedScript else it })
    updateProject(project.copy(objects = project.objects.map { if (it.id == objectId) updatedObj else it }))
  }

  fun addLookToObject(projectId: String, objectId: String, look: CostumeLook) {
    val project = getProject(projectId) ?: return
    val obj = project.objects.find { it.id == objectId } ?: return
    val updatedObj = obj.copy(looks = obj.looks + look)
    updateProject(project.copy(objects = project.objects.map { if (it.id == objectId) updatedObj else it }))
  }

  fun deleteLookFromObject(projectId: String, objectId: String, lookId: String) {
    val project = getProject(projectId) ?: return
    val obj = project.objects.find { it.id == objectId } ?: return
    if (obj.looks.size <= 1) return // Keep at least one look
    val updatedObj = obj.copy(looks = obj.looks.filterNot { it.id == lookId })
    updateProject(project.copy(objects = project.objects.map { if (it.id == objectId) updatedObj else it }))
  }
}
