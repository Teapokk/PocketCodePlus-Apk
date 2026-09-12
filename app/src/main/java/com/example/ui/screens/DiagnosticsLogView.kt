package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DiagnosticLog
import com.example.engine.LogLevel
import com.example.engine.LogSource
import com.example.engine.SyntaxAnalyzer
import com.example.model.Project
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsLogView(
  project: Project,
  runtimeLogs: List<DiagnosticLog> = emptyList(),
  onClearRuntimeLogs: (() -> Unit)? = null,
  onOpenMqttConfig: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var filterLevel by remember { mutableStateOf<LogLevel?>(null) }
  var filterSource by remember { mutableStateOf<LogSource?>(null) }
  var refreshTrigger by remember { mutableStateOf(0) }

  // Analyze project whenever project changes or refreshed
  val analysis = remember(project, refreshTrigger) {
    SyntaxAnalyzer.analyzeProject(project)
  }

  // Combined logs: static syntax logs + runtime events
  val allLogs = remember(analysis, runtimeLogs, filterLevel, filterSource) {
    val combined = analysis.logs + runtimeLogs.reversed()
    combined.filter { log ->
      (filterLevel == null || log.level == filterLevel) &&
        (filterSource == null || log.source == filterSource)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
      .testTag("diagnostics_log_view")
  ) {
    // Health Score Dashboard Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Block Health Score",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${analysis.totalBricks} blocks in ${analysis.totalScripts} scripts",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          val scoreColor = when {
            analysis.healthScore >= 90 -> Color(0xFF10B981)
            analysis.healthScore >= 70 -> Color(0xFFF59E0B)
            else -> Color(0xFFEF4444)
          }

          Surface(
            color = scoreColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${analysis.healthScore}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = scoreColor,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
          progress = { analysis.healthScore / 100f },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = when {
            analysis.healthScore >= 90 -> Color(0xFF10B981)
            analysis.healthScore >= 70 -> Color(0xFFF59E0B)
            else -> Color(0xFFEF4444)
          },
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stat Badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatChip(label = "Errors", count = analysis.errorCount, color = Color(0xFFEF4444), icon = Icons.Default.Error)
          StatChip(label = "Warnings", count = analysis.warningCount, color = Color(0xFFF59E0B), icon = Icons.Default.Warning)
          StatChip(label = "Hints", count = analysis.infoCount, color = Color(0xFF3B82F6), icon = Icons.Default.Info)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Controls and Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Diagnostic Log Entries (${allLogs.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { refreshTrigger++ },
          modifier = Modifier.size(32.dp).testTag("refresh_diagnostics")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
        }
        if (runtimeLogs.isNotEmpty() && onClearRuntimeLogs != null) {
          IconButton(
            onClick = onClearRuntimeLogs,
            modifier = Modifier.size(32.dp).testTag("clear_logs")
          ) {
            Icon(Icons.Default.ClearAll, contentDescription = "Clear logs", modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = filterLevel == null && filterSource == null,
          onClick = {
            filterLevel = null
            filterSource = null
          },
          label = { Text("All Logs", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors()
        )
      }
      item {
        FilterChip(
          selected = filterLevel == LogLevel.ERROR,
          onClick = { filterLevel = if (filterLevel == LogLevel.ERROR) null else LogLevel.ERROR },
          label = { Text("Errors (${analysis.errorCount})", fontSize = 12.sp) }
        )
      }
      item {
        FilterChip(
          selected = filterLevel == LogLevel.WARNING,
          onClick = { filterLevel = if (filterLevel == LogLevel.WARNING) null else LogLevel.WARNING },
          label = { Text("Warnings (${analysis.warningCount})", fontSize = 12.sp) }
        )
      }
      item {
        FilterChip(
          selected = filterSource == LogSource.SYNTAX_CHECK,
          onClick = { filterSource = if (filterSource == LogSource.SYNTAX_CHECK) null else LogSource.SYNTAX_CHECK },
          label = { Text("Syntax", fontSize = 12.sp) }
        )
      }
      item {
        FilterChip(
          selected = filterSource == LogSource.MQTT,
          onClick = { filterSource = if (filterSource == LogSource.MQTT) null else LogSource.MQTT },
          label = { Text("MQTT", fontSize = 12.sp) }
        )
      }
      if (onOpenMqttConfig != null) {
        item {
          AssistChip(
            onClick = onOpenMqttConfig,
            label = { Text("Broker Setup", fontSize = 11.sp) },
            leadingIcon = {
              Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(14.dp))
            },
            modifier = Modifier.testTag("log_open_mqtt_setup")
          )
        }
      }
      item {
        FilterChip(
          selected = filterSource == LogSource.AI,
          onClick = { filterSource = if (filterSource == LogSource.AI) null else LogSource.AI },
          label = { Text("AI", fontSize = 12.sp) }
        )
      }
      item {
        FilterChip(
          selected = filterSource == LogSource.PHYSICS,
          onClick = { filterSource = if (filterSource == LogSource.PHYSICS) null else LogSource.PHYSICS },
          label = { Text("Physics", fontSize = 12.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Log list
    if (allLogs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No diagnostic errors found!",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = "All blocks follow standard Pocket Code syntax rules.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(allLogs) { log ->
          DiagnosticLogCard(log)
        }
      }
    }
  }
}

@Composable
private fun StatChip(
  label: String,
  count: Int,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Surface(
    color = color.copy(alpha = 0.1f),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "$label: $count",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}

@Composable
private fun DiagnosticLogCard(log: DiagnosticLog) {
  val (color, icon) = when (log.level) {
    LogLevel.ERROR -> Color(0xFFEF4444) to Icons.Default.Error
    LogLevel.WARNING -> Color(0xFFF59E0B) to Icons.Default.Warning
    LogLevel.SUCCESS -> Color(0xFF10B981) to Icons.Default.CheckCircle
    LogLevel.INFO -> Color(0xFF3B82F6) to Icons.Default.Info
  }

  Card(
    modifier = Modifier.fillMaxWidth().testTag("diagnostic_log_${log.id}"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .background(color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = color.copy(alpha = 0.2f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = log.tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
            if (log.actorName != null) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Actor: ${log.actorName}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          val timeStr = remember(log.timestamp) {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
          }
          Text(
            text = timeStr,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = log.message,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (log.suggestion != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Fix: ${log.suggestion}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
              )
            }
          }
        }
      }
    }
  }
}
