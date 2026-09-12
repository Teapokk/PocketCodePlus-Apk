package com.example.engine

enum class LogLevel {
  INFO,
  WARNING,
  ERROR,
  SUCCESS
}

enum class LogSource {
  SYNTAX_CHECK,
  RUNTIME,
  MQTT,
  AI,
  PHYSICS,
  CAMERA,
  WEB
}

data class DiagnosticLog(
  val id: String = java.util.UUID.randomUUID().toString(),
  val timestamp: Long = System.currentTimeMillis(),
  val level: LogLevel,
  val source: LogSource,
  val tag: String,
  val message: String,
  val actorName: String? = null,
  val scriptHeader: String? = null,
  val brickName: String? = null,
  val suggestion: String? = null
)
