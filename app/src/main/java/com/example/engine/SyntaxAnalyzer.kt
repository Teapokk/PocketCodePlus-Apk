package com.example.engine

import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.Project

object SyntaxAnalyzer {

  data class AnalysisResult(
    val healthScore: Int, // 0 to 100
    val totalBricks: Int,
    val totalScripts: Int,
    val errorCount: Int,
    val warningCount: Int,
    val infoCount: Int,
    val logs: List<DiagnosticLog>
  )

  fun analyze(project: Project): AnalysisResult = analyzeProject(project)

  fun analyzeProject(project: Project): AnalysisResult {
    val logs = mutableListOf<DiagnosticLog>()
    var totalBricks = 0
    var totalScripts = 0

    // Collect all defined variables
    val definedVariables = project.variables.keys.toMutableSet()
    
    // Collect all broadcast messages used in receivers
    val broadcastReceivers = mutableSetOf<String>()
    val broadcastSenders = mutableSetOf<Pair<String, String>>() // (message, actorName)
    val mqttReceivers = mutableSetOf<String>()

    // First pass: gather declarations
    project.objects.forEach { obj ->
      totalScripts += obj.scripts.size
      obj.scripts.forEach { script ->
        if (script.header.op == BrickOp.WHEN_BROADCAST_RECEIVED) {
          val msg = script.header.paramString1.trim().lowercase()
          if (msg.isNotEmpty()) broadcastReceivers.add(msg)
        }
        if (script.header.op == BrickOp.WHEN_MQTT_RECEIVED) {
          val topic = script.header.paramString1.trim()
          if (topic.isNotEmpty()) mqttReceivers.add(topic)
        }

        fun scanBricks(bricks: List<Brick>) {
          for (b in bricks) {
            totalBricks++
            if (b.op == BrickOp.SET_VARIABLE || b.op == BrickOp.CHANGE_VARIABLE_BY) {
              val v = b.paramString1.trim()
              if (v.isNotEmpty()) definedVariables.add(v)
            }
            if (b.op == BrickOp.BROADCAST) {
              val msg = b.paramString1.trim().lowercase()
              if (msg.isNotEmpty()) broadcastSenders.add(msg to obj.name)
            }
            scanBricks(b.childBricks)
          }
        }
        scanBricks(script.bricks)
      }
    }

    // Second pass: deep inspection
    project.objects.forEach { obj ->
      obj.scripts.forEach { script ->
        val headerTitle = script.header.op.defaultLabel
        var encounteredForever = false

        script.bricks.forEachIndexed { index, brick ->
          val brickName = brick.op.defaultLabel

          // Check unreachable code after Forever
          if (encounteredForever) {
            logs.add(
              DiagnosticLog(
                level = LogLevel.WARNING,
                source = LogSource.SYNTAX_CHECK,
                tag = "DEAD_CODE",
                message = "Unreachable brick '$brickName' placed after Forever loop in '${obj.name}'",
                actorName = obj.name,
                scriptHeader = headerTitle,
                brickName = brickName,
                suggestion = "Move this brick inside the loop, before the loop, or to another script."
              )
            )
          }

          if (brick.op == BrickOp.FOREVER) {
            encounteredForever = true
            // Check if Forever loop has any delay/wait or yield
            val hasDelay = hasWaitOrDelay(brick.childBricks)
            if (!hasDelay && brick.childBricks.size > 2) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.WARNING,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "HIGH_CPU_LOOP",
                  message = "Forever loop in '${obj.name}' has no 'Wait seconds' block; it may run excessively fast.",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Add a small 'Wait 0.05 seconds' brick inside the loop to smooth out performance."
                )
              )
            }
          }

          // Check variables read before definition
          if (brick.op == BrickOp.IF_VARIABLE) {
            val varName = brick.paramString1.trim()
            if (varName.isEmpty()) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.ERROR,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "MISSING_VAR_PARAM",
                  message = "If variable condition has empty variable name in '${obj.name}'",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Choose a variable such as 'score' or 'lives'."
                )
              )
            } else if (!definedVariables.contains(varName)) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.WARNING,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "UNDEFINED_VARIABLE",
                  message = "Variable '$varName' is checked before being defined in project variables.",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Initialize '$varName' with a 'Set variable' brick or add it in project variables."
                )
              )
            }
          }

          // Check empty broadcasts
          if (brick.op == BrickOp.BROADCAST) {
            val msg = brick.paramString1.trim()
            if (msg.isEmpty()) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.ERROR,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "EMPTY_BROADCAST",
                  message = "Broadcast brick in '${obj.name}' has empty message name.",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Give your broadcast message a descriptive name like 'game_over' or 'next_level'."
                )
              )
            }
          }

          // Check MQTT Publish
          if (brick.op == BrickOp.MQTT_PUBLISH) {
            if (brick.paramString1.isBlank()) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.WARNING,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "MQTT_EMPTY_TOPIC",
                  message = "MQTT publish topic is empty in '${obj.name}'.",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Set a topic like 'pocketcode/game/score' on broker.emqx.io"
                )
              )
            }
          }

          // Check AI Ask Prompt
          if (brick.op == BrickOp.AI_ASK_PROMPT) {
            if (brick.paramString1.isBlank()) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.WARNING,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "AI_EMPTY_PROMPT",
                  message = "AI prompt is empty in '${obj.name}'.",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Enter a question or story prompt for the AI to generate."
                )
              )
            }
          }

          // Check Web Request URL
          if (brick.op == BrickOp.HTTP_GET_REQUEST || brick.op == BrickOp.OPEN_WEBVIEW) {
            val url = brick.paramString1.trim()
            if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
              logs.add(
                DiagnosticLog(
                  level = LogLevel.WARNING,
                  source = LogSource.SYNTAX_CHECK,
                  tag = "INVALID_URL_SCHEME",
                  message = "Web URL '$url' should begin with 'https://'",
                  actorName = obj.name,
                  scriptHeader = headerTitle,
                  brickName = brickName,
                  suggestion = "Prepend 'https://' to the web address."
                )
              )
            }
          }

          // Check invalid size %
          if (brick.op == BrickOp.SET_SIZE_PERCENT && brick.paramNum1 <= 0f) {
            logs.add(
              DiagnosticLog(
                level = LogLevel.WARNING,
                source = LogSource.SYNTAX_CHECK,
                tag = "ZERO_SIZE",
                message = "Set size is set to 0% or negative in '${obj.name}', which makes actor invisible.",
                actorName = obj.name,
                scriptHeader = headerTitle,
                brickName = brickName,
                suggestion = "Use a percentage between 20% and 300%."
              )
            )
          }

          // Check wait seconds
          if (brick.op == BrickOp.WAIT_SECONDS && brick.paramNum1 < 0f) {
            logs.add(
              DiagnosticLog(
                level = LogLevel.ERROR,
                source = LogSource.SYNTAX_CHECK,
                tag = "NEGATIVE_DELAY",
                message = "Wait seconds cannot be negative (${brick.paramNum1}s) in '${obj.name}'.",
                actorName = obj.name,
                scriptHeader = headerTitle,
                brickName = brickName,
                suggestion = "Set wait seconds to a positive number like 1.0 or 0.5."
              )
            )
          }
        }
      }
    }

    // Check for orphan broadcast senders (broadcast sent but no receiver)
    broadcastSenders.forEach { (msg, senderActor) ->
      if (!broadcastReceivers.contains(msg)) {
        logs.add(
          DiagnosticLog(
            level = LogLevel.INFO,
            source = LogSource.SYNTAX_CHECK,
            tag = "UNHANDLED_BROADCAST",
            message = "Message '$msg' is broadcast by '$senderActor' but no actor has 'When I receive message $msg'.",
            actorName = senderActor,
            suggestion = "Add a 'When I receive message $msg' script header in an actor to respond to this event."
          )
        )
      }
    }

    val errorCount = logs.count { it.level == LogLevel.ERROR }
    val warningCount = logs.count { it.level == LogLevel.WARNING }
    val infoCount = logs.count { it.level == LogLevel.INFO }

    val rawScore = 100 - (errorCount * 25) - (warningCount * 8)
    val healthScore = rawScore.coerceIn(0, 100)

    if (logs.isEmpty()) {
      logs.add(
        DiagnosticLog(
          level = LogLevel.SUCCESS,
          source = LogSource.SYNTAX_CHECK,
          tag = "ALL_CLEAN",
          message = "All $totalBricks visual blocks and $totalScripts scripts passed static syntax verification with zero errors!"
        )
      )
    }

    return AnalysisResult(
      healthScore = healthScore,
      totalBricks = totalBricks,
      totalScripts = totalScripts,
      errorCount = errorCount,
      warningCount = warningCount,
      infoCount = infoCount,
      logs = logs
    )
  }

  private fun hasWaitOrDelay(bricks: List<Brick>): Boolean {
    for (b in bricks) {
      if (b.op == BrickOp.WAIT_SECONDS || b.op == BrickOp.GLIDE_TO_XY) return true
      if (hasWaitOrDelay(b.childBricks)) return true
    }
    return false
  }
}
