package com.example.model

import java.util.UUID

enum class BrickCategory(val displayName: String, val colorHex: Long) {
  EVENT("Event", 0xFFF59E0B),
  CONTROL("Control", 0xFFEA580C),
  MOTION("Motion", 0xFF2563EB),
  PHYSICS("Physics", 0xFF0284C7),
  CAMERA("Camera", 0xFF06B6D4),
  LOOKS("Looks", 0xFF9333EA),
  SOUND("Sound", 0xFFDB2777),
  DATA("Data", 0xFFDC2626),
  PEN("Pen", 0xFF16A34A),
  AI("Artificial Intelligence", 0xFF8B5CF6),
  NETWORK("MQTT & Web", 0xFF0D9488),
  DEVICE("Device & Sensors", 0xFF64748B)
}

enum class BrickOp(val category: BrickCategory, val defaultLabel: String) {
  // Event
  WHEN_SCENE_STARTS(BrickCategory.EVENT, "When scene starts"),
  WHEN_TAPPED(BrickCategory.EVENT, "When tapped"),
  WHEN_BROADCAST_RECEIVED(BrickCategory.EVENT, "When I receive message"),
  BROADCAST(BrickCategory.EVENT, "Broadcast message"),

  // Control
  WAIT_SECONDS(BrickCategory.CONTROL, "Wait seconds"),
  REPEAT_TIMES(BrickCategory.CONTROL, "Repeat times"),
  FOREVER(BrickCategory.CONTROL, "Forever"),
  IF_VARIABLE(BrickCategory.CONTROL, "If variable"),

  // Motion
  PLACE_AT_XY(BrickCategory.MOTION, "Place at X, Y"),
  MOVE_STEPS(BrickCategory.MOTION, "Move steps"),
  TURN_DEGREES(BrickCategory.MOTION, "Turn degrees"),
  POINT_IN_DIRECTION(BrickCategory.MOTION, "Point in direction"),
  BOUNCE_IF_ON_EDGE(BrickCategory.MOTION, "Bounce if on edge"),
  CHANGE_X_BY(BrickCategory.MOTION, "Change X by"),
  CHANGE_Y_BY(BrickCategory.MOTION, "Change Y by"),
  SET_X_TO(BrickCategory.MOTION, "Set X to"),
  SET_Y_TO(BrickCategory.MOTION, "Set Y to"),
  GLIDE_TO_XY(BrickCategory.MOTION, "Glide seconds to X, Y"),

  // Physics (Newcatroid)
  SET_GRAVITY(BrickCategory.PHYSICS, "Set gravity X, Y"),
  SET_VELOCITY(BrickCategory.PHYSICS, "Set velocity X, Y"),
  CHANGE_VELOCITY_BY(BrickCategory.PHYSICS, "Change velocity by dX, dY"),
  SET_BOUNCE_ELASTICITY(BrickCategory.PHYSICS, "Set bounce elasticity %"),
  SET_MASS(BrickCategory.PHYSICS, "Set mass kg"),
  APPLY_IMPULSE(BrickCategory.PHYSICS, "Apply impulse force X, Y"),

  // Camera & 3D
  FIX_CAMERA_TO_SPRITE(BrickCategory.CAMERA, "Fix camera to this sprite"),
  SET_CAMERA_ZOOM(BrickCategory.CAMERA, "Set camera zoom %"),
  PAN_CAMERA(BrickCategory.CAMERA, "Pan camera X, Y"),
  SHAKE_CAMERA(BrickCategory.CAMERA, "Shake camera"),
  RESET_CAMERA(BrickCategory.CAMERA, "Reset camera"),

  // Looks
  NEXT_LOOK(BrickCategory.LOOKS, "Next look"),
  SET_LOOK(BrickCategory.LOOKS, "Switch to look #"),
  SET_SIZE_PERCENT(BrickCategory.LOOKS, "Set size to %"),
  CHANGE_SIZE_BY(BrickCategory.LOOKS, "Change size by"),
  SHOW(BrickCategory.LOOKS, "Show"),
  HIDE(BrickCategory.LOOKS, "Hide"),
  SAY_TEXT(BrickCategory.LOOKS, "Say message"),

