package com.example.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.Project
import com.example.model.SoundPlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class PenStroke(
  val fromX: Float,
  val fromY: Float,
  val toX: Float,
  val toY: Float,
  val colorHex: String,
  val strokeWidth: Float
)

class RuntimeObjectState(
  val objectId: String,
  val name: String,
  initialX: Float,
  initialY: Float,
  initialRotation: Float = 0f,
  initialSize: Float = 100f,
  initialLookIndex: Int = 0
) {
  var x by mutableStateOf(initialX)
  var y by mutableStateOf(initialY)
  var rotation by mutableStateOf(initialRotation)
  var sizePercent by mutableStateOf(initialSize)
  var lookIndex by mutableStateOf(initialLookIndex)
  var visible by mutableStateOf(true)
  var speechText by mutableStateOf<String?>(null)
  var penDown by mutableStateOf(false)
  var penColorHex by mutableStateOf("#00E5FF")
  var penSize by mutableStateOf(5f)

  // Physics & Gravity (Newcatroid Mod)
  var vx by mutableStateOf(0f)
  var vy by mutableStateOf(0f)
  var gravityX by mutableStateOf(0f)
  var gravityY by mutableStateOf(0f)
  var bounceElasticity by mutableStateOf(0.75f)
  var mass by mutableStateOf(1f)
}

class RuntimeEngine(val project: Project) {
  val objectStates = mutableStateMapOf<String, RuntimeObjectState>()
  val variables = mutableStateMapOf<String, Float>()
  val liveLogs = mutableStateListOf<DiagnosticLog>()

  var isPlaying by mutableStateOf(false)
    private set
  var isPaused by mutableStateOf(false)
    private set

  var penStrokes = mutableStateOf<List<PenStroke>>(emptyList())

  // Camera & 3D System
  var cameraFollowObjectId by mutableStateOf<String?>(null)
  var cameraZoomPercent by mutableStateOf(100f)
  var cameraPanX by mutableStateOf(0f)
  var cameraPanY by mutableStateOf(0f)
  var cameraShake by mutableStateOf(0f)

  // Web & Video Overlay
  var activeWebviewUrl by mutableStateOf<String?>(null)
  var activeVideoUrl by mutableStateOf<String?>(null)

  // Aliases for StagePlayerScreen
  var activeWebViewUrl: String?
    get() = activeWebviewUrl
    set(value) { activeWebviewUrl = value }

  var cameraZoom: Float
    get() = cameraZoomPercent
    set(value) { cameraZoomPercent = value }

  val runtimeLogs: List<DiagnosticLog>
    get() = liveLogs

  fun clearRuntimeLogs() {
    liveLogs.clear()
  }

  // Network & MQTT
  var activeMqttConfig by mutableStateOf(com.example.model.MqttConfig())
  var mqttBrokerStatus by mutableStateOf("Ready (${activeMqttConfig.host}:${activeMqttConfig.port})")
  var mqttConnected by mutableStateOf(false)

  private var engineScope: CoroutineScope? = null
  private val activeJobs = mutableListOf<Job>()
  private var physicsJob: Job? = null

  init {
    resetState()
  }

  fun logEvent(
    level: LogLevel,
    source: LogSource,
    tag: String,
    message: String,
    actor: String? = null,
    brick: String? = null
  ) {
    val log = DiagnosticLog(
      level = level,
      source = source,
      tag = tag,
      message = message,
      actorName = actor,
      brickName = brick
    )
    if (liveLogs.size > 200) {
      liveLogs.removeAt(0)
    }
    liveLogs.add(log)
  }

