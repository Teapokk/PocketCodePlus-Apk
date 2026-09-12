package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.RuntimeEngine
import com.example.model.Project
import com.example.repository.MqttConfigRepository
import com.example.ui.theme.PocketAccentAmber
import com.example.ui.theme.PocketTealPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StagePlayerScreen(
  project: Project,
  onBack: () -> Unit,
  onOpenMqttConfig: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val mqttRepository = remember { MqttConfigRepository.getInstance(context) }
  val savedMqttConfig by mqttRepository.config.collectAsState()

  val engine = remember(project) {
    RuntimeEngine(project).apply {
      activeMqttConfig = savedMqttConfig
    }
  }

  LaunchedEffect(savedMqttConfig) {
    engine.activeMqttConfig = savedMqttConfig
  }

  var showGrid by remember { mutableStateOf(false) }
  var showLiveLogsSheet by remember { mutableStateOf(false) }

  DisposableEffect(engine) {
    engine.start()
    onDispose {
      engine.stop()
    }
  }

  val bgColor = remember(project.backgroundHex) {
    try {
      Color(android.graphics.Color.parseColor(project.backgroundHex))
    } catch (_: Exception) {
      Color(0xFF0F172A)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF080C14))
  ) {
    // Top Control Bar
    Surface(
      color = Color(0xFF1E293B),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              engine.stop()
              onBack()
            },
            modifier = Modifier.testTag("stage_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
          Column {
            Text(
              text = project.title,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (engine.isPaused) "PAUSED" else if (engine.isPlaying) "RUNNING" else "STOPPED",
                color = if (engine.isPaused) PocketAccentAmber else Color(0xFF4ADE80),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              if (engine.cameraFollowObjectId != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "• CAM: SPRITE LOCK",
                  color = Color(0xFF00E5FF),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // MQTT Broker Config
          if (onOpenMqttConfig != null) {
            IconButton(
              onClick = onOpenMqttConfig,
              modifier = Modifier.testTag("stage_mqtt_config_button")
            ) {
              Icon(
                Icons.Default.Hub,
                contentDescription = "MQTT Broker Config",
                tint = if (engine.mqttConnected) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.75f)
              )
            }
          }

          // Toggle Grid
          IconButton(
            onClick = { showGrid = !showGrid },
            modifier = Modifier.testTag("toggle_grid_button")
          ) {
            Icon(
              Icons.Default.GridOn,
              contentDescription = "Toggle Grid",
              tint = if (showGrid) PocketAccentAmber else Color.White.copy(alpha = 0.6f)
            )
          }

          // Live Runtime Diagnostics Logs Sheet Toggle
          IconButton(
            onClick = { showLiveLogsSheet = true },
            modifier = Modifier.testTag("toggle_live_logs_button")
          ) {
            Icon(
              Icons.Default.Terminal,
              contentDescription = "Live Logs",
              tint = if (engine.runtimeLogs.isNotEmpty()) PocketAccentAmber else Color.White.copy(alpha = 0.7f)
            )
          }

          // Pause / Play toggle
          IconButton(
            onClick = {
              if (engine.isPaused) engine.resume() else engine.pause()
            },
            modifier = Modifier.testTag("pause_play_button")
          ) {
            Icon(
              if (engine.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (engine.isPaused) "Resume" else "Pause",
              tint = Color.White
            )
          }

          // Restart
          IconButton(
            onClick = { engine.restart() },
            modifier = Modifier.testTag("restart_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
          }
        }
      }
    }

    // Telemetry and HUD Bar (Variables, MQTT status, Camera Zoom)
    Surface(
      color = Color(0xFF0F172A),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Variables Pills
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.weight(1f)
        ) {
          engine.variables.forEach { (name, value) ->
            Surface(
              color = Color(0xFF1E293B),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "$name: ",
                  color = PocketAccentAmber,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = if (value % 1f == 0f) "${value.toInt()}" else String.format("%.1f", value),
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // Camera & MQTT Badges
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (engine.cameraZoom != 100f || engine.cameraPanX != 0f || engine.cameraPanY != 0f) {
            Surface(
              color = Color(0xFF00E5FF).copy(alpha = 0.15f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("${engine.cameraZoom.toInt()}%", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
          }

          if (engine.mqttConnected) {
            Surface(
              color = Color(0xFF10B981).copy(alpha = 0.2f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("EMQX", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }
      }
    }

    // Game Canvas Area
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
        .testTag("stage_canvas_box")
    ) {
      val centerX = constraints.maxWidth / 2f
      val centerY = constraints.maxHeight / 2f

      val scaleX = constraints.maxWidth / 320f
      val scaleY = constraints.maxHeight / 560f
      val baseScale = minOf(scaleX, scaleY)

      // Camera transformations
      val camZoom = (engine.cameraZoom / 100f).coerceIn(0.1f, 5.0f)
      val effectiveScale = baseScale * camZoom
      val camPanX = engine.cameraPanX
      val camPanY = engine.cameraPanY

      // Pen Drawings Canvas
      Canvas(
        modifier = Modifier.fillMaxSize()
      ) {
        // Optional coordinate grid overlay
        if (showGrid) {
          val gridPaint = Color.White.copy(alpha = 0.15f)
          val axisPaint = PocketAccentAmber.copy(alpha = 0.4f)
          // X and Y axes shifted by camera pan
          val gridCenterX = centerX - camPanX * effectiveScale
          val gridCenterY = centerY + camPanY * effectiveScale

          drawLine(axisPaint, Offset(0f, gridCenterY), Offset(size.width, gridCenterY), strokeWidth = 2f)
          drawLine(axisPaint, Offset(gridCenterX, 0f), Offset(gridCenterX, size.height), strokeWidth = 2f)

          for (x in -250..250 step 50) {
            val px = gridCenterX + x * effectiveScale
            drawLine(gridPaint, Offset(px, 0f), Offset(px, size.height), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
          }
          for (y in -350..350 step 50) {
            val py = gridCenterY - y * effectiveScale
            drawLine(gridPaint, Offset(0f, py), Offset(size.width, py), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
          }
        }

        // Render pen strokes with camera transformation
        engine.penStrokes.value.forEach { stroke ->
          val pColor = try {
            Color(android.graphics.Color.parseColor(stroke.colorHex))
          } catch (_: Exception) {
            Color.Cyan
          }
          val start = Offset(
            centerX + (stroke.fromX - camPanX) * effectiveScale,
            centerY - (stroke.fromY - camPanY) * effectiveScale
          )
          val end = Offset(
            centerX + (stroke.toX - camPanX) * effectiveScale,
            centerY - (stroke.toY - camPanY) * effectiveScale
          )
          drawLine(
            color = pColor,
            start = start,
            end = end,
            strokeWidth = stroke.strokeWidth * effectiveScale,
            cap = StrokeCap.Round
          )
        }
      }

      // Sprites / Actors rendering
      project.objects.forEach { obj ->
        val state = engine.objectStates[obj.id]
        if (state != null && state.visible) {
          val activeLook = obj.looks.getOrNull(state.lookIndex) ?: obj.looks.firstOrNull()
          val screenPxX = centerX + (state.x - camPanX) * effectiveScale
          val screenPxY = centerY - (state.y - camPanY) * effectiveScale
          val spriteSize = (64 * effectiveScale).coerceAtLeast(24f)

          Box(
            modifier = Modifier
              .offset {
                IntOffset(
                  (screenPxX - spriteSize / 2f).roundToInt(),
                  (screenPxY - spriteSize / 2f).roundToInt()
                )
              }
              .size(spriteSize.dp)
              .rotate(state.rotation)
              .scale(state.sizePercent / 100f)
              .pointerInput(obj.id) {
                detectTapGestures {
                  engine.onObjectTapped(obj.id)
                }
              }
              .testTag("sprite_${obj.id}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = activeLook?.emojiOrIcon ?: "⭐",
              fontSize = (32 * effectiveScale).coerceAtLeast(16f).sp
            )
          }

          // Speech Bubble if actor is saying something
          val speech = state.speechText
          if (speech != null) {
            Box(
              modifier = Modifier
                .offset {
                  IntOffset(
                    screenPxX.roundToInt() - 60,
                    (screenPxY - 45 * effectiveScale).roundToInt()
                  )
                }
            ) {
              Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                shadowElevation = 4.dp
              ) {
                Text(
                  text = speech,
                  color = Color.Black,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // Video Playback Overlay (Triggered by PLAY_VIDEO_URL brick)
      val activeVideo = engine.activeVideoUrl
      if (activeVideo != null) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Videocam, contentDescription = null, tint = PocketAccentAmber)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Video Stream Player", fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = { engine.activeVideoUrl = null }) {
                  Icon(Icons.Default.Close, contentDescription = "Close Video", tint = Color.White)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Simulated video player canvas frame
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Surface(
                    color = PocketAccentAmber,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Streaming Media...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = activeVideo,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }
      }

      // WebView In-App Browser Overlay (Triggered by OPEN_WEBVIEW brick)
      val activeWeb: String? = engine.activeWebViewUrl
      if (activeWeb != null) {
        val webUrl = activeWeb
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF1E293B))
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = webUrl,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                  )
                }
                IconButton(onClick = { engine.activeWebViewUrl = null }) {
                  Icon(Icons.Default.Close, contentDescription = "Close Browser", tint = Color.White)
                }
              }

              AndroidView(
                factory = { ctx ->
                  WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(webUrl)
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }
  }

  // Live Runtime Diagnostics Logs Sheet
  if (showLiveLogsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showLiveLogsSheet = false },
      containerColor = MaterialTheme.colorScheme.surface,
      dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
      DiagnosticsLogView(
        project = project,
        runtimeLogs = engine.runtimeLogs,
        onClearRuntimeLogs = { engine.clearRuntimeLogs() },
        onOpenMqttConfig = onOpenMqttConfig
      )
    }
  }
}