  // Sound
  PLAY_SOUND(BrickCategory.SOUND, "Play sound"),
  PLAY_NOTE_TONE(BrickCategory.SOUND, "Play note tone Hz"),

  // Data
  SET_VARIABLE(BrickCategory.DATA, "Set variable to"),
  CHANGE_VARIABLE_BY(BrickCategory.DATA, "Change variable by"),
  SHOW_VARIABLE(BrickCategory.DATA, "Show variable"),

  // Pen
  PEN_DOWN(BrickCategory.PEN, "Pen down"),
  PEN_UP(BrickCategory.PEN, "Pen up"),
  SET_PEN_COLOR(BrickCategory.PEN, "Set pen color"),
  SET_PEN_SIZE(BrickCategory.PEN, "Set pen size"),
  CLEAR_PEN(BrickCategory.PEN, "Clear pen drawing"),

  // Artificial Intelligence
  AI_ASK_PROMPT(BrickCategory.AI, "Ask AI prompt into variable"),
  AI_CLASSIFY_TEXT(BrickCategory.AI, "AI classify text sentiment"),
  AI_GENERATE_NPC_DIALOGUE(BrickCategory.AI, "AI generate dialogue"),

  // Network & MQTT (broker.emqx.io)
  MQTT_CONNECT(BrickCategory.NETWORK, "Connect MQTT to broker.emqx.io"),
  MQTT_PUBLISH(BrickCategory.NETWORK, "MQTT publish to topic"),
  WHEN_MQTT_RECEIVED(BrickCategory.NETWORK, "When MQTT message on topic"),
  HTTP_GET_REQUEST(BrickCategory.NETWORK, "HTTP GET URL into variable"),
  OPEN_WEBVIEW(BrickCategory.NETWORK, "Open WebView URL"),
  PLAY_VIDEO_URL(BrickCategory.NETWORK, "Play Video / YouTube URL"),

  // Device & Sensors
  VIBRATE_DEVICE(BrickCategory.DEVICE, "Vibrate phone ms"),
  TEXT_TO_SPEECH(BrickCategory.DEVICE, "Speak text aloud")
}

data class Brick(
  val id: String = UUID.randomUUID().toString(),
  val op: BrickOp,
  val paramString1: String = "", // e.g. broadcast, variable, sound, message, prompt, URL, topic
  val paramString2: String = "", // op (==, >, <), color, or second string
  val paramString3: String = "", // payload, secret or channel
  val paramNum1: Float = 0f,    // steps, degrees, seconds, x, size, value, vx, gravityX, zoom, ms
  val paramNum2: Float = 0f,    // y, note frequency, vy, gravityY
  val childBricks: List<Brick> = emptyList() // For compound bricks
)

data class Script(
  val id: String = UUID.randomUUID().toString(),
  val header: Brick = Brick(op = BrickOp.WHEN_SCENE_STARTS),
  val bricks: List<Brick> = emptyList()
)

data class CostumeLook(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val emojiOrIcon: String,
  val tintHex: String = "#FF9800",
  val shapeType: String = "EMOJI" // "EMOJI", "CIRCLE", "STAR", "RECT"
)

data class ProgramObject(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val isBackground: Boolean = false,
  val looks: List<CostumeLook> = listOf(CostumeLook(name = "Default", emojiOrIcon = "🐱")),
  val currentLookIndex: Int = 0,
  val initialX: Float = 0f,
  val initialY: Float = 0f,
  val initialRotation: Float = 0f,
  val initialSize: Float = 100f,
  val scripts: List<Script> = emptyList()
)

data class Project(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val description: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val backgroundHex: String = "#0F172A",
  val objects: List<ProgramObject> = emptyList(),
  val variables: Map<String, Float> = mapOf("score" to 0f),
  val isPreset: Boolean = false,
  val mqttConfig: MqttConfig = MqttConfig()
)