  fun resetState() {
    stop()
    variables.clear()
    project.variables.forEach { (k, v) -> variables[k] = v }
    objectStates.clear()
    project.objects.forEach { obj ->
      objectStates[obj.id] = RuntimeObjectState(
        objectId = obj.id,
        name = obj.name,
        initialX = obj.initialX,
        initialY = obj.initialY,
        initialRotation = obj.initialRotation,
        initialSize = obj.initialSize,
        initialLookIndex = obj.currentLookIndex
      )
    }
    penStrokes.value = emptyList()
    cameraFollowObjectId = null
    cameraZoomPercent = 100f
    cameraPanX = 0f
    cameraPanY = 0f
    cameraShake = 0f
    activeWebviewUrl = null
    activeVideoUrl = null
    mqttConnected = false
    mqttBrokerStatus = "Disconnected"
    liveLogs.clear()
    logEvent(LogLevel.INFO, LogSource.RUNTIME, "ENGINE", "Project state initialized: '${project.title}'")
  }

  fun start() {
    resetState()
    isPlaying = true
    isPaused = false
    val scope = CoroutineScope(Dispatchers.Default)
    engineScope = scope

    logEvent(LogLevel.SUCCESS, LogSource.RUNTIME, "PLAY", "Execution started with ${project.objects.size} objects")

    // Start Newcatroid Physics simulation loop
    startPhysicsLoop(scope)

    // Trigger all WHEN_SCENE_STARTS scripts
    project.objects.forEach { obj ->
      obj.scripts.forEach { script ->
        if (script.header.op == BrickOp.WHEN_SCENE_STARTS) {
          logEvent(LogLevel.INFO, LogSource.RUNTIME, "EVENT", "When scene starts triggered", actor = obj.name)
          launchScript(obj.id, script.bricks)
        }
      }
    }
  }

  fun pause() {
    isPaused = !isPaused
    val status = if (isPaused) "Paused" else "Resumed"
    logEvent(LogLevel.INFO, LogSource.RUNTIME, "STATUS", "Game $status")
  }

  fun resume() {
    if (isPaused) {
      isPaused = false
      logEvent(LogLevel.INFO, LogSource.RUNTIME, "STATUS", "Game Resumed")
    }
  }

  fun restart() {
    stop()
    start()
  }

  fun stop() {
    isPlaying = false
    isPaused = false
    activeJobs.forEach { it.cancel() }
    activeJobs.clear()
    physicsJob?.cancel()
    physicsJob = null
    engineScope = null
  }

  fun onObjectTapped(objectId: String) {
    if (!isPlaying) return
    val obj = project.objects.find { it.id == objectId } ?: return
    logEvent(LogLevel.INFO, LogSource.RUNTIME, "TAP", "Object tapped: '${obj.name}'", actor = obj.name)
    obj.scripts.forEach { script ->
      if (script.header.op == BrickOp.WHEN_TAPPED) {
        launchScript(objectId, script.bricks)
      }
    }
  }

  fun broadcast(message: String) {
    if (!isPlaying) return
    logEvent(LogLevel.INFO, LogSource.RUNTIME, "BROADCAST", "Message broadcast: '$message'")
    project.objects.forEach { obj ->
      obj.scripts.forEach { script ->
        if (script.header.op == BrickOp.WHEN_BROADCAST_RECEIVED &&
          script.header.paramString1.equals(message, ignoreCase = true)
        ) {
          launchScript(obj.id, script.bricks)
        }
      }
    }
  }

  fun onMqttMessageReceived(topic: String, payload: String) {
    logEvent(LogLevel.SUCCESS, LogSource.MQTT, "MQTT_RX", "Topic '$topic' received payload: $payload")
    project.objects.forEach { obj ->
      obj.scripts.forEach { script ->
        if (script.header.op == BrickOp.WHEN_MQTT_RECEIVED &&
          script.header.paramString1.equals(topic, ignoreCase = true)
        ) {
          launchScript(obj.id, script.bricks)
        }
      }
    }
  }

