package com.example

import com.example.engine.RuntimeEngine
import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.DefaultProjects
import com.example.model.ProgramObject
import com.example.model.Project
import com.example.model.Script
import com.example.repository.ProjectRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PocketCodeEngineTest {

  @Test
  fun testDefaultProjectsLoaded() {
    val repo = ProjectRepository()
    val projects = repo.projects.value
    assertTrue("Should have default preset projects", projects.isNotEmpty())
    assertNotNull(repo.getProject("preset_galaxy_dodger"))
    assertNotNull(repo.getProject("preset_bouncing_cat"))
    assertNotNull(repo.getProject("preset_neon_art"))
    assertNotNull(repo.getProject("preset_coin_clicker"))
  }

  @Test
  fun testProjectCreationAndModification() {
    val repo = ProjectRepository()
    val newProject = repo.createProject("My Game", "Test game description")
    assertEquals("My Game", newProject.title)
    assertTrue("Project should have at least 1 object", newProject.objects.isNotEmpty())

    // Add another actor
    val newActor = repo.addObject(newProject.id, "Alien", "👾")
    assertNotNull(newActor)

    // Add script
    val script = repo.addScript(newProject.id, newActor!!.id, BrickOp.WHEN_TAPPED)
    assertNotNull(script)

    // Add brick
    repo.addBrickToScript(
      newProject.id,
      newActor.id,
      script!!.id,
      Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "score", paramNum1 = 5f)
    )

    val updatedProject = repo.getProject(newProject.id)
    val updatedActor = updatedProject?.objects?.find { it.id == newActor.id }
    val updatedScript = updatedActor?.scripts?.find { it.id == script.id }
    assertEquals(1, updatedScript?.bricks?.size)
  }

  @Test
  fun testRuntimeEngineExecution() = runBlocking {
    val testObj = ProgramObject(
      id = "test_cat",
      name = "Cat",
      initialX = 10f,
      initialY = 20f,
      scripts = listOf(
        Script(
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.SET_VARIABLE, paramString1 = "score", paramNum1 = 100f),
            Brick(op = BrickOp.PLACE_AT_XY, paramNum1 = 50f, paramNum2 = 60f)
          )
        )
      )
    )

    val testProject = Project(
      id = "test_p",
      title = "Test Run",
      description = "Test description",
      objects = listOf(testObj),
      variables = mapOf("score" to 0f)
    )

    val engine = RuntimeEngine(testProject)
    assertEquals(10f, engine.objectStates["test_cat"]?.x)
    assertEquals(20f, engine.objectStates["test_cat"]?.y)
    assertEquals(0f, engine.variables["score"])

    engine.start()
    // Small delay to allow start script to run
    kotlinx.coroutines.delay(100)
    assertEquals(100f, engine.variables["score"])
    assertEquals(50f, engine.objectStates["test_cat"]?.x)
    assertEquals(60f, engine.objectStates["test_cat"]?.y)

    engine.stop()
  }

  @Test
  fun testCatrobatAndPcpExportImport() {
    val repo = ProjectRepository()
    val sample = repo.projects.value.first()
    
    // Export to PCP and parse back
    val pcpBytes = com.example.repository.CatrobatFileManager.exportToPcpZip(sample)
    assertTrue("PCP export should produce non-empty zip bytes", pcpBytes.isNotEmpty())

    val importedProject = com.example.repository.CatrobatFileManager.importZip(pcpBytes)
    assertNotNull("Imported project should not be null", importedProject)
    assertEquals(sample.title, importedProject?.title)
    assertEquals(sample.objects.size, importedProject?.objects?.size)

    // Export to Catrobat XML archive
    val catrobatBytes = com.example.repository.CatrobatFileManager.exportToCatrobatZip(sample)
    assertTrue("Catrobat archive should produce non-empty zip bytes", catrobatBytes.isNotEmpty())
  }

  @Test
  fun testSyntaxAnalyzer() {
    val repo = ProjectRepository()
    val project = repo.projects.value.first()
    val diagnostics = com.example.engine.SyntaxAnalyzer.analyze(project)
    
    assertTrue("Health score should be between 0 and 100", diagnostics.healthScore in 0..100)
    assertTrue("Total blocks count should be positive", diagnostics.totalBricks >= 1)
  }

  @Test
  fun testMqttConfigAndRepository() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    val repo = com.example.repository.MqttConfigRepository(context)

    // Check defaults
    val initial = repo.config.value
    assertEquals("broker.emqx.io", initial.host)
    assertEquals(1883, initial.port)
    assertEquals(false, initial.hasAuth)
    assertEquals("tcp://broker.emqx.io:1883", initial.uriString)

    // Save custom configuration
    val custom = com.example.model.MqttConfig(
      host = "test.mosquitto.org",
      port = 8883,
      username = "developer",
      password = "secret_password",
      useSsl = true,
      defaultTopic = "pocketcode/sensors/temp"
    )
    repo.saveConfig(custom)

    val loaded = repo.config.value
    assertEquals("test.mosquitto.org", loaded.host)
    assertEquals(8883, loaded.port)
    assertEquals("developer", loaded.username)
    assertEquals("secret_password", loaded.password)
    assertTrue("Should indicate auth is present", loaded.hasAuth)
    assertTrue("Should be SSL", loaded.useSsl)
    assertEquals("ssl://test.mosquitto.org:8883", loaded.uriString)

    // Reset to defaults
    val reset = repo.resetToDefaults()
    assertEquals("broker.emqx.io", reset.host)
    assertEquals(1883, reset.port)
  }

  @Test
  fun testRuntimeEngineWithCustomMqttConfig() = runBlocking {
    val testObj = ProgramObject(
      id = "network_actor",
      name = "NetworkActor",
      scripts = listOf(
        Script(
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.MQTT_CONNECT),
            Brick(op = BrickOp.MQTT_PUBLISH, paramString1 = "game/scores", paramString2 = "100")
          )
        )
      )
    )

    val testProject = Project(
      id = "mqtt_proj",
      title = "MQTT Test Project",
      description = "MQTT Project for testing",
      objects = listOf(testObj)
    )

    val engine = RuntimeEngine(testProject)
    val customConfig = com.example.model.MqttConfig(
      host = "broker.hivemq.com",
      port = 1883,
      username = "player1",
      password = "pwd"
    )
    engine.activeMqttConfig = customConfig

    engine.start()
    kotlinx.coroutines.delay(100)

    assertTrue("MQTT connected state should be true", engine.mqttConnected)
    assertTrue("MQTT status should mention broker.hivemq.com", engine.mqttBrokerStatus.contains("broker.hivemq.com"))
    assertTrue("Logs should record MQTT publish", engine.runtimeLogs.any { it.source == com.example.engine.LogSource.MQTT })

    engine.stop()
  }
}