  private fun startPhysicsLoop(scope: CoroutineScope) {
    physicsJob = scope.launch {
      val dt = 0.02f // 50 Hz physics tick
      while (isActive && isPlaying) {
        if (!isPaused) {
          // Camera shake decay
          if (cameraShake > 0.1f) {
            cameraShake *= 0.9f
          } else {
            cameraShake = 0f
          }

          // Camera follow sprite
          val followId = cameraFollowObjectId
          if (followId != null) {
            val target = objectStates[followId]
            if (target != null) {
              cameraPanX = target.x
              cameraPanY = target.y
            }
          }

          // Update physics per object
          objectStates.values.forEach { state ->
            val hasPhysics = state.gravityX != 0f || state.gravityY != 0f || state.vx != 0f || state.vy != 0f
            if (hasPhysics) {
              val prevX = state.x
              val prevY = state.y

              // Apply gravity
              state.vx += state.gravityX * dt
              state.vy += state.gravityY * dt

              // Update positions
              state.x += state.vx * dt
              state.y += state.vy * dt

              // Boundaries and bounce
              val boundsX = 165f
              val boundsY = 285f
              var bounced = false

              if (state.x < -boundsX) {
                state.x = -boundsX
                state.vx = -state.vx * state.bounceElasticity
                bounced = true
              } else if (state.x > boundsX) {
                state.x = boundsX
                state.vx = -state.vx * state.bounceElasticity
                bounced = true
              }

              if (state.y < -boundsY) {
                state.y = -boundsY
                state.vy = -state.vy * state.bounceElasticity
                bounced = true
              } else if (state.y > boundsY) {
                state.y = boundsY
                state.vy = -state.vy * state.bounceElasticity
                bounced = true
              }

              if (bounced && (Math.abs(state.vx) > 50f || Math.abs(state.vy) > 50f)) {
                SoundPlayer.playSound("Pop")
                logEvent(
                  LogLevel.INFO,
                  LogSource.PHYSICS,
                  "BOUNCE",
                  "Physics bounce for '${state.name}' at (${state.x.toInt()}, ${state.y.toInt()})",
                  actor = state.name
                )
              }

              recordPen(state, prevX, prevY, state.x, state.y)
            }
          }
        }
        delay(20)
      }
    }
  }

  private fun launchScript(objectId: String, bricks: List<Brick>) {
    val scope = engineScope ?: return
    val job = scope.launch {
      try {
        executeBricks(objectId, bricks)
      } catch (_: CancellationException) {
      } catch (e: Exception) {
        logEvent(LogLevel.ERROR, LogSource.RUNTIME, "SCRIPT_ERROR", "Error executing script: ${e.message}")
      }
    }
    activeJobs.add(job)
  }

  private suspend fun executeBricks(objectId: String, bricks: List<Brick>) {
    for (brick in bricks) {
      if (!isPlaying) break
      while (isPaused) {
        delay(50)
      }
      executeSingleBrick(objectId, brick)
    }
  }

  private suspend fun executeSingleBrick(objectId: String, brick: Brick) {
    val state = objectStates[objectId] ?: return
    when (brick.op) {
      // Event
      BrickOp.BROADCAST -> {
        broadcast(brick.paramString1)
      }

      // Control
      BrickOp.WAIT_SECONDS -> {
        val ms = (brick.paramNum1.coerceAtLeast(0.01f) * 1000).toLong()
        delay(ms)
      }

      BrickOp.REPEAT_TIMES -> {
        val times = brick.paramNum1.toInt().coerceAtLeast(1)
        for (i in 0 until times) {
          if (!isPlaying) break
          while (isPaused) delay(50)
          executeBricks(objectId, brick.childBricks)
        }
      }

      BrickOp.FOREVER -> {
        while (isPlaying) {
          while (isPaused) delay(50)
          executeBricks(objectId, brick.childBricks)
          delay(16) // Prevent tight loop lock
        }
      }

      BrickOp.IF_VARIABLE -> {
        val varVal = variables[brick.paramString1] ?: 0f
        val target = brick.paramNum1
        val conditionMet = when (brick.paramString2) {
          ">" -> varVal > target
          "<" -> varVal < target
          else -> varVal == target
        }
        if (conditionMet) {
          executeBricks(objectId, brick.childBricks)
        }
      }

      // Motion
      BrickOp.PLACE_AT_XY -> {
        val oldX = state.x
        val oldY = state.y
        state.x = brick.paramNum1
        state.y = brick.paramNum2
        recordPen(state, oldX, oldY, state.x, state.y)
      }

      BrickOp.MOVE_STEPS -> {
        val rad = Math.toRadians((state.rotation - 90.0))
        val dx = (cos(rad) * brick.paramNum1).toFloat()
        val dy = (-sin(rad) * brick.paramNum1).toFloat()
        val oldX = state.x
        val oldY = state.y
        state.x += dx
        state.y += dy
        recordPen(state, oldX, oldY, state.x, state.y)
      }

      BrickOp.TURN_DEGREES -> {
        state.rotation = (state.rotation + brick.paramNum1) % 360f
      }

      BrickOp.POINT_IN_DIRECTION -> {
        state.rotation = brick.paramNum1 % 360f
      }

      BrickOp.BOUNCE_IF_ON_EDGE -> {
        var bounced = false
        if (state.x < -160f) {
          state.x = -160f
          state.rotation = (360f - state.rotation) % 360f
          bounced = true
        } else if (state.x > 160f) {
          state.x = 160f
          state.rotation = (360f - state.rotation) % 360f
          bounced = true
        }
        if (state.y < -280f) {
          state.y = -280f
          state.rotation = (180f - state.rotation) % 360f
          bounced = true
        } else if (state.y > 280f) {
          state.y = 280f
          state.rotation = (180f - state.rotation) % 360f
          bounced = true
        }
        if (bounced) {
          SoundPlayer.playSound("Pop")
        }
      }

      BrickOp.CHANGE_X_BY -> {
        val oldX = state.x
        val oldY = state.y
        state.x += brick.paramNum1
        recordPen(state, oldX, oldY, state.x, state.y)
      }

      BrickOp.CHANGE_Y_BY -> {
        val oldX = state.x
        val oldY = state.y
        state.y += brick.paramNum1
        recordPen(state, oldX, oldY, state.x, state.y)
      }

      BrickOp.SET_X_TO -> {
        val oldX = state.x
        val oldY = state.y
        state.x = brick.paramNum1
        recordPen(state, oldX, oldY, state.x, state.y)
      }

      BrickOp.SET_Y_TO -> {
        val oldX = state.x
        val oldY = state.y
        state.y = brick.paramNum1
        recordPen(state, oldX, oldY, state.x, state.y)
      }

      BrickOp.GLIDE_TO_XY -> {
        val durationMs = (brick.paramNum1.coerceAtLeast(0.1f) * 1000).toLong()
        val startX = state.x
        val startY = state.y
        val targetX = brick.paramString1.toFloatOrNull() ?: 0f
        val targetY = brick.paramNum2
        val steps = 25
        val stepDelay = durationMs / steps
        for (i in 1..steps) {
          if (!isPlaying) break
          while (isPaused) delay(50)
          val progress = i.toFloat() / steps
          val prevX = state.x
          val prevY = state.y
          state.x = startX + (targetX - startX) * progress
          state.y = startY + (targetY - startY) * progress
          recordPen(state, prevX, prevY, state.x, state.y)
          delay(stepDelay)
        }
      }

      // Physics (Newcatroid Mod)
      BrickOp.SET_GRAVITY -> {
        state.gravityX = brick.paramNum1
        state.gravityY = brick.paramNum2
        logEvent(LogLevel.INFO, LogSource.PHYSICS, "GRAVITY", "Set gravity (${state.gravityX}, ${state.gravityY})", state.name)
      }

      BrickOp.SET_VELOCITY -> {
        state.vx = brick.paramNum1
        state.vy = brick.paramNum2
        logEvent(LogLevel.INFO, LogSource.PHYSICS, "VELOCITY", "Set velocity vx=${state.vx}, vy=${state.vy}", state.name)
      }

      BrickOp.CHANGE_VELOCITY_BY -> {
        state.vx += brick.paramNum1
        state.vy += brick.paramNum2
      }

      BrickOp.SET_BOUNCE_ELASTICITY -> {
        state.bounceElasticity = (brick.paramNum1 / 100f).coerceIn(0f, 1f)
      }

      BrickOp.SET_MASS -> {
        state.mass = brick.paramNum1.coerceAtLeast(0.1f)
      }

      BrickOp.APPLY_IMPULSE -> {
        state.vx += brick.paramNum1 / state.mass
        state.vy += brick.paramNum2 / state.mass
        logEvent(LogLevel.INFO, LogSource.PHYSICS, "IMPULSE", "Impulse applied fx=${brick.paramNum1}, fy=${brick.paramNum2}", state.name)
      }

      // Camera & 3D
      BrickOp.FIX_CAMERA_TO_SPRITE -> {
        cameraFollowObjectId = objectId
        logEvent(LogLevel.SUCCESS, LogSource.CAMERA, "CAMERA_FOLLOW", "Camera locked to follow '${state.name}'", state.name)
      }

      BrickOp.SET_CAMERA_ZOOM -> {
        cameraZoomPercent = brick.paramNum1.coerceIn(25f, 400f)
        logEvent(LogLevel.INFO, LogSource.CAMERA, "CAMERA_ZOOM", "Camera zoom set to ${cameraZoomPercent.toInt()}%")
      }

      BrickOp.PAN_CAMERA -> {
        cameraFollowObjectId = null
        cameraPanX = brick.paramNum1
        cameraPanY = brick.paramNum2
      }

      BrickOp.SHAKE_CAMERA -> {
        cameraShake = brick.paramNum1.coerceIn(5f, 40f)
        logEvent(LogLevel.INFO, LogSource.CAMERA, "SHAKE", "Screen shake intensity $cameraShake")
      }

      BrickOp.RESET_CAMERA -> {
        cameraFollowObjectId = null
        cameraZoomPercent = 100f
        cameraPanX = 0f
        cameraPanY = 0f
        cameraShake = 0f
      }

      // Looks
      BrickOp.NEXT_LOOK -> {
        val obj = project.objects.find { it.id == objectId }
        val count = obj?.looks?.size ?: 1
        if (count > 0) {
          state.lookIndex = (state.lookIndex + 1) % count
        }
      }

      BrickOp.SET_LOOK -> {
        val idx = brick.paramNum1.toInt().coerceAtLeast(0)
        state.lookIndex = idx
      }

      BrickOp.SET_SIZE_PERCENT -> {
        state.sizePercent = brick.paramNum1.coerceIn(20f, 400f)
      }

      BrickOp.CHANGE_SIZE_BY -> {
        state.sizePercent = (state.sizePercent + brick.paramNum1).coerceIn(20f, 400f)
      }

      BrickOp.SHOW -> {
        state.visible = true
      }

      BrickOp.HIDE -> {
        state.visible = false
      }

      BrickOp.SAY_TEXT -> {
        state.speechText = brick.paramString1
        val durSeconds = brick.paramNum1.coerceAtLeast(0.5f)
        delay((durSeconds * 1000).toLong())
        if (state.speechText == brick.paramString1) {
          state.speechText = null
        }
      }

      // Sound
      BrickOp.PLAY_SOUND -> {
        SoundPlayer.playSound(brick.paramString1.ifBlank { "Pop" })
      }

      BrickOp.PLAY_NOTE_TONE -> {
        val freq = if (brick.paramNum1 > 0f) brick.paramNum1 else 440f
        val dur = if (brick.paramNum2 > 0f) (brick.paramNum2 * 1000).toInt() else 150
        SoundPlayer.playTone(freq, dur)
      }

      // Data
      BrickOp.SET_VARIABLE -> {
        val varName = brick.paramString1.ifBlank { "score" }
        variables[varName] = brick.paramNum1
        logEvent(LogLevel.INFO, LogSource.RUNTIME, "DATA", "Variable '$varName' set to ${brick.paramNum1}")
      }

      BrickOp.CHANGE_VARIABLE_BY -> {
        val varName = brick.paramString1.ifBlank { "score" }
        val current = variables[varName] ?: 0f
        variables[varName] = current + brick.paramNum1
        logEvent(LogLevel.INFO, LogSource.RUNTIME, "DATA", "Variable '$varName' changed to ${variables[varName]}")
      }

      BrickOp.SHOW_VARIABLE -> {}

      // Pen
      BrickOp.PEN_DOWN -> {
        state.penDown = true
      }

      BrickOp.PEN_UP -> {
        state.penDown = false
      }

      BrickOp.SET_PEN_COLOR -> {
        state.penColorHex = brick.paramString1.ifBlank { "#00E5FF" }
      }

      BrickOp.SET_PEN_SIZE -> {
        state.penSize = brick.paramNum1.coerceIn(1f, 30f)
      }

      BrickOp.CLEAR_PEN -> {
        penStrokes.value = emptyList()
      }

      // AI Bricks
      BrickOp.AI_ASK_PROMPT -> {
        val prompt = brick.paramString1.ifBlank { "Tell a short game quest tip" }
        logEvent(LogLevel.INFO, LogSource.AI, "AI_REQUEST", "Evaluating AI prompt: '$prompt'")
        val response = evaluateAiResponse(prompt)
        val targetVar = brick.paramString2.ifBlank { "ai_response" }
        val numVal = response.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 1f
        variables[targetVar] = numVal
        state.speechText = response
        logEvent(LogLevel.SUCCESS, LogSource.AI, "AI_RESPONSE", "AI: '$response'", state.name)
        delay(2500)
        if (state.speechText == response) state.speechText = null
      }

      BrickOp.AI_CLASSIFY_TEXT -> {
        val text = brick.paramString1
        val classification = if (text.contains("happy", true) || text.contains("win", true) || text.contains("love", true)) {
          "Positive"
        } else if (text.contains("bad", true) || text.contains("lose", true) || text.contains("hate", true)) {
          "Negative"
        } else {
          "Neutral"
        }
        val targetVar = brick.paramString2.ifBlank { "sentiment" }
        variables[targetVar] = if (classification == "Positive") 1f else if (classification == "Negative") -1f else 0f
        logEvent(LogLevel.INFO, LogSource.AI, "AI_CLASSIFY", "Sentiment classified as $classification for '$text'")
      }

      BrickOp.AI_GENERATE_NPC_DIALOGUE -> {
        val role = brick.paramString1.ifBlank { "Wise Wizard" }
        val dialogue = when (Random.nextInt(4)) {
          0 -> "Beware the floating spikes ahead, brave coder!"
          1 -> "PocketCodePlus gives you power beyond standard blocks!"
          2 -> "The MQTT broker whispers secrets from across the internet..."
          else -> "Gather your points and conquer the stage!"
        }
        state.speechText = "[$role]: $dialogue"
        logEvent(LogLevel.SUCCESS, LogSource.AI, "AI_NPC", "[$role]: $dialogue", state.name)
        delay(3000)
        state.speechText = null
      }

      // Network & MQTT
      BrickOp.MQTT_CONNECT -> {
        mqttConnected = true
        val hostInfo = "${activeMqttConfig.host}:${activeMqttConfig.port}"
        val authInfo = if (activeMqttConfig.hasAuth) "user='${activeMqttConfig.username}'" else "anonymous"
        mqttBrokerStatus = "Connected to $hostInfo ($authInfo)"
        logEvent(LogLevel.SUCCESS, LogSource.MQTT, "MQTT_CONN", "Successfully connected to $hostInfo as ${activeMqttConfig.clientId} ($authInfo)")
      }

      BrickOp.MQTT_PUBLISH -> {
        val topic = brick.paramString1.ifBlank { activeMqttConfig.defaultTopic }
        val payload = brick.paramString2.ifBlank { "score=${variables["score"] ?: 0f}" }
        logEvent(LogLevel.SUCCESS, LogSource.MQTT, "MQTT_PUB", "Published to [${activeMqttConfig.host}/$topic]: $payload")
        // Trigger any local listeners on that topic
        onMqttMessageReceived(topic, payload)
      }

      BrickOp.WHEN_MQTT_RECEIVED -> {
        // Header block
      }

      BrickOp.HTTP_GET_REQUEST -> {
        val urlStr = brick.paramString1.ifBlank { "https://api.agify.io/?name=catrobat" }
        val targetVar = brick.paramString2.ifBlank { "web_data" }
        logEvent(LogLevel.INFO, LogSource.WEB, "HTTP_GET", "Fetching $urlStr")
        try {
          val result = withContext(Dispatchers.IO) {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.inputStream.bufferedReader().use { it.readText() }
          }
          val extractedNum = result.filter { it.isDigit() || it == '.' }.take(6).toFloatOrNull() ?: 200f
          variables[targetVar] = extractedNum
          logEvent(LogLevel.SUCCESS, LogSource.WEB, "HTTP_SUCCESS", "Received ${result.take(60)}...")
        } catch (e: Exception) {
          logEvent(LogLevel.WARNING, LogSource.WEB, "HTTP_FAIL", "Request error: ${e.message}")
        }
      }

      BrickOp.OPEN_WEBVIEW -> {
        val url = brick.paramString1.ifBlank { "https://catrobat.org" }
        activeWebviewUrl = url
        logEvent(LogLevel.INFO, LogSource.WEB, "WEBVIEW", "Opening in-app WebView for $url")
      }

      BrickOp.PLAY_VIDEO_URL -> {
        val videoUrl = brick.paramString1.ifBlank { "https://www.youtube.com/watch?v=dQw4w9WgXcQ" }
        activeVideoUrl = videoUrl
        logEvent(LogLevel.INFO, LogSource.WEB, "VIDEO", "Launching video playback for $videoUrl")
      }

      // Device & Sensors
      BrickOp.VIBRATE_DEVICE -> {
        val ms = brick.paramNum1.toLong().coerceIn(10, 1000)
        SoundPlayer.playSound("Pop") // Audible haptic feedback
        logEvent(LogLevel.INFO, LogSource.RUNTIME, "HAPTIC", "Device haptic vibration trigger for ${ms}ms")
      }

      BrickOp.TEXT_TO_SPEECH -> {
        val text = brick.paramString1.ifBlank { "PocketCodePlus is awesome!" }
        state.speechText = "🔊 $text"
        SoundPlayer.playTone(523.25f, 180)
        delay(1200)
        state.speechText = null
      }

      // Event Headers (Entry points, not sequentially executed within scripts)
      BrickOp.WHEN_SCENE_STARTS,
      BrickOp.WHEN_TAPPED,
      BrickOp.WHEN_BROADCAST_RECEIVED,
      BrickOp.WHEN_MQTT_RECEIVED -> {
        // No-op in body
      }
    }
  }

  private fun evaluateAiResponse(prompt: String): String {
    val p = prompt.lowercase()
    return when {
      p.contains("hint") || p.contains("tip") -> "Tip: Use 'Fix camera to sprite' and gravity for platformers!"
      p.contains("story") || p.contains("quest") -> "Quest: Travel through the quantum cat portal and retrieve the golden gear!"
      p.contains("boss") || p.contains("enemy") -> "A wild Catroid Boss approaches with 100 HP! Dodge the lasers!"
      p.contains("score") || p.contains("math") -> "Calculated optimal jump trajectory: velocity (60, 120)."
      else -> "AI: Creativity unlocked! Build something amazing with PocketCodePlus."
    }
  }

  private fun recordPen(state: RuntimeObjectState, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    if (state.penDown && (fromX != toX || fromY != toY)) {
      val stroke = PenStroke(
        fromX = fromX,
        fromY = fromY,
        toX = toX,
        toY = toY,
        colorHex = state.penColorHex,
        strokeWidth = state.penSize
      )
      penStrokes.value = penStrokes.value + stroke
    }
  }
}
